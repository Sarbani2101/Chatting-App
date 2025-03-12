package com.example.chatting_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatting_app.R
import com.example.chatting_app.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddFriendAdapter(
    private val context: Context,
    private val userList: List<User>,
    private val listener: OnFriendRequestClickListener
) : RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder>() {

    interface OnFriendRequestClickListener {
        fun onSendFriendRequest(receiverId: String)
    }

    class AddFriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userNameProfile)
        val btnAdd: Button = view.findViewById(R.id.btnAdd)
        val btnUndo: Button = view.findViewById(R.id.btnUndo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_add_friend, parent, false)
        return AddFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddFriendViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.name

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val targetUserId = user.uid ?: return

        checkFriendStatus(currentUserId, targetUserId, holder)

        holder.btnAdd.setOnClickListener {
            sendFriendRequest(currentUserId, targetUserId, holder)
        }

        holder.btnUndo.setOnClickListener {
            cancelFriendRequest(currentUserId, targetUserId, holder)
        }
    }

    private fun sendFriendRequest(currentUserId: String, targetUserId: String, holder: AddFriendViewHolder) {
        val dbRef = FirebaseDatabase.getInstance().getReference("friendRequests")
        dbRef.child(targetUserId).child(currentUserId).setValue("pending")
            .addOnSuccessListener {
                Toast.makeText(context, "Friend Request Sent", Toast.LENGTH_SHORT).show()
                holder.btnAdd.visibility = View.GONE
                holder.btnUndo.visibility = View.VISIBLE
            }
    }

    private fun cancelFriendRequest(currentUserId: String, targetUserId: String, holder: AddFriendViewHolder) {
        val dbRef = FirebaseDatabase.getInstance().getReference("friendRequests")
        dbRef.child(targetUserId).child(currentUserId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Friend Request Canceled", Toast.LENGTH_SHORT).show()
                holder.btnAdd.visibility = View.VISIBLE
                holder.btnUndo.visibility = View.GONE
            }
    }

    private fun checkFriendStatus(currentUserId: String, targetUserId: String, holder: AddFriendViewHolder) {
        val friendsRef = FirebaseDatabase.getInstance().getReference("friends")
            .child(currentUserId).child(targetUserId)

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    holder.btnAdd.visibility = View.GONE
                    holder.btnUndo.visibility = View.GONE
                } else {
                    checkRequestStatus(currentUserId, targetUserId, holder)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkRequestStatus(currentUserId: String, targetUserId: String, holder: AddFriendViewHolder) {
        val requestRef = FirebaseDatabase.getInstance().getReference("friendRequests")
            .child(targetUserId).child(currentUserId)

        requestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    holder.btnAdd.visibility = View.GONE
                    holder.btnUndo.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int = userList.size
}
