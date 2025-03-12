package com.example.chatting_app.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chatting_app.MainActivity
import com.example.chatting_app.R
import com.example.chatting_app.dataclass.User
import com.example.chatting_app.fragments.EditFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var imgEdit: ImageView
    private lateinit var locationText: TextView
    private lateinit var nameText: TextView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var logOut: TextView
    private lateinit var imgMsg: ImageView

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users") // Ensure the path is correct

        // Bind views
        nameText = findViewById(R.id.name)
        nameTextView = findViewById(R.id.nameUser)
        emailTextView = findViewById(R.id.emailUser)
        profileImageView = findViewById(R.id.userProfile)
        backButton = findViewById(R.id.backBtn)
        locationText = findViewById(R.id.locAddress)
        progressBar = findViewById(R.id.profileProgress)
        imgEdit = findViewById(R.id.imgEdit)
        logOut = findViewById(R.id.txtLogOut)
        imgMsg = findViewById(R.id.imgMsg)

        // Load user profile from Firebase
        loadUserProfile()
        listenForNameChanges()

        // Open image picker on profile image click
        profileImageView.setOnClickListener { openImagePicker() }

        // Back button functionality
        backButton.setOnClickListener { finish() }

        imgEdit.setOnClickListener {
            val editFragment = EditFragment()
            findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .commit()
        }


        // Logout functionality
        logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, OnboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }


        // Navigate to MessageFragment (if needed)
        imgMsg.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Replace with your message activity
            intent.putExtra("openFragment", "MessageFragment")
            startActivity(intent)
        }

    }

    private fun listenForNameChanges() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val newName = snapshot.getValue(String::class.java)
                    if (!newName.isNullOrEmpty()) {
                        nameTextView.text = newName  // Update UI
                        nameText.text = newName      // If there's another TextView for name
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to update UI: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            val imageUri = data.data
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Upload the image as Base64 to Firebase
            uploadProfileImage(bitmap)
        }
    }

    private fun uploadProfileImage(bitmap: Bitmap) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            // Save the Base64 image string to Firebase
            database.child(userId).child("profileImage").setValue(base64Image)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile image updated.", Toast.LENGTH_SHORT).show()
                        Glide.with(this).load(bitmap).into(profileImageView) // Update UI
                    } else {
                        Toast.makeText(this, "Failed to update profile image.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        progressBar.visibility = View.GONE
                        if (user != null) {
                            nameTextView.text = user.name.ifEmpty { "N/A" }
                            emailTextView.text = user.email.ifEmpty { "N/A" }

                            nameText.text = user.name.ifEmpty { "N/A" }
                            locationText.text = user.address.ifEmpty { "N/A" }

                            // Load profile image from Base64
                            if (user.profileImage.isNotEmpty()) {
                                try {
                                    val decodedBytes = Base64.decode(user.profileImage, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    profileImageView.setImageBitmap(bitmap)
                                } catch (e: Exception) {
                                    profileImageView.setImageResource(R.drawable.ic_default_profile_image)
                                }
                            } else {
                                profileImageView.setImageResource(R.drawable.ic_default_profile_image)
                            }
                        } else {
                            Toast.makeText(this@ProfileActivity, "User data is null.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ProfileActivity, "User data not found.", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
        }
    }
}