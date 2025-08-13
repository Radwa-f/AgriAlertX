package ma.ensaj.agri_alert.network


import ma.ensaj.agri_alert.model.BackendImageResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query



interface BackendImagesApiService {
    @GET("api/images/random")
    fun getRandomImage(
        @Query("query") query: String
    ): Call<BackendImageResponse>

    // or use crop name explicitly:
    @GET("api/images/crop")
    fun getCropImage(
        @Query("name") cropName: String
    ): Call<BackendImageResponse>
}
