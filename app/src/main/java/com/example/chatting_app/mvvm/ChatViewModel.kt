package com.example.chatting_app.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatting_app.dataclass.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference
    private val _chatList = MutableLiveData<List<Chat>>()
    val chatList: LiveData<List<Chat>> get() = _chatList

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun fetchChatList() {
        if (currentUserId == null) return

        // Clear existing listeners
        database.child("chats").child(currentUserId).removeEventListener(chatEventListener)

        // Fetch chat data
        database.child("chats").child(currentUserId)
            .orderByChild("timestamp")
            .addValueEventListener(chatEventListener)
    }

    private val chatEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val chatItems = mutableListOf<Chat>()
            val userIds = snapshot.children.map { it.key }.filterNotNull()

            if (userIds.isEmpty()) {
                _chatList.postValue(chatItems)
                return
            }

            // Fetch users in a single call
            database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    userIds.forEach { userId ->
                        val lastMessage = snapshot.child(userId).child("lastMessage").getValue(String::class.java) ?: ""
                        val timestamp = snapshot.child(userId).child("timestamp").getValue(Long::class.java) ?: 0L
                        val isRead = snapshot.child(userId).child("isRead").getValue(Boolean::class.java) ?: false

                        val userInfo = userSnapshot.child(userId)
                        val userName = userInfo.child("name").getValue(String::class.java) ?: "Unknown"
                        val profileImage = userInfo.child("profileImage").getValue(String::class.java) ?: ""

                        val chat = Chat(
                            chatId = userId,
                            receiverUid = userId,
                            receiverName = userName,
                            lastMessage = lastMessage,
                            timestamp = timestamp,
                            profileImageUrl = profileImage,
                            isRead = isRead
                        )

                        chatItems.add(chat)
                    }

                    chatItems.sortByDescending { it.timestamp }
                    _chatList.postValue(chatItems)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    _chatList.postValue(emptyList())
                    fetchChatList()
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
            _chatList.postValue(emptyList())
            fetchChatList()
        }
    }
}
