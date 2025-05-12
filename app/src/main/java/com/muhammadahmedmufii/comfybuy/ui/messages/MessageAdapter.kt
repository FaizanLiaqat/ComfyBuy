package com.muhammadahmedmufii.comfybuy.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter // Import ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.MessageItem
import com.muhammadahmedmufii.comfybuy.R

// Use ListAdapter for better performance
class MessageAdapter(
    private val onItemClick: (MessageItem) -> Unit
) : ListAdapter<MessageItem, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false) // Ensure item_message.xml is correct
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePic: ImageView = itemView.findViewById(R.id.ivProfilePic)
        private val name: TextView = itemView.findViewById(R.id.tvName)
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val time: TextView = itemView.findViewById(R.id.tvTime)
        private val statusIndicator: ImageView = itemView.findViewById(R.id.ivStatus)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(message: MessageItem) {
            name.text = message.name
            messageText.text = message.message
            time.text = message.time
            profilePic.setImageResource(message.profilePicResId) // Use ResId for now
            statusIndicator.visibility = if (message.hasUnreadMessages) View.VISIBLE else View.GONE
        }
    }

    // DiffUtil callback for MessageItem
    class MessageDiffCallback : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.userId == newItem.userId // Assuming userId makes a chat unique
        }
        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem == newItem // Data class equals handles content
        }
    }
}