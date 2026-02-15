package etf.ri.rma.newsfeedapp.data.network.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {
    @GET("news/all")
    suspend fun getAllNewsByCategory(
        @Query("categories") category: String,
        @Query("language") language: String = "en",
        @Query("locale") locale: String = "us",
        @Query("limit") limit: Int = 10,
        @Query("api_token") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<NewsApiResponse>

    @GET("news/similar/{uuid}")
    suspend fun searchSimilarNews(
        @Path("uuid") uuid: String,
        @Query("api_token") apiKey: String
    ): Response<NewsApiResponse>



}