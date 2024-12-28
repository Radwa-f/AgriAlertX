package ma.ensaj.agri_alert

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.ActivityUpdateProfileBinding
import ma.ensaj.agri_alert.model.ProfileUpdateRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        // Existing user data from Intent
        val firstName = intent.getStringExtra("firstName")
        val lastName = intent.getStringExtra("lastName")
        val email = intent.getStringExtra("email")
        val phone = intent.getStringExtra("phone")
        val crops = intent.getStringArrayListExtra("crops") ?: arrayListOf()

        // Pre-filled fields
        binding.etFirstName.setText(firstName)
        binding.etLastName.setText(lastName)
        binding.etEmail.setText(email)
        binding.etPhone.setText(phone)
        binding.cropSpinner.setText(crops.joinToString(", "))

        // Updated Profile Button Click Listener
        binding.btnUpdateProfile.setOnClickListener {
            val updatedFirstName = binding.etFirstName.text.toString()
            val updatedLastName = binding.etLastName.text.toString()
            val updatedPhone = binding.etPhone.text.toString()
            val updatedCrops = binding.cropSpinner.text.toString().split(",").map { it.trim() }

            // Validate input
            if (updatedFirstName.isBlank() || updatedLastName.isBlank() || updatedPhone.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Make API call to update profile
            val request = ProfileUpdateRequest(
                firstName = updatedFirstName,
                lastName = updatedLastName,
                phoneNumber = updatedPhone,
                crops = updatedCrops
            )
            updateProfile(request)
        }
    }

    private fun updateProfile(request: ProfileUpdateRequest) {
        lifecycleScope.launch {
            try {
                val token = "Bearer " + SharedPreferencesHelper.getToken(this@UpdateProfileActivity)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.updateProfile(token, request)
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@UpdateProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@UpdateProfileActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@UpdateProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UpdateProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
