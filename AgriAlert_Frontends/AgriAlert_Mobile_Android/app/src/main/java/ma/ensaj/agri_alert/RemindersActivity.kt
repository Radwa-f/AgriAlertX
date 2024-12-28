package ma.ensaj.agri_alert

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import ma.ensaj.agri_alert.model.Reminder
import ma.ensaj.agri_alert.model.ReminderDataStore
import ma.ensaj.agri_alert.model.dataStore
import ma.ensaj.agri_alert.util.AddReminderDialog
import ma.ensaj.agri_alert.view.adapters.ReminderAdapter

class RemindersActivity : AppCompatActivity() {
    private lateinit var remindersAdapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()
    private lateinit var reminderDataStore: ReminderDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        reminderDataStore = ReminderDataStore(this)

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        val selectedDate = intent.getStringExtra("selected_date") ?: "No date selected"
        findViewById<TextView>(R.id.tv_selected_date).text = selectedDate

        findViewById<CardView>(R.id.card_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        remindersAdapter = ReminderAdapter(
            reminders,
            onEdit = { reminder ->
                val editDialog = AddReminderDialog(reminder.time.substringBefore(" "), reminder)
                editDialog.show(supportFragmentManager, "EditReminderDialog")

            },
            onDelete = { reminder ->
                showDeleteConfirmation(reminder)
            },
            onUpdate = { reminder ->
                lifecycleScope.launch {
                    reminder.isCompleted = true
                    remindersAdapter.notifyDataSetChanged()
                }
            }
        )

        findViewById<RecyclerView>(R.id.rv_reminders).apply {
            adapter = remindersAdapter
            layoutManager = LinearLayoutManager(this@RemindersActivity)
        }

        findViewById<RecyclerView>(R.id.rv_reminders).apply {
            adapter = remindersAdapter
            layoutManager = LinearLayoutManager(this@RemindersActivity)
        }

        findViewById<CardView>(R.id.card_add_reminder).setOnClickListener {
            val addDialog = AddReminderDialog(selectedDate)
            addDialog.show(supportFragmentManager, "AddReminderDialog")
        }

        loadReminders()
    }

    private fun loadReminders() {
        val selectedDate = intent.getStringExtra("selected_date") ?: "No date selected"

        lifecycleScope.launch {
            reminderDataStore.remindersFlow.collect { remindersList ->

                val filteredReminders = remindersList.filter { reminder ->
                    reminder.time.startsWith(selectedDate)
                }

                reminders.clear()
                reminders.addAll(filteredReminders)
                remindersAdapter.notifyDataSetChanged()
            }
        }
    }


    fun addReminder(reminder: Reminder) {
        lifecycleScope.launch {
            reminders.add(reminder)
            reminderDataStore.saveReminders(reminders)
            remindersAdapter.notifyItemInserted(reminders.size - 1)
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val reminder = reminders[position]

                // Show confirmation dialog
                AlertDialog.Builder(this@RemindersActivity)
                    .setTitle("Delete Reminder")
                    .setMessage("Are you sure you want to delete this reminder?")
                    .setPositiveButton("Yes") { _, _ ->
                        reminders.removeAt(position)
                        remindersAdapter.notifyItemRemoved(position)
                        lifecycleScope.launch {
                            reminderDataStore.saveReminders(reminders)
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        remindersAdapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(findViewById(R.id.rv_reminders))
    }

    private fun showDeleteConfirmation(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Yes") { _, _ ->
                // Remove the reminder from the list
                lifecycleScope.launch {
                    val updatedReminders = reminders.toMutableList().apply { remove(reminder) }
                    remindersAdapter.updateReminders(updatedReminders) // Call update method in adapter
                    remindersAdapter.notifyDataSetChanged()

                    // Optionally remove the reminder from DataStore or database if used
                    val dataStore = ReminderDataStore(this@RemindersActivity)
                    dataStore.saveReminders(updatedReminders)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }


    fun updateReminder(updatedReminder: Reminder) {
        lifecycleScope.launch {
            val updatedList = reminders.map { if (it.id == updatedReminder.id) updatedReminder else it }
            reminders.clear()
            reminders.addAll(updatedList)
            remindersAdapter.notifyDataSetChanged()

            // Save updated list to DataStore or database
            ReminderDataStore(this@RemindersActivity).saveReminders(reminders)
        }
    }

}
