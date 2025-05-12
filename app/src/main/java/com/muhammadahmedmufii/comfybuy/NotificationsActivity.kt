//package com.muhammadahmedmufii.comfybuy
//
//import android.os.Bundle
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
//class NotificationsActivity : AppCompatActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: NotificationsAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_notifications)
//
//        // Set up back button
//        findViewById<View>(R.id.btnBack).setOnClickListener {
//            onBackPressed()
//        }
//
//        // Set up RecyclerView
//        recyclerView = findViewById(R.id.recyclerViewNotifications)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // Create sample data
//        val notifications = createSampleNotifications()
//
//        // Set up adapter
//        adapter = NotificationsAdapter(this, notifications)
//        recyclerView.adapter = adapter
//    }
//
//    private fun createSampleNotifications(): List<Notification> {
//        return listOf(
//            Notification(
//                id = "1",
//                type = NotificationType.MESSAGE,
//                title = "New message from Sarah",
//                description = "Is the backpack still available?",
//                time = "2m ago"
//            ),
//            Notification(
//                id = "2",
//                type = NotificationType.OFFER,
//                title = "New offer received",
//                description = "Someone made an offer on your vintage camera",
//                time = "1h ago"
//            ),
//            Notification(
//                id = "3",
//                type = NotificationType.ALERT,
//                title = "Price drop alert",
//                description = "A item in your wishlist is now 20% off",
//                time = "3h ago"
//            )
//        )
//    }
//}

package com.muhammadahmedmufii.comfybuy

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Set up back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create sample data
        val notifications = createSampleNotifications()

        // Set up adapter
        adapter = NotificationsAdapter(this, notifications)
        recyclerView.adapter = adapter
    }

    private fun createSampleNotifications(): List<Notification> {
        return listOf(
            Notification(
                id = "1",
                type = NotificationType.MESSAGE,
                title = "New message from Sarah",
                description = "Is the backpack still available?",
                time = "2m ago"
            ),
            Notification(
                id = "2",
                type = NotificationType.OFFER,
                title = "New offer received",
                description = "Someone made an offer on your vintage camera",
                time = "1h ago"
            ),
            Notification(
                id = "3",
                type = NotificationType.ALERT,
                title = "Price drop alert",
                description = "A item in your wishlist is now 20% off",
                time = "3h ago"
            )
        )
    }
}