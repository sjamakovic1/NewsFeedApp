package etf.ri.rma.newsfeedapp.data

import androidx.room.Entity

@Entity(
    tableName   = "NewsTags",
    primaryKeys = ["newsId","tagsId"]
)
data class NewsTagsCrossRef(
    val newsId: Int,
    val tagsId: Int
)
