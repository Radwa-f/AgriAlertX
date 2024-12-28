package ma.ensaj.agri_alert.util

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.RemindersActivity
import ma.ensaj.agri_alert.model.Reminder
import ma.ensaj.agri_alert.worker.ReminderWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AddReminderDialog(
    private val selectedDate: String,
    private val reminder: Reminder? = null // Optional parameter for editing
) : DialogFragment() {

    private lateinit var titleEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var timeButton: Button
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_reminder, container, false)

        titleEditText = view.findViewById(R.id.et_title)
        messageEditText = view.findViewById(R.id.et_message)
        timeButton = view.findViewById(R.id.btn_time)

        // Pre-fill fields if editing
        reminder?.let {
            titleEditText.setText(it.title)
            messageEditText.setText(it.message)
            selectedTime = it.time.substringAfter(" ") // Extract time from "YYYY-MM-DD HH:mm"
            timeButton.text = selectedTime
        }

        // Set up Time Picker
        timeButton.setOnClickListener {
            showTimePicker()
        }

        // Save or update reminder
        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = titleEditText.text.toString()
            val message = messageEditText.text.toString()

            if (title.isBlank()) {
                titleEditText.error = "Title is required"
                return@setOnClickListener
            }

            if (message.isBlank()) {
                messageEditText.error = "Message is required"
                return@setOnClickListener
            }

            if (selectedTime.isEmpty()) {
                timeButton.error = "Please select a time"
                return@setOnClickListener
            }

            // Combine date and time
            val fullDateTime = "$selectedDate $selectedTime"
            val reminderTimeInMillis = parseDateTimeToMillis(fullDateTime)

            if (reminderTimeInMillis == null || reminderTimeInMillis < System.currentTimeMillis()) {
                timeButton.error = "Invalid or past time selected"
                return@setOnClickListener
            }

            Log.d("AddReminderDialog", "Reminder Scheduled for: $fullDateTime")

            val updatedReminder = reminder?.copy(
                title = title,
                message = message,
                time = fullDateTime
            ) ?: Reminder(title = title, message = message, time = fullDateTime)

            // Pass reminder to the activity
            if (reminder != null) {
                (activity as? RemindersActivity)?.updateReminder(updatedReminder)
            } else {
                (activity as? RemindersActivity)?.addReminder(updatedReminder)
            }

            // Schedule notification using WorkManager
            scheduleNotification(updatedReminder, reminderTimeInMillis)

            dismiss()
        }

        return view
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                timeButton.text = selectedTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        // Customize TimePicker dialog appearance
        timePickerDialog.setOnShowListener {
            timePickerDialog.window?.setBackgroundDrawableResource(android.R.color.white)
            timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.my_green))
            timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.my_dark))

            // Access header view and customize it
            val header = timePickerDialog.findViewById<View>(
                resources.getIdentifier("time_header", "id", "android")
            )
            header?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.my_blue))
        }

        timePickerDialog.show()
    }


    private fun parseDateTimeToMillis(dateTime: String): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val date = sdf.parse(dateTime)
            date?.time
        } catch (e: Exception) {
            Log.e("AddReminderDialog", "Error parsing date/time: ${e.message}")
            null
        }
    }

    private fun scheduleNotification(reminder: Reminder, reminderTimeInMillis: Long) {
        val delayInMillis = reminderTimeInMillis - System.currentTimeMillis()

        val data = Data.Builder()
            .putString("title", reminder.title)
            .putString("message", reminder.message)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }
}
