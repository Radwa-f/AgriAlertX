package ma.ensaj.agri_alert

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ma.ensaj.agri_alert.service.CropAnalysisService
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import ma.ensaj.agri_alert.util.TokenHelper
import ma.ensaj.agri_alert.worker.CropAnalysisWorker
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        startCropAnalysisWorker()
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        val logo: ImageView = findViewById(R.id.logo)

        logo.animate()
            .scaleX(0.7f)
            .scaleY(0.7f)
            .setDuration(2000)
            .withEndAction {
                logo.animate()
                    .translationY(-100f)
                    .setDuration(1000)
                    .withEndAction {
                        logo.animate()
                            .alpha(0f)
                            .setDuration(4000)
                            .start()
                    }
                    .start()
            }
            .start()

        val thread = Thread {
            try {
                Thread.sleep(4000)
                val intent = Intent(this, MainActivity::class.java) // Redirect to Login page
                startActivity(intent)
                finish()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    private fun navigateBasedOnSession() {
        val token = SharedPreferencesHelper.getToken(this)

        // Check if token exists and is valid
        if (token.isNullOrEmpty() || TokenHelper.isTokenExpired(token)) {
            // Token is either missing or expired
            SharedPreferencesHelper.deleteToken(this) // Clear expired token
            val intent = Intent(this, MainActivity::class.java) // Redirect to Login page
            startActivity(intent)
        } else {
            // Token is valid
            val intent = Intent(this, HomeActivity::class.java) // Redirect to Home page
            startActivity(intent)
        }
        finish()
    }

    private fun startCropAnalysisWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CropAnalysisWorker>(
            2, TimeUnit.HOURS // Repeat every 6 hours
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "CropAnalysisWorker",
            ExistingPeriodicWorkPolicy.KEEP, // Avoid overwriting existing tasks
            workRequest
        )
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
