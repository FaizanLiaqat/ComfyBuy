package com.muhammadahmedmufii.comfybuy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.ImageView
import android.widget.TextView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import com.bumptech.glide.Glide

class ChatActivity : AppCompatActivity() {
    private val TAG = "ChatActivity"
    private val RTDB_URL = "https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app"

    // UI
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage : TextInputEditText
    private lateinit var btnSend   : ImageView
    private lateinit var btnAttach : ImageView
    private lateinit var tvStatus  : TextView
    private lateinit var btnBack   : ImageView
    private lateinit var profilePic: CircleImageView
    private lateinit var tvName    : TextView

    // Data
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    // Firebase
    private lateinit var db          : FirebaseDatabase
    private lateinit var messagesRef: DatabaseReference
    private lateinit var statusRef  : DatabaseReference
    private lateinit var myStatusRef: DatabaseReference

    // IDs
    private lateinit var chatId      : String
    private lateinit var opponentId  : String
    private lateinit var currentUid  : String

    // pick image
    private val pickImage = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { sendImageAsBase64(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // pull Intent extras
        chatId     = intent.getStringExtra("CHAT_ID") ?: run { finish(); return }
        opponentId = intent.getStringExtra("CHAT_USER_ID") ?: run { finish(); return }
        val chatName = intent.getStringExtra("CHAT_NAME") ?: "Chat"

        // Firebase init
        currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        db         = FirebaseDatabase.getInstance(RTDB_URL)

        // bind UI
        btnBack     = findViewById(R.id.btnBack)
        profilePic  = findViewById(R.id.profilePic)
        tvName      = findViewById(R.id.tvName)
        tvStatus    = findViewById(R.id.tvStatus)
        rvMessages  = findViewById(R.id.rvMessages)
        etMessage   = findViewById(R.id.etMessage)
        btnSend     = findViewById(R.id.btnSend)
        btnAttach   = findViewById(R.id.btnAttach)

        tvName.text = chatName
        btnBack.setOnClickListener { finish() }
        loadOpponentProfilePic()

        // RecyclerView
        adapter = ChatAdapter(messages)
        rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvMessages.adapter = adapter

        // DB refs
        messagesRef = db.reference.child("chats").child(chatId).child("messages")
        statusRef   = db.reference.child("status").child(opponentId)
        myStatusRef = db.reference.child("status").child(currentUid)

        goOnline()
        listenToOpponentStatus()

        btnSend.setOnClickListener { sendMessage() }
        btnAttach.setOnClickListener { pickImage.launch("image/*") }

        listenForMessages()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesRef.removeEventListener(childListener)
        myStatusRef.onDisconnect().cancel()
        myStatusRef.setValue(false)
    }

    // Presence
    private fun goOnline() {
        myStatusRef.onDisconnect().setValue(false)
        myStatusRef.setValue(true)
    }
    private fun listenToOpponentStatus() {
        statusRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val online = snap.getValue(Boolean::class.java) ?: false
                tvStatus.text = if (online) "Online" else "Offline"
                tvStatus.setTextColor(if (online) 0xFF4CAF50.toInt() else 0xFF888888.toInt())
            }
            override fun onCancelled(err: DatabaseError) {}
        })
    }

    // Profile pic loader
    private fun loadOpponentProfilePic() {
        // Fetch the Base64 string directly from the user node
        db.reference.child("users")
            .child(opponentId)
            .child("profileImageBase64")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val b64 = snap.getValue(String::class.java)
                    if (!b64.isNullOrEmpty()) {
                        val bmp = base64ToBitmap(b64)
                        if (bmp != null) {
                            profilePic.setImageBitmap(bmp)
                        } else {
                            Log.e(TAG, "loadOpponentProfilePic: Failed to decode Base64 image")
                        }
                    } else {
                        Log.w(TAG, "loadOpponentProfilePic: No Base64 string found for user $opponentId")
                    }
                }
                override fun onCancelled(err: DatabaseError) {
                    Log.e(TAG, "loadOpponentProfilePic cancelled", err.toException())
                }
            })
    }


    // Messaging
    private lateinit var childListener: ChildEventListener
    private fun listenForMessages() {
        childListener = object: ChildEventListener {
            override fun onChildAdded(snap: DataSnapshot, prev: String?) {
                val dto  = snap.getValue(MessageDto::class.java) ?: return
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date(dto.timestamp))
                val msg = when {
                    dto.imageUrl != null -> {
                        // imageUrl holds Base64 string
                        val uri = base64ToUri(dto.imageUrl)
                        if (dto.senderId == currentUid)
                            ChatMessage.SentImage(uri!!, time)
                        else
                            ChatMessage.ReceivedImage(uri!!, time)
                    }
                    dto.senderId == currentUid ->
                        ChatMessage.SentText(dto.text!!, time)
                    else ->
                        ChatMessage.ReceivedText(dto.text!!, time)
                }
                messages.add(msg)
                adapter.notifyItemInserted(messages.size - 1)
                rvMessages.scrollToPosition(messages.size - 1)
            }
            override fun onCancelled(err: DatabaseError) {}
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
        }
        messagesRef.orderByChild("timestamp").addChildEventListener(childListener)
    }
    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return
        val key = messagesRef.push().key ?: return
        val now = System.currentTimeMillis()
        messagesRef.child(key)
            .setValue(MessageDto(currentUid, text, null, now))
            .addOnSuccessListener { etMessage.text?.clear() }
            .addOnFailureListener { Log.e(TAG, "send failed", it) }
    }

    // Send image as Base64
    private fun sendImageAsBase64(uri: Uri) {
        val key = messagesRef.push().key ?: return
        val now = System.currentTimeMillis()
        val b64 = uriToBase64(uri)
        if (b64 == null) {
            Log.e(TAG, "Failed to convert image to Base64")
            return
        }
        messagesRef.child(key)
            .setValue(MessageDto(currentUid, null, b64, now))
    }

    // Conversion helpers
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) {
            Log.w(TAG, "bitmapToBase64: Received null bitmap.")
            return null
        }
        return try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            val byteArray = baos.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "bitmapToBase64 error", e)
            null
        }
    }

    private fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null
        return try {
            val decoded = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        } catch (e: Exception) {
            Log.e(TAG, "base64ToBitmap error", e)
            null
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val bmp = contentResolver.openInputStream(uri)
                ?.use { BitmapFactory.decodeStream(it) }
            bitmapToBase64(bmp)
        } catch (e: Exception) {
            Log.e(TAG, "uriToBase64 error", e)
            null
        }
    }

    private fun base64ToUri(base64String: String?): Uri? {
        val bmp = base64ToBitmap(base64String) ?: return null
        val file = File(cacheDir, "img_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, out)
        }
        return Uri.fromFile(file)
    }
}
