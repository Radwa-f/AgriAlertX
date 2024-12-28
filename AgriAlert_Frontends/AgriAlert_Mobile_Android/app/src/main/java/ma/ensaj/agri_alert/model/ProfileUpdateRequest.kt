package ma.ensaj.agri_alert.model

data class ProfileUpdateRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val crops: List<String>
)
