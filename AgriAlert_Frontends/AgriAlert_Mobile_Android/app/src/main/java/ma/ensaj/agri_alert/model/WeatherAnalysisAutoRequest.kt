package ma.ensaj.agri_alert.model

data class WeatherAnalysisAutoRequest(
    val latitude: Double?,
    val longitude: Double?,
    val cropNames: List<String>
)
