package com.example.chatting_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatting_app.adapter.AddFriendAdapter
import com.example.chatting_app.databinding.FragmentFriendListBinding
import com.example.chatting_app.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendListFragment : Fragment(), AddFriendAdapter.OnFriendRequestClickListener {

    private lateinit var binding: FragmentFriendListBinding
    private lateinit var addFriendAdapter: AddFriendAdapter
    private val userList = mutableListOf<User>()
    private val acceptedFriends = mutableSetOf<String>()
    private val unfriendedUsers = mutableSetOf<String>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendListBinding.inflate(inflater, container, false)

        // Set up RecyclerView
        binding.addFriendRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        addFriendAdapter = AddFriendAdapter(requireContext(), userList, this)
        binding.addFriendRecyclerview.adapter = addFriendAdapter

        // Step 1: Fetch accepted and unfriended users
        fetchAcceptedAndUnfriendedUsers()

        return binding.root
    }

    private fun fetchAcceptedAndUnfriendedUsers() {
        if (currentUserId == null) return

        database.child("friends").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    acceptedFriends.clear()
                    unfriendedUsers.clear()

                    for (friendSnapshot in snapshot.children) {
                        val status = friendSnapshot.child("status").getValue(String::class.java)
                        when (status) {
                            "accepted" -> acceptedFriends.add(friendSnapshot.key!!) // Add to accepted friends list
                            "unfriended" -> unfriendedUsers.add(friendSnapshot.key!!) // Add to unfriended users list
                        }
                    }
                    fetchUsers()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FriendListFragment", "Failed to fetch friends: ${error.message}")
                }
            })
    }

    private fun fetchUsers() {
        if (currentUserId == null) return

        binding.friendProgress.visibility = View.VISIBLE

        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.uid != currentUserId) {

                        // ✅ Only add unfriended and new users (EXCLUDE accepted friends)
                        if (!acceptedFriends.contains(user.uid)) {
                            if (unfriendedUsers.contains(user.uid)) {
                                user.status = "Unfriended"
                            } else {
                                user.status = "Send Request"
                            }
                            userList.add(user) // Add user to the list
                        }
                    }
                }

                binding.friendProgress.visibility = View.GONE
                binding.noTxt.visibility = if (userList.isEmpty()) View.VISIBLE else View.GONE
                addFriendAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.friendProgress.visibility = View.GONE
                Log.e("FriendListFragment", "Error fetching users: ${error.message}")
            }
        })
    }

    override fun onSendFriendRequest(receiverId: String) {
        if (currentUserId == null) return

        // Step 3a: If user was unfriended, remove unfriended status first
        if (unfriendedUsers.contains(receiverId)) {
            database.child("friends").child(currentUserId).child(receiverId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d("FriendRequest", "Removed unfriended status for $receiverId")
                    unfriendedUsers.remove(receiverId)
                }
                .addOnFailureListener { e ->
                    Log.e("FriendRequest", "Failed to remove unfriended status: ${e.message}")
                }
        }

        // Step 3b: Send friend request
        val requestRef = database.child("friendRequests").child(receiverId).child(currentUserId)
        val request = mapOf(
            "senderId" to currentUserId,
            "status" to "pending"
        )

        requestRef.setValue(request)
            .addOnSuccessListener {
                Log.d("FriendRequest", "Friend request sent to $receiverId")
            }
            .addOnFailureListener { e ->
                Log.e("FriendRequest", "Failed to send request: ${e.message}")
            }
    }
}
