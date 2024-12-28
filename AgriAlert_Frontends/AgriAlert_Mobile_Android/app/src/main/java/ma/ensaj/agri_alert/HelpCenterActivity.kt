package ma.ensaj.agri_alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HelpCenterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help_center)

        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        // Make sure the root view has the correct ID
        val rootView = findViewById<View>(R.id.main)
        rootView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } ?: run {
            throw IllegalStateException("Root view with id 'main' not found in layout")
        }
        val chatbot = findViewById<CardView>(R.id.card_chatbot)
        chatbot.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java) // Use 'this' instead of 'requireContext'
            startActivity(intent)
        }
        // Handle back navigation
        val backCard = findViewById<CardView>(R.id.my_card_back)
        backCard.setOnClickListener {
            finish() // Finish the current activity and return to the previous one
        }
    }
}
