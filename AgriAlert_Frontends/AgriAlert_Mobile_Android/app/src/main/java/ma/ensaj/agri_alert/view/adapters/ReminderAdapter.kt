package ma.ensaj.agri_alert.view.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.Reminder

class ReminderAdapter(
    private var reminders: List<Reminder>,
    private val onEdit: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit,
    private val onUpdate: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_reminder_title)
        val message: TextView = itemView.findViewById(R.id.tv_reminder_message)
        val time: TextView = itemView.findViewById(R.id.tv_reminder_time)
        val checkbox: CheckBox = itemView.findViewById(R.id.cb_reminder_completed)
        val editIcon: ImageView = itemView.findViewById(R.id.iv_reminder_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        holder.title.text = reminder.title
        holder.message.text = reminder.message
        holder.time.text = reminder.time
        holder.checkbox.isChecked = reminder.isCompleted

        // Cross out completed reminders
        holder.title.paintFlags = if (reminder.isCompleted) {
            holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Checkbox listener
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            reminder.isCompleted = isChecked
            onUpdate(reminder)
            notifyItemChanged(position)
        }

        // Edit icon listener
        holder.editIcon.setOnClickListener {
            onEdit(reminder)
        }
    }
    fun updateReminders(newReminders: List<Reminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }

    override fun getItemCount() = reminders.size
}
