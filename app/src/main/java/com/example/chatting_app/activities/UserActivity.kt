package com.example.chatting_app.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.chatting_app.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid") // Friend's UID
        val name = intent.getStringExtra("name") // Friend's Name
        binding.userNameProfile.text = name
        binding.nameUser.text = name

        // Back Button
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Start Chat
        binding.userChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("friendId", uid)
            intent.putExtra("friendName", name)
            startActivity(intent)
        }

        // Unfriend Button
        binding.btnUnfriend.setOnClickListener {
            showUnfriendDialog(uid)

        }
    }

    // Show Confirmation Dialog for Unfriend
    private fun showUnfriendDialog(friendUid: String?) {
        if (friendUid == null) return

        AlertDialog.Builder(this)
            .setTitle("Unfriend")
            .setMessage("Are you sure you want to unfriend this user?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                unfriendUser(friendUid)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun unfriendUser(friendUid: String) {
        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            database.child("friends")
                .child(currentUserUid)
                .child(friendUid)
                .removeValue()
                .addOnSuccessListener {
                    // Remove current user from friend's friend list
                    database.child("friends")
                        .child(friendUid)
                        .child(currentUserUid)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Unfriended Successfully", Toast.LENGTH_SHORT).show()

                            val intent = Intent("com.example.chatting_app.UNFRIEND_EVENT").apply {
                                putExtra("unfriendedUserId", friendUid)
                            }
                            sendBroadcast(intent)

                            binding.btnUnfriend.visibility = View.GONE
                            binding.txtUnfriend.visibility = View.VISIBLE

                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to remove from friend's list", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to unfriend", Toast.LENGTH_SHORT).show()
                }
        }
    }


}
