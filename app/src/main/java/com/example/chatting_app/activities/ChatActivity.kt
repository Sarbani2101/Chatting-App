package com.example.chatting_app.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatting_app.R
import com.example.chatting_app.adapter.ChatAdapter
import com.example.chatting_app.databinding.ActivityChatBinding
import com.example.chatting_app.dataclass.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var friendId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendId = intent.getStringExtra("friendId")
        val friendName = intent.getStringExtra("friendName")
        val profileImage = intent.getStringExtra("profileImage")

        binding.userName.text = friendName
        Glide.with(this)
            .load(profileImage)
            .error(R.drawable.ic_profile)
            .into(binding.profileImage)

        val senderId = FirebaseAuth.getInstance().currentUser?.uid

        // Setup chat rooms
        senderRoom = senderId + friendId
        receiverRoom = friendId + senderId

        dbRef = FirebaseDatabase.getInstance().getReference("Chats")

        // Setup RecyclerView
        messagesList = ArrayList()
        chatAdapter = ChatAdapter(this, messagesList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.setHasFixedSize(true)
        binding.chatRecyclerView.adapter = chatAdapter

        // Load messages from Firebase
        loadMessages()
        setupMessageInput()

        // Send message
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    private fun setupMessageInput() {
        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    binding.sendButton.visibility = View.GONE
                    binding.microImg.visibility = View.VISIBLE
//                    updateTypingStatus(false)
                } else {
                    binding.sendButton.visibility = View.VISIBLE
                    binding.microImg.visibility = View.GONE
//                    updateTypingStatus(true)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadMessages() {
        dbRef.child(senderRoom!!).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (data in snapshot.children) {
                    val message = data.getValue(Message::class.java)
                    if (message != null) {
                        messagesList.add(message)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messagesList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(messageText: String) {
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        val receiverId = friendId!!
        val messageId = dbRef.push().key!!

        val message = Message(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            senderName = FirebaseAuth.getInstance().currentUser!!.displayName ?: "Unknown",
            message = messageText,
            messageType = "text",
            mediaUrl = "",
            timestamp = System.currentTimeMillis(),
            status = "sent"
        )

        // Save message in sender's chat room
        dbRef.child(senderRoom!!).child(messageId).setValue(message).addOnCompleteListener {
            // Save message in receiver's chat room
            dbRef.child(receiverRoom!!).child(messageId).setValue(message)

            // Update last message and timestamp in the chats list for both sender and receiver
            updateChatList(senderId, receiverId, messageText)
            updateChatList(receiverId, senderId, messageText)
        }

        // Clear input field after sending
        binding.messageEditText.setText("")
    }

    private fun updateChatList(senderId: String, receiverId: String, lastMessage: String) {
        val timestamp = System.currentTimeMillis()
        val chatData = mapOf(
            "lastMessage" to lastMessage,
            "timestamp" to timestamp
        )

        // Update last message and timestamp for sender and receiver
        dbRef.child("chats").child(senderId).child(receiverId).updateChildren(chatData)
        dbRef.child("chats").child(receiverId).child(senderId).updateChildren(chatData)
    }


}
