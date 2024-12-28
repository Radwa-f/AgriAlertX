package ma.ensaj.agri_alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ma.ensaj.agri_alert.model.ChangePassword
import ma.ensaj.agri_alert.network.RetrofitClient

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var otpEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        emailEditText = findViewById(R.id.et_email)
        otpEditText = findViewById(R.id.et_otp)
        newPasswordEditText = findViewById(R.id.et_new_password)
        confirmPasswordEditText = findViewById(R.id.et_confirm_password)

        findViewById<View>(R.id.btn_send_otp).setOnClickListener {
            sendOtp()
        }
        findViewById<View>(R.id.btn_verify_otp).setOnClickListener {
            verifyOtp()
        }
        findViewById<View>(R.id.btn_reset_password).setOnClickListener {
            resetPassword()
        }
    }

    private fun navigateToCard(cardId: Int) {
        findViewById<View>(R.id.card_enter_email).visibility = if (cardId == 1) View.VISIBLE else View.GONE
        findViewById<View>(R.id.card_verify_otp).visibility = if (cardId == 2) View.VISIBLE else View.GONE
        findViewById<View>(R.id.card_reset_password).visibility = if (cardId == 3) View.VISIBLE else View.GONE
    }

    private fun sendOtp() {
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            showToast("Please enter your email")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.verfication(email)
                if (response.isSuccessful) {
                    showToast("OTP sent to your email")
                    navigateToCard(2)
                } else {
                    showToast("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showToast("An error occurred: ${e.message}")
            }
        }
    }

    private fun verifyOtp() {
        val otp = otpEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        if (otp.isEmpty()) {
            showToast("Please enter the OTP")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.verifyOtp(otp.toInt(), email)
                if (response.isSuccessful) {
                    showToast("OTP verified")
                    navigateToCard(3)
                } else {
                    showToast("Invalid OTP")
                }
            } catch (e: Exception) {
                showToast("An error occurred: ${e.message}")
            }
        }
    }

    private fun resetPassword() {
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        if (newPassword != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        lifecycleScope.launch {
            try {
                val changePasswordRequest = ChangePassword(newPassword, confirmPassword)
                val response = RetrofitClient.instance.changePassword(changePasswordRequest, email)
                if (response.isSuccessful) {
                    showToast("Password reset successful")
                    startActivity(Intent(this@ForgotPasswordActivity, MainActivity::class.java))
                    finish()
                } else {
                    showToast("Error resetting password")
                }
            } catch (e: Exception) {
                showToast("An error occurred: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
