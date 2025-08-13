package ma.ensaj.agri_alert.network


import ma.ensaj.agri_alert.model.CropAnalysisResponse
import ma.ensaj.agri_alert.model.WeatherAnalysisAutoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface BackendCropAnalysisApiService {
    @POST("api/crops/weather-analysis/auto")
    suspend fun analyzeAuto(@Body body: WeatherAnalysisAutoRequest): Response<CropAnalysisResponse>
}
