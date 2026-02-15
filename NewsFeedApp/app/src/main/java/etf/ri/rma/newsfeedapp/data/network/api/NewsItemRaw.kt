package etf.ri.rma.newsfeedapp.data.network.api

import com.google.gson.annotations.SerializedName

data class NewsItemRaw(
    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("snippet")
    val snippet: String,

    @SerializedName("image_url")
    val image_url: String?,

    @SerializedName("source")
    val source: String,

    @SerializedName("published_at")
    val published_at: String,

    @SerializedName("categories")
    val categories: List<String>
)
