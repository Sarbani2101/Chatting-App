package com.example.chatting_app.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatting_app.dataclass.Contact
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.*

class StoriesViewModel : ViewModel() {

    private val database = Firebase.database.reference
    private val _storiesList = MutableLiveData<List<Contact>>()
    val storiesList: LiveData<List<Contact>> get() = _storiesList

    fun fetchStories(currentUserId: String, currentLat: Double, currentLon: Double) {
        database.child("friends").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val stories = mutableListOf<Contact>()
                    snapshot.children.forEach { child ->
                        val friendId = child.key ?: return@forEach

                        database.child("users").child(friendId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val friendName = dataSnapshot.child("name").value as? String ?: "Unknown"
                                    val profileImage = dataSnapshot.child("profileImage").value as? String ?: ""
                                    val friendLat = dataSnapshot.child("latitude").value as? Double
                                    val friendLon = dataSnapshot.child("longitude").value as? Double

                                    if (friendLat != null && friendLon != null) {
                                        val distance = calculateDistance(currentLat, currentLon, friendLat, friendLon)
                                        if (distance <= MAX_RADIUS_KM) {
                                            stories.add(Contact(friendId, friendName, profileImage))
                                        }
                                    }

                                    _storiesList.value = stories
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    companion object {
        private const val MAX_RADIUS_KM = 90.0

        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = sin(latDistance / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(lonDistance / 2).pow(2)
            return 6371.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
        }
    }
}
