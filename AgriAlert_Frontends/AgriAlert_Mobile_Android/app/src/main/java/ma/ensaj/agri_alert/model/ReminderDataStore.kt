package ma.ensaj.agri_alert.model

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ma.ensaj.agri_alert.model.Reminder

val Context.dataStore by preferencesDataStore(name = "reminders")

object ReminderPreferencesKeys {
    val REMINDERS_KEY = stringPreferencesKey("reminders")
}

class ReminderDataStore(private val context: Context) {

    private val gson = Gson()

    // Save reminders to DataStore
    suspend fun saveReminders(reminders: List<Reminder>) {
        val remindersJson = gson.toJson(reminders)
        context.dataStore.edit { preferences ->
            preferences[ReminderPreferencesKeys.REMINDERS_KEY] = remindersJson
        }
    }

    // Get reminders from DataStore
    val remindersFlow: Flow<List<Reminder>> = context.dataStore.data.map { preferences ->
        val remindersJson = preferences[ReminderPreferencesKeys.REMINDERS_KEY] ?: "[]"
        val type = object : TypeToken<List<Reminder>>() {}.type
        gson.fromJson(remindersJson, type)
    }
}
