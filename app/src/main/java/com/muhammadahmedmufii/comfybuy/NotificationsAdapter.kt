package com.muhammadahmedmufii.comfybuy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NotificationsAdapter(
    private val context: Context,
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconContainer)
        private val icon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val description: TextView = itemView.findViewById(R.id.tvDescription)
        private val time: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(notification: Notification) {
            title.text = notification.title
            description.text = notification.description
            time.text = notification.time

            when (notification.type) {
                NotificationType.MESSAGE -> {
                    icon.setImageResource(R.drawable.ic_message)
                    iconContainer.background.setTint(
                        ContextCompat.getColor(context, R.color.icon_background_message)
                    )
                }
                NotificationType.OFFER -> {
                    icon.setImageResource(R.drawable.ic_heart)
                    iconContainer.background.setTint(
                        ContextCompat.getColor(context, R.color.icon_background_offer)
                    )
                }
                NotificationType.ALERT -> {
                    icon.setImageResource(R.drawable.ic_notifications)
                    iconContainer.background.setTint(
                        ContextCompat.getColor(context, R.color.icon_background_alert)
                    )
                }
            }
        }
    }
}