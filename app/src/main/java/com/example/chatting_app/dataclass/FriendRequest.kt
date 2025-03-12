package com.example.chatting_app.dataclass

data class FriendRequest(
    val senderId: String,
    val receiverId: String,
    val status: String,
    val senderName: String,
    val profileImage: String = ""
){
    constructor() : this ("", "", "", "")
}
