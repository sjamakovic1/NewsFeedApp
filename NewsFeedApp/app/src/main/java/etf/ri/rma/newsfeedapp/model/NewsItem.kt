package etf.ri.rma.newsfeedapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(tableName = "News")
data class NewsItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val title: String,
    val snippet: String,
    val imageUrl: String?,
    val category: String,
    val isFeatured: Boolean,
    val source: String,
    val publishedDate: String
) {
    @Ignore
    var imageTags: MutableList<String> = mutableListOf()
}
