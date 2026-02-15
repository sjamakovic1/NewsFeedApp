package etf.ri.rma.newsfeedapp.data.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ImagaApiService {
    @GET("v2/tags")
    suspend fun getTags(
        @Header("Authorization") authHeader: String,
        @Query("image_url") imageUrl: String
    ): Response<ResponseBody>
}

