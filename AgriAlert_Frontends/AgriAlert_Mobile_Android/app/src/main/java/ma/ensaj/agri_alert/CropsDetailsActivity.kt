package ma.ensaj.agri_alert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import ma.ensaj.agri_alert.databinding.ActivityCropsDetailsBinding

class CropsDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropsDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        // Initialize ViewBinding
        binding = ActivityCropsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from Intent
        val cropName = intent.getStringExtra("CROP_NAME") ?: "Unknown Crop"
        val severity = intent.getStringExtra("SEVERITY")?.uppercase() ?: "UNKNOWN"
        val alerts = intent.getStringExtra("ALERTS") ?: "No Alerts"
        val recommendations = intent.getStringExtra("RECOMMENDATIONS") ?: "No Recommendations"
        val insights = intent.getStringExtra("INSIGHTS") ?: "No Insights"
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // Set data to views
        binding.tvCropName.text = cropName
        binding.tvOverallSeverity.text = "Severity: $severity"

        // Apply color based on severity
        val colorRes = when (severity) {
            "HIGH" -> R.color.severity_high_text
            "MEDIUM" -> R.color.severity_medium_text
            "LOW" -> R.color.severity_low_text
            else -> R.color.severity_low_text
        }
        binding.tvOverallSeverity.setTextColor(ContextCompat.getColor(this, colorRes))

        binding.tvAlerts.text = alerts
        binding.tvRecommendationsTitle.text = recommendations
        binding.tvInsightsTitle.text = insights

        // Load image using Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.coffee) // Replace with your placeholder image
            .into(binding.newsImage)
    }

}
