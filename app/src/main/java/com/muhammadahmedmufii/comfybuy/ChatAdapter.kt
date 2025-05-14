package com.muhammadahmedmufii.comfybuy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatAdapter(private val items: List<ChatMessage>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val R_TEXT  = 0
        private const val S_TEXT  = 1
        private const val R_IMAGE = 2
        private const val S_IMAGE = 3
    }

    override fun getItemViewType(pos: Int) = when(items[pos]) {
        is ChatMessage.ReceivedText  -> R_TEXT
        is ChatMessage.SentText      -> S_TEXT
        is ChatMessage.ReceivedImage -> R_IMAGE
        is ChatMessage.SentImage     -> S_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R_TEXT, R_IMAGE -> {
                val v = inflater.inflate(R.layout.item_message_received, parent, false)
                ReceivedVH(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_message_sent, parent, false)
                SentVH(v)
            }
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val msg = items[position]) {
            is ChatMessage.ReceivedText -> (holder as ReceivedVH).bindText(msg)
            is ChatMessage.ReceivedImage-> (holder as ReceivedVH).bindImage(msg)
            is ChatMessage.SentText     -> (holder as SentVH).bindText(msg)
            is ChatMessage.SentImage    -> (holder as SentVH).bindImage(msg)
        }
    }

    inner class ReceivedVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvText   : TextView   = view.findViewById(R.id.tvReceivedText)
        private val ivImage  : ImageView  = view.findViewById(R.id.ivReceivedImage)
        private val tvTime   : TextView   = view.findViewById(R.id.tvReceivedTime)

        fun bindText(m: ChatMessage.ReceivedText) {
            ivImage.visibility = View.GONE
            tvText.visibility = View.VISIBLE
            tvText.text = m.text
            tvTime.text = m.time
        }
        fun bindImage(m: ChatMessage.ReceivedImage) {
            tvText.visibility = View.GONE
            ivImage.visibility = View.VISIBLE
            Glide.with(ivImage.context)
                .load(m.uri)
                .into(ivImage)
            tvTime.text = m.time
        }
    }

    inner class SentVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvText   : TextView   = view.findViewById(R.id.tvSentText)
        private val ivImage  : ImageView  = view.findViewById(R.id.ivSentImage)
        private val tvTime   : TextView   = view.findViewById(R.id.tvSentTime)

        fun bindText(m: ChatMessage.SentText) {
            ivImage.visibility = View.GONE
            tvText.visibility = View.VISIBLE
            tvText.text = m.text
            tvTime.text = m.time
        }
        fun bindImage(m: ChatMessage.SentImage) {
            tvText.visibility = View.GONE
            ivImage.visibility = View.VISIBLE
            Glide.with(ivImage.context)
                .load(m.uri)
                .into(ivImage)
            tvTime.text = m.time
        }
    }
}
