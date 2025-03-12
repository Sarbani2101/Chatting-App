package com.example.chatting_app.dataclass

data class Chat(
    val senderId: String = "",
    val receiverUid: String = "",
    val senderName: String = "",
    var receiverName: String = "",
    var lastMessage: String = "",
    var timestamp: Long = 0L,
    val profileImageUrl: String = "",
    var isRead: Boolean = false,
    var isSent: Boolean = false, // Field to track received messages
    var chatId: String = "" // New field to store the chat ID
)
