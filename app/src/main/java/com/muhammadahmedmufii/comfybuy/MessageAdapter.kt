package com.muhammadahmedmufii.comfybuy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val messages: List<MessageItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePic: ImageView = itemView.findViewById(R.id.ivProfilePic)
        private val name: TextView = itemView.findViewById(R.id.tvName)
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val time: TextView = itemView.findViewById(R.id.tvTime)
        private val statusIndicator: ImageView = itemView.findViewById(R.id.ivStatus)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }

        fun bind(message: MessageItem) {
            name.text = message.name
            messageText.text = message.message
            time.text = message.time
            profilePic.setImageResource(message.profilePic)

            // Show/hide unread message indicator
            statusIndicator.visibility = if (message.hasUnreadMessages) View.VISIBLE else View.GONE
        }
    }
}