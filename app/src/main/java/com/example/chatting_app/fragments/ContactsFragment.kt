package com.example.chatting_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatting_app.adapter.ContactsAdapter
import com.example.chatting_app.databinding.FragmentContactsBinding
import com.example.chatting_app.dataclass.Contact
import com.example.chatting_app.viewmodel.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi

class ContactsFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val contactList = mutableListOf<Contact>()
    private val acceptedFriendsList = mutableListOf<String>()

    private val unfriendReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.chatting_app.UNFRIEND_EVENT") {
                val unfriendedUserId = intent.getStringExtra("unfriendedUserId")
                if (unfriendedUserId != null) {
                    removeUnfriendedUser(unfriendedUserId)
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.contactRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        contactsAdapter = ContactsAdapter(contactList) { contact ->
            onContactClick(contact)
        }
        binding.contactRecyclerview.adapter = contactsAdapter

        requireContext().registerReceiver(
            unfriendReceiver,
            IntentFilter("com.example.chatting_app.UNFRIEND_EVENT"),
            Context.RECEIVER_EXPORTED
        )

        fetchAcceptedFriends()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(unfriendReceiver)
    }

    private fun fetchAcceptedFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendsRef = FirebaseDatabase.getInstance()
            .getReference("friends")
            .child(currentUserId)

        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                acceptedFriendsList.clear()
                for (childSnapshot in snapshot.children) {
                    val friendId = childSnapshot.key
                    if (friendId != null) {
                        acceptedFriendsList.add(friendId)
                    }
                }
                fetchContacts()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContactsFragment", "Error fetching accepted friends: ${error.message}")
            }
        })
    }

    private fun fetchContacts() {
        val currentUserId = auth.currentUser?.uid ?: return
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()
                for (childSnapshot in snapshot.children) {
                    val userId = childSnapshot.key
                    val name = childSnapshot.child("name").value as? String
                    val profileImage = childSnapshot.child("profileImage").value as? String

                    if (userId != null && name != null && userId != currentUserId &&
                        acceptedFriendsList.contains(userId)) {

                        val contact = Contact(userId, name, profileImage ?: "")
                        contactList.add(contact)
                    }
                }
                contactsAdapter.notifyDataSetChanged()

                sharedViewModel.setContacts(contactList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContactsFragment", "Error fetching contacts: ${error.message}")
            }
        })
    }

    private fun removeUnfriendedUser(unfriendedUserId: String) {
        val index = contactList.indexOfFirst { it.uid == unfriendedUserId }
        if (index != -1) {
            contactList.removeAt(index)
            contactsAdapter.notifyItemRemoved(index)

            sharedViewModel.setContacts(contactList)
        }
    }

    private fun onContactClick(contact: Contact) {
        Log.d("ContactsFragment", "Clicked on contact: ${contact.name}")

    }

}
