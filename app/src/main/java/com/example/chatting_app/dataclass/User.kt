package com.example.chatting_app.dataclass

data class User(
    var name: String = "",
    val city: String = "",
    var status: String = "Available",
    val email: String = "",
    val uid: String = "",
    val latitude: Double = 0.0,  // Change to Double
    val longitude: Double = 0.0, // Change to Double
    val address: String = "",
    val profileImage: String = ""
){
    constructor() : this("", "", "", "", "", 0.0, 0.0, "", "")
}