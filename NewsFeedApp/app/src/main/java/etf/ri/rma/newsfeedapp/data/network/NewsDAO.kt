package etf.ri.rma.newsfeedapp.data.network

import android.util.Log
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.data.SavedNewsDAO
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.toNewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NewsDAO(
    private val savedNewsDAO: SavedNewsDAO
) {
    companion object {
        private const val BASE_URL = "https://api.thenewsapi.com/v1/"
        private const val API_KEY = "87BMPSH5LAMI867wfab6jKhCnpBJXHMjzg7EKUfE"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var apiService = retrofit.create(NewsApiService::class.java)

    private val allNews = mutableListOf<NewsItem>()
    private val lastCategoryCallTime = mutableMapOf<String, LocalDateTime>()
    private val similarNewsCache = mutableMapOf<String, List<NewsItem>>()

    init {
        allNews.addAll(NewsData.getAllNews())
    }

    fun setApiService(service: NewsApiService) {
        this.apiService = service
    }

    suspend fun getTopStoriesByCategory(category: String): List<NewsItem> = withContext(Dispatchers.IO) {
        // cache window
        val now = LocalDateTime.now()
        val lastCall = lastCategoryCallTime[category]
        val recentlyCalled = lastCall != null && Duration.between(lastCall, now).seconds < 30
        lastCategoryCallTime[category] = now

        if (recentlyCalled) {
            return@withContext allNews
                .filter { it.category.equals(category, true) }
                .sortedByDescending { it.isFeatured }
        }

        // pokušaj poziva API-ja
        val resp = try {
            apiService.getAllNewsByCategory(
                category = category,
                apiKey = API_KEY,
                limit = 10
            )
        } catch (e: Exception) {
            Log.e("NewsDAO", "API exception, fallback na DB: ${e.message}")
            // offline fallback
            return@withContext savedNewsDAO
                .getNewsWithCategory(category)
                .map { it.toNewsItem() }
        }

        if (!resp.isSuccessful) {
            Log.e("NewsDAO", "API error ${resp.code()}, fallback na DB")
            return@withContext savedNewsDAO
                .getNewsWithCategory(category)
                .map { it.toNewsItem() }
        }

        val newItems = resp.body()?.data.orEmpty().mapNotNull { raw ->
            if (raw.uuid.isBlank() || raw.title.isBlank()) return@mapNotNull null
            NewsItem(
                uuid = raw.uuid,
                title = raw.title,
                snippet = raw.snippet,
                imageUrl = raw.image_url,
                category = category,
                isFeatured = true,
                source = raw.source,
                publishedDate = formatDate(raw.published_at)
            ).also { it.imageTags = arrayListOf() }
        }

        newItems.forEach { savedNewsDAO.saveNews(it) }
        val top3 = newItems.take(3).map { it.uuid }.toSet()
        newItems.forEach { ni ->
            val idx = allNews.indexOfFirst { it.uuid == ni.uuid }
            if (idx >= 0) allNews[idx] = ni else allNews.add(ni)
        }
        allNews.replaceAll { item ->
            if (item.category.equals(category, true) && item.uuid !in top3)
                item.copy(isFeatured = false)
            else item
        }
        val result = mutableListOf<NewsItem>()
        result += allNews.filter { it.uuid in top3 }
        result += allNews.filter { it.category.equals(category, true) && it.uuid !in top3 }
        return@withContext result.take(3)
    }

    suspend fun getAllStories(): List<NewsItem> = withContext(Dispatchers.IO) {
        val local = savedNewsDAO.allNews().map { it.toNewsItem() }
        if (local.isNotEmpty()) {

            return@withContext local.sortedWith(
                compareByDescending<NewsItem> { it.isFeatured }
                    .thenByDescending { it.publishedDate }
            )
        }

        return@withContext allNews.sortedWith(
            compareByDescending<NewsItem> { it.isFeatured }
                .thenByDescending { it.publishedDate }
        )
    }

    suspend fun getSimilarStories(uuid: String): List<NewsItem> = withContext(Dispatchers.IO) {
        if (!isValidUUID(uuid)) throw InvalidUUIDException()

        similarNewsCache[uuid]?.let { return@withContext it }

        val resp = try {
            apiService.searchSimilarNews(uuid, API_KEY)
        } catch (e: Exception) {
            Log.e("NewsDAO", "Similar API exception: ${e.message}")
            return@withContext emptyList()
        }
        if (!resp.isSuccessful) {
            Log.e("NewsDAO", "Similar API error ${resp.code()}")
            return@withContext emptyList()
        }

        val items = resp.body()?.data.orEmpty()
            .filter { it.uuid.isNotBlank() && it.uuid != uuid }
            .map {
                NewsItem(
                    uuid = it.uuid,
                    title = it.title,
                    snippet = it.snippet,
                    imageUrl = it.image_url.orEmpty(),
                    category = it.categories.firstOrNull() ?: "general",
                    isFeatured = false,
                    source = it.source,
                    publishedDate = formatDate(it.published_at)
                ).also { ni -> ni.imageTags = arrayListOf() }
            }


        items.forEach { savedNewsDAO.saveNews(it) }
        similarNewsCache[uuid] = items
        return@withContext items
    }

    private fun isValidUUID(uuid: String): Boolean = try {
        UUID.fromString(uuid)
        true
    } catch (_: Exception) {
        false
    }

    private fun formatDate(apiDate: String): String = try {
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val dt = LocalDateTime.parse(apiDate, inFmt)
        val outFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        dt.format(outFmt)
    } catch (e: Exception) {
        Log.e("NewsDAO", "Date parse error: $apiDate")
        apiDate
    }
}
