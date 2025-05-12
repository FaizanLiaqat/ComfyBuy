package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Messages : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewChats)
        searchEditText = findViewById(R.id.etSearch)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up search functionality
        //setupSearch()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create sample data matching the screenshot
        val messageList = listOf(
            MessageItem(
                "Emily Parker",
                "The vintage lamp is still available?",
                "2m ago",
                R.drawable.avatar_placeholder,
                true // Has unread message
            ),
            MessageItem(
                "Michael Chen",
                "Great! I'll pick it up tomorrow",
                "1h ago",
                R.drawable.avatar_placeholder,
                false
            ),
            MessageItem(
                "Sophie Williams",
                "Can you do $40 for the chair?",
                "2h ago",
                R.drawable.avatar_placeholder,
                true // Has unread message
            )
        )

        // Set up adapter with click listener
        messageAdapter = MessageAdapter(messageList) { position ->
            // Open chat activity when a chat is clicked
            val selectedMessage = messageList[position]
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("CHAT_NAME", selectedMessage.name)
            intent.putExtra("CHAT_PROFILE_PIC", selectedMessage.profilePic)
            startActivity(intent)
        }

        recyclerView.adapter = messageAdapter
    }

//    private fun setupSearch() {
//        // Implement search functionality
//        searchEditText.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                // Filter the chat list based on the search query
//                // This is a placeholder - you would implement actual filtering
//                return true
//            }
//        })
//    }
}