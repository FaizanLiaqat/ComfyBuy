package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {


    private lateinit var rv: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: ImageView
    private lateinit var btnAttach: ImageView
    private lateinit var btnLocation: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var profilePic: CircleImageView

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            messages.add(ChatMessage.SentImage(it, time))
            adapter.notifyItemInserted(messages.size - 1)
            rv.scrollToPosition(messages.size - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize views
        rv = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        btnLocation = findViewById(R.id.btnLocation)
        btnBack = findViewById(R.id.btnBack)
        tvName = findViewById(R.id.tvName)
        tvStatus = findViewById(R.id.tvStatus)
        profilePic = findViewById(R.id.profilePic)

        val opponentChatUserId = intent.getStringExtra("CHAT_USER_ID") // Get the other user's ID
        val chatName = intent.getStringExtra("CHAT_NAME") ?: "Chat User"
        val profilePicRes = intent.getIntExtra("CHAT_PROFILE_PIC_RES_ID", R.drawable.avatar_placeholder)

        Log.d("ChatActivity", "Chatting with User ID: $opponentChatUserId, Name: $chatName")


        // Set up toolbar
        tvName.text = chatName
        tvStatus.text = "Online"
        profilePic.setImageResource(profilePicRes)

        // Set up back button
        btnBack.setOnClickListener {
            finish()
        }

        // Set up RecyclerView
        adapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter

        // Load initial messages
        loadInitialMessages()

        // Set up click listeners
        setupClickListeners()
    }

    private fun loadInitialMessages() {
        // Clear any existing messages
        messages.clear()

        // Add sample messages to match the screenshot
        messages.add(ChatMessage.ReceivedText("Hi! Is the backpack still available?", "10:30 AM"))
        messages.add(ChatMessage.SentText("Yes, it's still available! Are you interested?", "10:31 AM"))
        messages.add(ChatMessage.ReceivedText("Great! Could you tell me more about its condition?", "10:32 AM"))

        adapter.notifyDataSetChanged()
        rv.scrollToPosition(messages.size - 1)
    }

    private fun setupClickListeners() {
        // Send button click listener
        btnSend.setOnClickListener {
            val txt = etMessage.text.toString().trim()
            if (txt.isNotEmpty()) {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                messages.add(ChatMessage.SentText(txt, time))
                adapter.notifyItemInserted(messages.size - 1)
                rv.scrollToPosition(messages.size - 1)
                etMessage.text?.clear()

                // Simulate received response after a delay
                simulateResponse()
            }
        }

        // Attach button click listener
        btnAttach.setOnClickListener {
            getImage.launch("image/*")
        }

        // Location button click listener
        btnLocation.setOnClickListener {
            // Implement location sharing functionality
            // For now, just show a toast
            android.widget.Toast.makeText(this, "Location sharing coming soon!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun simulateResponse() {
        // Simulate typing indicator
        // In a real app, you would show a typing indicator here

        // Simulate delay before response
        rv.postDelayed({
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val responseOptions = listOf(
                "Thanks for the information!",
                "That sounds great!",
                "I'll get back to you soon.",
                "Can we meet tomorrow to see the item?",
                "Is the price negotiable?",
                "Do you have any other items for sale?"
            )

            val randomResponse = responseOptions.random()
            messages.add(ChatMessage.ReceivedText(randomResponse, time))
            adapter.notifyItemInserted(messages.size - 1)
            rv.scrollToPosition(messages.size - 1)
        }, 1500) // 1.5 second delay
    }
}