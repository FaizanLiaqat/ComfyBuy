package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: ImageView
    private lateinit var btnAttach: ImageView

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

        rv = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)

        adapter = ChatAdapter(messages)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter

        // — Hard-coded initial messages —
        messages.add(ChatMessage.ReceivedText("Hi! Is the backpack still available?", "10:30 AM"))
        messages.add(ChatMessage.ReceivedText("I can pay via PayPal if that works.", "10:31 AM"))
        messages.add(ChatMessage.SentText("Yes, it’s still available! Are you interested?", "10:32 AM"))
        messages.add(ChatMessage.ReceivedText("Great! Could you tell me more about its condition?", "10:33 AM"))
        adapter.notifyDataSetChanged()
        rv.scrollToPosition(messages.size - 1)

        // — Sending text messages —
        btnSend.setOnClickListener {
            val txt = etMessage.text.toString().trim()
            if (txt.isNotEmpty()) {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                messages.add(ChatMessage.SentText(txt, time))
                adapter.notifyItemInserted(messages.size - 1)
                rv.scrollToPosition(messages.size - 1)
                etMessage.text?.clear()
            }
        }

        // — Attaching images —
        btnAttach.setOnClickListener {
            getImage.launch("image/*")
        }
    }
}
