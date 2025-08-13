package ma.ensaj.agri_alert.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.ChatAdapter
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.databinding.FragmentChatbotBinding
import ma.ensaj.agri_alert.network.ChatBotApi
import ma.ensaj.agri_alert.network.ChatRequest

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val chatMessages = mutableListOf<Pair<String, String>>() // User and Bot messages
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set status bar color if needed (this affects the activity, not just fragment)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.my_dark)
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(chatMessages)
        binding.rvChatConversation.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatConversation.adapter = adapter
    }

    private fun setupClickListeners() {
        // Back button click listener
        binding.root.findViewById<CardView>(R.id.card_back)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Send button click listener
        binding.btnSendMessage.setOnClickListener {
            val userMessage = binding.etMessageInput.text.toString()
            if (userMessage.isNotBlank()) {
                sendMessageToChatBot(userMessage)
                binding.etMessageInput.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessageToChatBot(userMessage: String) {
        // Add user message to the chat
        chatMessages.add(Pair("User", userMessage))
        adapter.notifyItemInserted(chatMessages.size - 1)
        binding.rvChatConversation.scrollToPosition(chatMessages.size - 1)

        // Call the chatbot API
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ChatBotApi.chatbotService.getChatResponse(ChatRequest(userMessage))
                }

                // Add chatbot response to the chat
                chatMessages.add(Pair("Bot", response.response))
                adapter.notifyItemInserted(chatMessages.size - 1)
                binding.rvChatConversation.scrollToPosition(chatMessages.size - 1)
            } catch (e: Exception) {
                Log.e("ChatbotFragment", "Error calling chatbot API: ${e.message}")
                Toast.makeText(requireContext(), "Failed to connect to chatbot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}