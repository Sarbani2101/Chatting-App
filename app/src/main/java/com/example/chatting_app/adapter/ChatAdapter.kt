package com.example.chatting_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatting_app.R
import com.example.chatting_app.databinding.ItemMessageReceivedBinding
import com.example.chatting_app.databinding.ItemMessageSentBinding
import com.example.chatting_app.dataclass.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(
    private val context: Context,
    private val messagesList: ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            SentViewHolder(
                ItemMessageSentBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        } else {
            ReceiveViewHolder(
                ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]

        if (holder is SentViewHolder) {
            holder.binding.messageText.text = message.message
            holder.binding.txtTime.text = formatTime(message.timestamp)
        } else if (holder is ReceiveViewHolder) {
            (holder as ReceiveViewHolder).binding.messageText.text = message.message
            holder.binding.txtTime.text = formatTime(message.timestamp)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messagesList[position].senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int = messagesList.size

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    inner class SentViewHolder(val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceiveViewHolder(val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root)
}
