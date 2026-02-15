package etf.ri.rma.newsfeedapp.data

import androidx.room.*
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import etf.ri.rma.newsfeedapp.data.toNewsItem


@Dao
interface SavedNewsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewsBlocking(news: NewsItem): Long


    @Query("SELECT EXISTS(SELECT 1 FROM News WHERE uuid = :uuid)")
    fun existsByUuidBlocking(uuid: String): Boolean


    suspend fun saveNews(news: NewsItem): Boolean = withContext(Dispatchers.IO) {
        if (!existsByUuidBlocking(news.uuid)) {
            insertNewsBlocking(news)
            true
        } else false
    }


    @Transaction
    @Query("SELECT * FROM News")
    fun allNewsBlocking(): List<NewsWithTags>

    suspend fun allNews(): List<NewsWithTags> = withContext(Dispatchers.IO) {
        allNewsBlocking()
    }


    @Transaction
    @Query("SELECT * FROM News WHERE category = :category")
    fun getNewsWithCategoryBlocking(category: String): List<NewsWithTags>

    suspend fun getNewsWithCategory(category: String): List<NewsWithTags> =
        withContext(Dispatchers.IO) { getNewsWithCategoryBlocking(category) }


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTagBlocking(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewsTagCrossRefBlocking(crossRef: NewsTagsCrossRef)

    @Query("SELECT id FROM Tags WHERE value = :value LIMIT 1")
    fun getTagByValueBlocking(value: String): Int?


    @Transaction
    suspend fun addTags(tags: List<String>, newsId: Int): Int = withContext(Dispatchers.IO) {
        var newCount = 0
        for (value in tags) {
            val existing = getTagByValueBlocking(value)
            val tagId = existing ?: run {
                newCount++
                insertTagBlocking(TagEntity(value = value)).toInt()
            }
            insertNewsTagCrossRefBlocking(NewsTagsCrossRef(newsId, tagId))
        }
        newCount
    }


    @Query("""
    SELECT Tags.value 
      FROM Tags 
      JOIN NewsTags ON Tags.id = NewsTags.tagsId 
     WHERE NewsTags.newsId = :newsId
  """)
    fun getTagsBlocking(newsId: Int): List<String>

    suspend fun getTags(newsId: Int): List<String> = withContext(Dispatchers.IO) {
        getTagsBlocking(newsId)
    }


    @Transaction
    @Query("""
    SELECT DISTINCT News.* 
      FROM News 
      JOIN NewsTags ON News.id = NewsTags.newsId 
      JOIN Tags     ON Tags.id = NewsTags.tagsId 
     WHERE Tags.value IN (:tagValues)
   ORDER BY publishedDate DESC
  """)
    fun getSimilarNewsQueryBlocking(tagValues: List<String>): List<NewsWithTags>

    suspend fun getSimilarNews(tagValues: List<String>): List<NewsItem> =
        withContext(Dispatchers.IO) {
            getSimilarNewsQueryBlocking(tagValues)
                .map { it.toNewsItem() }
        }
}
