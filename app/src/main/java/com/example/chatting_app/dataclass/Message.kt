package com.example.chatting_app.dataclass

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    var senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val receiverId : String = "",
    val messageType: String = "text", // "text", "image", "video", "audio"
    val mediaUrl: String = "",  // URL for media (image, video, audio)
    val status: String = "sent"  // "sent", "delivered", "read"
)



