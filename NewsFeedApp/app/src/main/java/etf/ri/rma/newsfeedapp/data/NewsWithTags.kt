package etf.ri.rma.newsfeedapp.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import etf.ri.rma.newsfeedapp.model.NewsItem

data class NewsWithTags(
    @Embedded val news: NewsItem,
    @Relation(
        parentColumn  = "id",
        entityColumn  = "id",
        associateBy   = Junction(
            value        = NewsTagsCrossRef::class,
            parentColumn = "newsId",
            entityColumn = "tagsId"
        )
    )
    val imageTags: List<TagEntity>
)
fun NewsWithTags.toNewsItem(): NewsItem {
    return NewsItem(
        id            = news.id,
        uuid          = news.uuid,
        title         = news.title,
        snippet       = news.snippet,
        imageUrl      = news.imageUrl,
        category      = news.category,
        isFeatured    = news.isFeatured,
        source        = news.source,
        publishedDate = news.publishedDate
    ).also { itm ->
        itm.imageTags = imageTags.map { it.value }.toMutableList()
    }
}