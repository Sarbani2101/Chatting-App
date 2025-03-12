package com.example.chatting_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatting_app.adapter.FriendRequestAdapter
import com.example.chatting_app.databinding.FragmentFriendRequestBinding
import com.example.chatting_app.dataclass.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendRequestFragment : Fragment(), FriendRequestAdapter.OnRequestActionListener {
    private lateinit var binding: FragmentFriendRequestBinding
    private lateinit var requestAdapter: FriendRequestAdapter
    private val requestList = ArrayList<FriendRequest>()
    private lateinit var database: DatabaseReference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        currentUserId?.let { getFriendRequests() }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.reqRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        requestAdapter = FriendRequestAdapter(requireContext(), requestList, this)
        binding.reqRecyclerview.adapter = requestAdapter
    }

    private fun getFriendRequests() {
        binding.friendProgress.visibility = View.VISIBLE
        binding.noTxt.visibility = View.VISIBLE
        val requestRef = database.child("friendRequests").child(currentUserId!!)
        requestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.key?.let { senderId ->
                        // Fetch username for each senderId
                        database.child("users").child(senderId).child("name").get().addOnSuccessListener { nameSnapshot ->
                            val senderName = nameSnapshot.value as? String ?: "Unknown"
                            requestList.add(FriendRequest(senderId, currentUserId, "pending", senderName))
                            requestAdapter.notifyDataSetChanged() // Notify adapter for each addition
                            binding.friendProgress.visibility = View.GONE
                            binding.noTxt.visibility = if (requestList.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
                binding.friendProgress.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                binding.friendProgress.visibility = View.GONE
                Log.e("FriendRequestFragment", "Error fetching friend requests: ${error.message}")
            }
        })
    }

    override fun onAcceptRequest(senderId: String) {
        if (currentUserId == null) return
        val friendRef = database.child("friends")
        friendRef.child(currentUserId).child(senderId).setValue(true)
        friendRef.child(senderId).child(currentUserId).setValue(true)
        database.child("friendRequests").child(currentUserId).child(senderId).removeValue()
    }

    override fun onRejectRequest(senderId: String) {
        currentUserId?.let {
            database.child("friendRequests").child(it).child(senderId).removeValue()
        }
    }
}