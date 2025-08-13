package ma.ensaj.agri_alert.network


import ma.ensaj.agri_alert.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BackendWeatherApiService {
    @GET("api/weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): WeatherResponse
}