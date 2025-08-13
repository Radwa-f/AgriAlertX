package ma.ensaj.agri_alert.view.adapters


import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.R

class BulletAdapter(
    private val items: List<String>,
    private val dotColorRes: Int
) : RecyclerView.Adapter<BulletAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val dot: View = view.findViewById(R.id.dot)
        val text: TextView = view.findViewById(R.id.tv_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bullet, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ctx: Context = holder.itemView.context
        holder.text.text = items[position]
        holder.dot.backgroundTintList =
            ColorStateList.valueOf(ctx.getColor(dotColorRes))
    }

    override fun getItemCount(): Int = items.size
}
