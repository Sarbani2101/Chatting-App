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

        // ✅ Remove previous listeners before adding new ones
        database.child("chats").child(currentUserId).removeEventListener(chatEventListener)

        database.child("chats").child(currentUserId)
            .orderByChild("timestamp")
            .addValueEventListener(chatEventListener)
    }

    private val chatEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val chatItems = mutableListOf<Chat>()

            snapshot.children.forEach { childSnapshot ->
                val userId = childSnapshot.key ?: return@forEach
                val lastMessage = childSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                val timestamp = childSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                val isRead = childSnapshot.child("isRead").getValue(Boolean::class.java) ?: false

                // ✅ Fetch user details once
                database.child("users").child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                            val profileImage = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""

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

                            // ✅ Sort by timestamp (latest first)
                            chatItems.sortByDescending { it.timestamp }

                            // ✅ Use postValue to update LiveData properly
                            _chatList.postValue(chatItems)
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }
}
