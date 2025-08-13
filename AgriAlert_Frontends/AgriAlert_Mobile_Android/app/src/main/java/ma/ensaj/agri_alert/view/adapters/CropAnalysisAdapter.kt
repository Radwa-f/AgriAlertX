package ma.ensaj.agri_alert.view.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.R

import ma.ensaj.agri_alert.model.BackendImageResponse
import ma.ensaj.agri_alert.model.CropAnalysis
import ma.ensaj.agri_alert.network.RetrofitClient

import ma.ensaj.agri_alert.theme.SeverityChip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.util.regex.Pattern

class CropAnalysisAdapter(
    private val cropAnalyses: List<Pair<String, CropAnalysis>>
) : RecyclerView.Adapter<CropAnalysisAdapter.CropAnalysisViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropAnalysisViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crop_analysis, parent, false)
        return CropAnalysisViewHolder(view)
    }

    override fun onBindViewHolder(holder: CropAnalysisViewHolder, position: Int) {
        var (cropName, analysis) = cropAnalyses[position]
        holder.bind(cropName, analysis)


        // Fetch image for the crop name
        RetrofitClient.imagesApi.getCropImage(cropName)
            .enqueue(object : Callback<BackendImageResponse> {
                override fun onResponse(
                    call: Call<BackendImageResponse>,
                    response: Response<BackendImageResponse>
                ) {
                    val imageUrl = response.body()?.url
                    Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.coffee)
                        .into(holder.cropImageView)

                    holder.itemView.setOnClickListener {
                        val intent = Intent(holder.itemView.context, CropsDetailsActivity::class.java)
                        intent.putExtra("CROP_NAME", cropName)
                        intent.putExtra("SEVERITY", analysis.overallSeverity)
                        intent.putExtra("ALERTS", analysis.alerts.joinToString("\n") { "${it.title}: ${it.message}" })
                        intent.putExtra("RECOMMENDATIONS", analysis.recommendations.joinToString("\n") { it.message })
                        intent.putExtra("INSIGHTS", analysis.insights.joinToString("\n"))
                        intent.putExtra("IMAGE_URL", imageUrl)
                        holder.itemView.context.startActivity(intent)
                    }
                }
                override fun onFailure(call: Call<BackendImageResponse>, t: Throwable) {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.coffee)
                        .into(holder.cropImageView)
                }
            })

    }

    override fun getItemCount(): Int = cropAnalyses.size

    class CropAnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cropImageView: ImageView = itemView.findViewById(R.id.news_image)
        private val cropNameTextView: TextView = itemView.findViewById(R.id.tv_crop_name)
        private val severityTextView: TextView = itemView.findViewById(R.id.tv_overall_severity)
        private val alertsTextView: TextView = itemView.findViewById(R.id.tv_alerts_title)

        fun bind(cropName: String, analysis: CropAnalysis) {
            cropNameTextView.text = cropName

            val ml = parseMlTopPrediction(analysis.insights.joinToString("\n")) // Pair(predClass, prob) or null
            val predictedIsThisCrop = ml?.first?.let { keysEqual(it, cropName) } == true
            val prob = ml?.second ?: 0.0

            if (predictedIsThisCrop && prob >= 90.0) {
                // Hide alerts, show success card
                severityTextView.apply {
                    text = "Overall Severity: Low"

                    // Update text color based on severity
                    val colorRes = when (analysis.overallSeverity.uppercase()) {
                        "HIGH" -> R.color.severity_low_text
                        "MEDIUM" -> R.color.severity_low_text
                        "LOW" -> R.color.severity_low_text
                        else -> R.color.severity_low_text
                    }
                    setTextColor(context.getColor(colorRes))
                }
                // Alerts
                val alerts = "Good News! The Weather today is just right for this crop!"
                alertsTextView.text = alerts
                   } else {
                // Set severity text and color based on severity level
                severityTextView.apply {
                    text = "Overall Severity: ${analysis.overallSeverity}"

                    // Update text color based on severity
                    val colorRes = when (analysis.overallSeverity.uppercase()) {
                        "HIGH" -> R.color.severity_high_text
                        "MEDIUM" -> R.color.severity_medium_text
                        "LOW" -> R.color.severity_low_text
                        else -> R.color.severity_low_text
                    }
                    setTextColor(context.getColor(colorRes))
                }

                // Alerts
                val alerts = analysis.alerts.joinToString("\n") { "${it.title}: ${it.message}" }
                alertsTextView.text = alerts
            }

        }
    }



}
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
private fun splitLines(input: String): List<String> =
    input.split(Regex("\\r?\\n"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }



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