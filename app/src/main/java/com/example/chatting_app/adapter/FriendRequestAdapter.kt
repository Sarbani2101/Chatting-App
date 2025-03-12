package com.example.chatting_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatting_app.R
import com.example.chatting_app.dataclass.FriendRequest

class FriendRequestAdapter(
    private val context: Context,
    private val requestList: ArrayList<FriendRequest>,
    private val listener: OnRequestActionListener
) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    // Interface for handling user actions
    interface OnRequestActionListener {
        fun onAcceptRequest(senderId: String)
        fun onRejectRequest(senderId: String)
    }

    // ViewHolder class to hold views for each item
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileIcon)
        val username: TextView = view.findViewById(R.id.usernameTextView)
        val acceptButton: Button = view.findViewById(R.id.acceptButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)
        val txtFriend: TextView = view.findViewById(R.id.txtFriend)
        val txtNoFriend: TextView = view.findViewById(R.id.txtNoFriend)
    }

    // Inflating the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false)
        return ViewHolder(view)
    }

    // Binding data to the views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestList[position]

        // Set username
        holder.username.text = request.senderName

        // Load profile image using Glide
        Glide.with(context)
            .load(request.profileImage)
            .placeholder(R.drawable.ic_default_profile_image) // Placeholder while loading
            .error(R.drawable.ic_default_profile_image) // Error case
            .into(holder.profileImage)

        // Handle accept button click
        holder.acceptButton.setOnClickListener {
            listener.onAcceptRequest(request.senderId)

            // Remove item and notify adapter
            requestList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, requestList.size) // Fix potential index issues

            Toast.makeText(context, "Friend Request Accepted", Toast.LENGTH_SHORT).show()

            // Update UI state
            holder.acceptButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.txtFriend.visibility = View.VISIBLE
            holder.txtNoFriend.visibility = View.GONE
        }

        // Handle reject button click
        holder.rejectButton.setOnClickListener {
            listener.onRejectRequest(request.senderId)

            // Remove item and notify adapter
            requestList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, requestList.size) // Fix potential index issues

            Toast.makeText(context, "Friend Request Rejected", Toast.LENGTH_SHORT).show()

            // Update UI state
            holder.acceptButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.txtFriend.visibility = View.GONE
            holder.txtNoFriend.visibility = View.VISIBLE
        }

        // Reset visibility state for recycled views
        if (holder.txtFriend.visibility == View.VISIBLE) {
            holder.acceptButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.txtFriend.visibility = View.VISIBLE
            holder.txtNoFriend.visibility = View.GONE
        } else if (holder.txtNoFriend.visibility == View.VISIBLE) {
            holder.acceptButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.txtFriend.visibility = View.GONE
            holder.txtNoFriend.visibility = View.VISIBLE
        } else {
            holder.acceptButton.visibility = View.VISIBLE
            holder.rejectButton.visibility = View.VISIBLE
            holder.txtFriend.visibility = View.GONE
            holder.txtNoFriend.visibility = View.GONE
        }
    }

    // Return total item count
    override fun getItemCount(): Int = requestList.size
}
