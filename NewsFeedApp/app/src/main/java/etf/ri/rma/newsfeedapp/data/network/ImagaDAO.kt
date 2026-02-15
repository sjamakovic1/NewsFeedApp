package etf.ri.rma.newsfeedapp.data.network

import android.util.Log
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class ImagaDAO {

    companion object {
        private const val API_KEY = "acc_408d87ab75ea44b"
        private const val API_SECRET = "e70e8438d6fb831b574743acda6b3f54"
    }

    private val imageTagCache = ConcurrentHashMap<String, List<String>>()

    private lateinit var apiService: ImagaApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imagga.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        setApiService(retrofit.create(ImagaApiService::class.java))
    }

    fun setApiService(service: ImagaApiService) {
        this.apiService = service
    }

    suspend fun getTags(imageURL: String): List<String> = withContext(Dispatchers.IO) {
        if (!isValidUrl(imageURL)) throw InvalidImageURLException()

        imageTagCache[imageURL]?.let { return@withContext it }

        val credential = Credentials.basic(API_KEY, API_SECRET)
        val response = apiService.getTags(credential, imageURL)

        if (!response.isSuccessful) return@withContext emptyList()

        val jsonBody = response.body()?.string() ?: return@withContext emptyList()
        val json = JSONObject(jsonBody)
        val tags = json.getJSONObject("result")
            .getJSONArray("tags")
            .let { tagArray ->
                (0 until tagArray.length()).mapNotNull { i ->
                    tagArray.getJSONObject(i).optJSONObject("tag")?.optString("en")
                }
            }

        imageTagCache[imageURL] = tags
        return@withContext tags
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (_: Exception) {
            false
        }
    }
}
