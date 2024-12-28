package ma.ensaj.agri_alert.model

import com.google.gson.annotations.SerializedName

data class LocationUpdateRequest(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)
