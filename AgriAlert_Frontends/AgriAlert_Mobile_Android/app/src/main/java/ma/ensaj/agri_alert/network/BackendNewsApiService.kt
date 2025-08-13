package ma.ensaj.agri_alert.network


import ma.ensaj.agri_alert.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BackendNewsApiService {
    @GET("api/news")
    suspend fun getNews(
        @Query("country") country: String = "ma"
    ): NewsResponse
}
