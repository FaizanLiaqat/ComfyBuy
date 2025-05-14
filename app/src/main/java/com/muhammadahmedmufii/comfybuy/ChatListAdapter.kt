// ChatListAdapter.kt
package com.muhammadahmedmufii.comfybuy

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class ChatListAdapter(
    private val items: List<ChatListItem>,
    private val onClick: (ChatListItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatVH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        holder.bind(items[position])
    }

    inner class ChatVH(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile: CircleImageView = view.findViewById(R.id.ivProfilePic)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val ivStatus: View = view.findViewById(R.id.ivStatus)

        fun bind(item: ChatListItem) {
            tvName.text = item.fullName
            tvMessage.visibility = View.VISIBLE
            tvTime.visibility = View.VISIBLE
            ivStatus.visibility = View.VISIBLE

            item.profileImageBase64?.let { b64 ->
                try {
                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ivProfile.setImageBitmap(bmp)
                } catch (e: Exception) {
                    ivProfile.setImageResource(R.drawable.avatar_placeholder)
                }
            } ?: ivProfile.setImageResource(R.drawable.avatar_placeholder)

            itemView.setOnClickListener { onClick(item) }
        }
    }
}
