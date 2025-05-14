// ChatListActivity.kt
package com.muhammadahmedmufii.comfybuy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.muhammadahmedmufii.comfybuy.data.local.UserEntity
import com.muhammadahmedmufii.comfybuy.databinding.FragmentSearchResultsBinding
import com.muhammadahmedmufii.comfybuy.ui.home.HomeFragment
import com.muhammadahmedmufii.comfybuy.ui.profile.ProfileFragment
import com.muhammadahmedmufii.comfybuy.ui.searchresults.SearchResultsFragment

class ChatListActivity : AppCompatActivity() {
    private val TAG = "ChatListActivity"
    private val RTDB_URL = "https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app"

    private lateinit var rvChats: RecyclerView
    private val chats = mutableListOf<ChatListItem>()
    private lateinit var adapter: ChatListAdapter

    private lateinit var db: FirebaseDatabase
    private lateinit var userChatsRef: DatabaseReference
    private lateinit var chatsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference

    private lateinit var currentUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_list)

        currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        db = FirebaseDatabase.getInstance(RTDB_URL)
        userChatsRef = db.reference.child("userChats").child(currentUid)
        chatsRef = db.reference.child("chats")
        usersRef = db.reference.child("users")

        setupBottomNavigationListeners()
        rvChats = findViewById(R.id.rvChats)
        adapter = ChatListAdapter(chats) { item ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("CHAT_ID", item.chatId)
                putExtra("CHAT_USER_ID", item.userId)
                putExtra("CHAT_NAME", item.fullName)
            }
            startActivity(intent)
        }
        rvChats.layoutManager = LinearLayoutManager(this)
        rvChats.adapter = adapter

        loadChatList()
    }

    private fun loadChatList() {
        // 1) get chat IDs
        userChatsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val chatIds = snap.children.mapNotNull { it.key }
                for (chatId in chatIds) {
                    // 2) for each chat, find the other participant
                    chatsRef.child(chatId).child("participants").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(partSnap: DataSnapshot) {
                            val otherId = partSnap.children
                                .mapNotNull { it.key }
                                .firstOrNull { it != currentUid } ?: return

                            // 3) fetch user info
                            usersRef.child(otherId).addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(userSnap: DataSnapshot) {
                                    val user = userSnap.getValue(UserEntity::class.java)
                                    if (user != null) {
                                        chats.add(ChatListItem(
                                            chatId = chatId,
                                            userId = otherId,
                                            fullName = user.fullName ?: "",
                                            profileImageBase64 = user.profileImageBase64
                                        ))
                                        adapter.notifyItemInserted(chats.size - 1)
                                    }
                                }
                                override fun onCancelled(err: DatabaseError) {
                                    Log.e(TAG, "Failed to load user $otherId", err.toException())
                                }
                            })
                        }
                        override fun onCancelled(err: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(err: DatabaseError) {
                Log.e(TAG, "Failed to load userChats for $currentUid", err.toException())
            }
        })
    }

    private fun setupBottomNavigationListeners() {
        // Find the LinearLayouts from the included layout_bottom_navigation.xml
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
          //  replaceFragment(SearchResultsFragment.newInstance(null)) // Pass null or an initial query
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openSearchFragment", true)
            startActivity(intent)
            finish() // Optional, if you want to close the current activity

        }
        findViewById<LinearLayout>(R.id.navSell).setOnClickListener {
            val intent = Intent(this, CreateListingActivity::class.java)
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            //replaceFragment(MessagesFragment.newInstance()) // Navigate to MessagesFragment
            // No longer starts Messages Activity:
            // showPlaceholderToast("Messages")
            //replaceFragment(ChatListActivity.newInstance(null))
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openProfileFragment", true)
            startActivity(intent)
            finish() // Optional, if you want to close the current activity
        }
    }
}
