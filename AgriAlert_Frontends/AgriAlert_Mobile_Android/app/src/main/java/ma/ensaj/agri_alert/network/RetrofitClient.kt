package ma.ensaj.agri_alert.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8087/"

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()


    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val instance: UserService by lazy {
        retrofit.create(UserService::class.java)
    }


    val newsApi: BackendNewsApiService by lazy {
        retrofit.create(BackendNewsApiService::class.java)
    }

    val imagesApi: BackendImagesApiService by lazy {
        retrofit.create(BackendImagesApiService::class.java)
    }

    val weatherBackend: BackendWeatherApiService by lazy {
        retrofit.create(BackendWeatherApiService::class.java)
    }

    val cropAnalysisBackend: BackendCropAnalysisApiService by lazy {
        retrofit.create(BackendCropAnalysisApiService::class.java)
    }


}
