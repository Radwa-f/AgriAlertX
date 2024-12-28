package ma.ensaj.agri_alert.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatBotApi {
    private const val BASE_URL = "http://10.0.2.2:5000" // Emulator to localhost
    //private const val BASE_URL = "http://10.0.2.2:8086"
    val chatbotService: ChatBotService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatBotService::class.java)
    }
}
