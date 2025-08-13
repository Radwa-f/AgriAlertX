package ma.ensaj.agri_alert.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import com.google.android.material.chip.Chip
import ma.ensaj.agri_alert.R
import java.util.Locale
import kotlin.math.min

object SeverityChip {

    fun apply(chip: Chip, ctx: Context, severityRaw: String?) {
        val s = (severityRaw ?: "LOW").uppercase(Locale.ROOT)

        val (bg, fg) = when (s) {
            "HIGH"   -> R.color.severity_high_bg   to R.color.severity_high_text
            "MEDIUM" -> R.color.severity_medium_bg to R.color.severity_medium_text
            else     -> R.color.severity_low_bg    to R.color.severity_low_text
        }

        chip.text = s
        chip.chipBackgroundColor = ColorStateList.valueOf(ctx.getColor(bg))
        chip.setTextColor(ctx.getColor(fg))

        chip.isChipIconVisible = true
        chip.chipIcon = buildIcon(ctx, s, ctx.getColor(fg))
        chip.chipIconTint = null                        // keep our custom colors
        chip.chipIconSize = ctx.dp(18f)
        chip.iconStartPadding = ctx.dp(6f)
        chip.isClickable = false

        chip.chipStrokeWidth = ctx.dp(0.75f)
        chip.chipStrokeColor = ColorStateList.valueOf(ctx.getColor(bg))
    }

    // --- draw icons on the fly (no drawable files) ---
    private fun buildIcon(ctx: Context, severity: String, color: Int): BitmapDrawable {
        val size = ctx.dpInt(18f)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

        when (severity) {
            "HIGH" -> drawWarning(c, paint)        // triangle + !
            "MEDIUM" -> drawInfo(c, paint)         // circle + i
            else -> drawCheck(c, paint)            // circle + check
        }
        return BitmapDrawable(ctx.resources, bmp)
    }

    private fun drawWarning(c: Canvas, p: Paint) {
        val w = c.width.toFloat(); val h = c.height.toFloat()
        // Triangle
        val path = Path().apply {
            moveTo(w / 2f, h * 0.06f)
            lineTo(w * 0.94f, h * 0.94f)
            lineTo(w * 0.06f, h * 0.94f)
            close()
        }
        c.drawPath(path, p)

        // Exclamation (white)
        val ex = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; strokeWidth = h * 0.12f; style = Paint.Style.FILL
        }
        c.drawRect(w/2f - h*0.03f, h*0.35f, w/2f + h*0.03f, h*0.68f, ex)
        c.drawCircle(w/2f, h*0.80f, h*0.06f, ex)
    }

    private fun drawInfo(c: Canvas, p: Paint) {
        val w = c.width.toFloat(); val h = c.height.toFloat()
        val r = min(w, h) * 0.48f
        c.drawCircle(w/2f, h/2f, r, p)

        val t = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD; textSize = h * 0.82f
        }
        // Center text vertically
        val y = h/2f - (t.descent() + t.ascent())/2f
        c.drawText("i", w/2f, y, t)
    }

    private fun drawCheck(c: Canvas, p: Paint) {
        val w = c.width.toFloat(); val h = c.height.toFloat()
        val r = min(w, h) * 0.48f
        c.drawCircle(w/2f, h/2f, r, p)

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; style = Paint.Style.STROKE
            strokeWidth = h * 0.14f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        val path = Path().apply {
            moveTo(w*0.30f, h*0.55f)
            lineTo(w*0.46f, h*0.72f)
            lineTo(w*0.75f, h*0.38f)
        }
        c.drawPath(path, stroke)
    }

    // dp helpers
    private fun Context.dp(v: Float) = v * resources.displayMetrics.density
    private fun Context.dpInt(v: Float) = (dp(v) + 0.5f).toInt()
}
