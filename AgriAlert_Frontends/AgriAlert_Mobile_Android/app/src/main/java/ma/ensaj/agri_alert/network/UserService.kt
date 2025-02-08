package ma.ensaj.agri_alert.network

import ma.ensaj.agri_alert.model.AuthResponse
import ma.ensaj.agri_alert.model.ChangePassword
import ma.ensaj.agri_alert.model.ChatRequest
import ma.ensaj.agri_alert.model.ChatResponse
import ma.ensaj.agri_alert.model.CropAnalysisResponse
import ma.ensaj.agri_alert.model.LocationUpdateRequest
import ma.ensaj.agri_alert.model.LoginRequest
import ma.ensaj.agri_alert.model.ProfileUpdateRequest
import ma.ensaj.agri_alert.model.RegistrationRequest
import ma.ensaj.agri_alert.model.UserProfile
import ma.ensaj.agri_alert.model.WeatherAnalysisRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {

    @POST("api/v1/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<String>


    @POST("api/v1/registration")
    suspend fun registerUser(@Body request: RegistrationRequest): Response<Void>

    @POST("api/crops/weather-analysis")
    suspend fun getWeatherAnalysis(@Body request: WeatherAnalysisRequest): Response<CropAnalysisResponse>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfile>

    @DELETE("api/v1/user/delete")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<String>

    @PUT("api/v1/user/location")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body locationUpdateRequest: LocationUpdateRequest
    ): Response<String>

    @POST("forgotPassword/verification/{email}")
    suspend fun verfication(@Path("email") email: String): Response<String>

    @POST("forgotPassword/verifyOTP/{otp}/{email}")
    suspend fun verifyOtp(@Path("otp") otp: Int, @Path("email") email: String): Response<String>

    @POST("forgotPassword/changePassword/{email}")
    suspend fun changePassword(@Body request: ChangePassword, @Path("email") email: String): Response<String>

    @PUT("api/v1/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<Void>

    @POST("api/chatbot")
    suspend fun getChatResponse(@Body request: ChatRequest): Response<ChatResponse>

}
