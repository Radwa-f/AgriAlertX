package ma.ensaj.agri_alert

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import ma.ensaj.agri_alert.databinding.ActivityCropsDetailsBinding
import ma.ensaj.agri_alert.theme.SeverityChip
import ma.ensaj.agri_alert.view.adapters.BulletAdapter
import android.view.View
import java.util.Locale
import java.util.regex.Pattern

class CropsDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropsDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        binding = ActivityCropsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Get data
        val cropName = intent.getStringExtra("CROP_NAME") ?: "Unknown Crop"
        val severity = intent.getStringExtra("SEVERITY") ?: "LOW"
        val alerts = intent.getStringExtra("ALERTS") ?: ""
        val recommendations = intent.getStringExtra("RECOMMENDATIONS") ?: ""
        val insights = intent.getStringExtra("INSIGHTS") ?: ""
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // --- Header
        binding.tvCropName.text = cropName
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.coffee)
            .into(binding.newsImage)

        val ml = parseMlTopPrediction(insights) // Pair(predClass, prob) or null
        val predictedIsThisCrop = ml?.first?.let { keysEqual(it, cropName) } == true
        val prob = ml?.second ?: 0.0

        // ---- NEW: success state if ML strongly agrees (>=90%)
        if (predictedIsThisCrop && prob >= 90.0) {
            // Hide alerts, show success card
            binding.tvAlertsTitle.visibility = View.GONE
            // if you use a RecyclerView for alerts:
            val rvAlertsId = resources.getIdentifier("rvAlerts", "id", packageName)
            if (rvAlertsId != 0) findViewById<View>(rvAlertsId)?.visibility = View.GONE

            SeverityChip.apply(binding.chipSeverity, this, "Low")
            binding.cardMlOk.visibility = View.VISIBLE
            binding.tvMlOk.text = "Great news! ML says weather looks right for $cropName today (~${prob.toInt()}% match)."
        } else {
            // Normal path: show alerts
            binding.cardMlOk.visibility = View.GONE
            binding.tvAlertsTitle.visibility = View.VISIBLE

            // --- Severity pill
            SeverityChip.apply(binding.chipSeverity, this, severity)

            // --- Alerts list
            binding.rvAlerts.layoutManager = LinearLayoutManager(this)
            binding.rvAlerts.adapter = BulletAdapter(
                splitLines(alerts),
                dotColorRes = R.color.severity_medium    // amber-ish for alerts
            )
        }

        // --- Recommendations list
        binding.rvRecommendations.layoutManager = LinearLayoutManager(this)
        binding.rvRecommendations.adapter = BulletAdapter(
            splitLines(recommendations),
            dotColorRes = R.color.severity_low       // green for actions
        )

        // --- Insights (kept as paragraph; you can also make it a list the same way)
        binding.tvInsightsBody.text = insights
    }

    private fun splitLines(input: String): List<String> =
        input.split(Regex("\\r?\\n"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}

private fun splitLines(input: String): List<String> =
    input.split(Regex("\\r?\\n"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }

/**
 * Parse: "ML top prediction: muskmelon (53.2%)."
 */
private fun parseMlTopPrediction(insights: String): Pair<String, Double>? {
    val pattern = Pattern.compile("ML top prediction:\\s*([A-Za-z\\s()\\-]+)\\s*\\((\\d+(?:\\.\\d+)?)%\\)", Pattern.CASE_INSENSITIVE)
    val m = pattern.matcher(insights)
    return if (m.find()) {
        val klass = m.group(1).trim()
        val pct = m.group(2).toDoubleOrNull() ?: return null
        Pair(klass, pct)
    } else null
}

/**
 * Normalize names so "Coffee beans", "Lentil (Rabi)" etc. match ML classes like "coffee", "lentil".
 */
private fun norm(s: String): String {
    return s.lowercase(Locale.ROOT)
        .replace(Regex("\\(.*?\\)"), "")     // remove parentheses
        .replace(Regex("\\b(beans|grains)\\b"), "") // remove suffixes we add for images
        .replace(Regex("[^a-z]"), "")        // keep letters only
        .trim()
}

private fun keysEqual(a: String, b: String): Boolean = norm(a) == norm(b)

/**
 * Remove ML lines like:
 *  - "ML top prediction: ..."
 *  - "Next: ..."
 *  - "Assumed soil pH = ..."
 */
private fun filterInsightsWithoutMl(insights: String): String {
    val lines = splitLines(insights)
    val filtered = lines.filterNot {
        it.startsWith("ML top prediction:", ignoreCase = true) ||
                it.startsWith("Next:", ignoreCase = true) ||
                it.startsWith("Assumed soil pH", ignoreCase = true)
    }
    return filtered.joinToString("\n")
}