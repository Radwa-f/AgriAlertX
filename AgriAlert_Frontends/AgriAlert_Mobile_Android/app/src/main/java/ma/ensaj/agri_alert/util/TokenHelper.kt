package ma.ensaj.agri_alert.util

import android.util.Base64
import org.json.JSONObject

object TokenHelper {

    // Check if the token is expired
    fun isTokenExpired(token: String): Boolean {
        try {
            // Split the JWT and decode the payload
            val parts = token.split(".")
            if (parts.size < 3) return true // Malformed token

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonObject = JSONObject(payload)

            // Extract the expiration time (exp claim)
            val exp = jsonObject.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000

            // Token is expired if current time > exp
            return exp < currentTime
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true // Treat as expired if decoding fails
    }
}
