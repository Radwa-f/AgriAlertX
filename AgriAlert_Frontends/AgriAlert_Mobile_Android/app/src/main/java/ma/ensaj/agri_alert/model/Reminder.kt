package ma.ensaj.agri_alert.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)  val id: Int = 0,
    val title: String,
    val message: String,
    val time: String,
    var isCompleted: Boolean = false
)
