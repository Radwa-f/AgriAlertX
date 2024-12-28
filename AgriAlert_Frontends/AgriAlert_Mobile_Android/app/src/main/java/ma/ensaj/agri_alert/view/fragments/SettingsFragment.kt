package ma.ensaj.agri_alert.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.HelpCenterActivity
import ma.ensaj.agri_alert.MainActivity
import ma.ensaj.agri_alert.UserProfileActivity
import ma.ensaj.agri_alert.databinding.FragmentSettingsBinding
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SettingsFragment", "onViewCreated called")

        val sharedPref = requireContext().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        val isWeatherNotificationsEnabled = sharedPref.getBoolean("weather_notifications", true) // Default is true
        binding.switchWeatherUpdates.isChecked = isWeatherNotificationsEnabled
        // Edit Profile Click Listener
        binding.tvEditProfile.setOnClickListener {
            Log.d("SettingsFragment", "Edit Profile clicked")
            val intent = Intent(requireContext(), UserProfileActivity::class.java)
            startActivity(intent)
        }

        // Weather Updates Toggle
        binding.switchWeatherUpdates.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableWeatherNotifications(requireContext())
                Toast.makeText(context, "Weather Notifications Enabled", Toast.LENGTH_SHORT).show()
            } else {
                disableWeatherNotifications(requireContext())
                Toast.makeText(context, "Weather Notifications Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvHelpCenter.setOnClickListener {
            Log.d("SettingsFragment", "Help Center clicked")
            val intent = Intent(requireContext(), HelpCenterActivity::class.java)
            startActivity(intent)
        }

        // Logout Button Click
        binding.btnLogout.setOnClickListener {
            Log.d("SettingsFragment", "Logout clicked")


            // Clear user session data
            val sharedPref = requireContext().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear() // Clears all stored data in SharedPreferences
            editor.apply()
            Log.d("SettingsFragment", "Session cleared")

            // Intent to MainActivity
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the activity stack
            startActivity(intent)

            // Show a toast message
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.setOnClickListener {
            deleteAccount()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun clearUserData(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear() // Clears all saved data
            apply()
        }
    }

    private fun deleteAccount() {
        val token = SharedPreferencesHelper.getToken(requireContext())

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "You are not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.deleteAccount("Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()

                        // Clear shared preferences
                        val sharedPref = requireContext().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
                        sharedPref.edit().clear().apply()

                        // Navigate to MainActivity
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete account: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DeleteAccount", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "An error occurred while deleting account", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Enables weather notifications and saves the state to SharedPreferences
     */
    private fun enableWeatherNotifications(context: Context) {
        Log.d("SettingsFragment", "Enabling weather notifications")
        saveNotificationPreference(context, true)
        // Add logic to subscribe to notification topics or start notification services if needed
    }

    /**
     * Disables weather notifications and saves the state to SharedPreferences
     */
    private fun disableWeatherNotifications(context: Context) {
        Log.d("SettingsFragment", "Disabling weather notifications")
        saveNotificationPreference(context, false)
        // Add logic to unsubscribe from notification topics or stop notification services if needed
    }

    /**
     * Saves the notification preference to SharedPreferences
     */
    private fun saveNotificationPreference(context: Context, isEnabled: Boolean) {
        val sharedPref = context.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("weather_notifications", isEnabled)
        editor.apply()
        Log.d("SettingsFragment", "Notification preference saved: $isEnabled")
    }
}
