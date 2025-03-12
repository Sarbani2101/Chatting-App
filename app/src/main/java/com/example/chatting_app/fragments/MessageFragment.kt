package com.example.chatting_app.fragments

import ChatListAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatting_app.activities.ProfileActivity
import com.example.chatting_app.activities.ChatActivity
import com.example.chatting_app.adapter.StoriesAdapter
import com.example.chatting_app.databinding.FragmentMessageBinding
import com.example.chatting_app.dataclass.Chat
import com.example.chatting_app.dataclass.Contact
import com.example.chatting_app.mvvm.ChatViewModel
import com.example.chatting_app.mvvm.StoriesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val storiesViewModel: StoriesViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    // Adapters
    private lateinit var storiesAdapter: StoriesAdapter
    private lateinit var chatListAdapter: ChatListAdapter

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)

        setupRecyclerViews()
        setupClickListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentUserId != null) {
            // ✅ Fetch location from Firebase
            fetchUserLocation(currentUserId)
        }

        // ✅ Observe data from ViewModel
        observeData()
        observeChatList()
        chatViewModel.fetchChatList()
    }

    private fun fetchUserLocation(userId: String) {
        database = FirebaseDatabase.getInstance().getReference("users").child(userId)

        database.child("latitude").get().addOnSuccessListener { snapshot ->
            currentLat = snapshot.value?.toString()?.toDoubleOrNull() ?: 0.0
            checkIfDataIsReady()
        }

        database.child("longitude").get().addOnSuccessListener { snapshot ->
            currentLon = snapshot.value?.toString()?.toDoubleOrNull() ?: 0.0
            checkIfDataIsReady()
        }
    }

    private fun checkIfDataIsReady() {
        if (currentLat != 0.0 && currentLon != 0.0) {
            // ✅ Fetch stories after location is ready
            storiesViewModel.fetchStories(currentUserId!!, currentLat, currentLon)
        }
    }

    private fun setupRecyclerViews() {
        // ✅ Stories RecyclerView setup
        binding.storiesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        storiesAdapter = StoriesAdapter { contact ->
            openChat(contact)
        }

        binding.storiesRecyclerView.adapter = storiesAdapter


        binding.chatListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatListAdapter = ChatListAdapter { chat ->
            openChat(chat)
        }
        binding.chatListRecyclerView.adapter = chatListAdapter
    }

    private fun observeChatList() {
        chatViewModel.chatList.observe(viewLifecycleOwner) { chatList ->
            chatListAdapter.submitList(chatList)
        }
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra("friendId", chat.receiverUid)
            putExtra("friendName", chat.receiverName)
            putExtra("profileImageUrl", chat.profileImageUrl)
        }
        startActivity(intent)
    }

    private fun observeData() {
        storiesViewModel.storiesList.observe(viewLifecycleOwner) { stories ->
            storiesAdapter.submitList(stories) // Use submitList for ListAdapter
        }
    }

    private fun setupClickListeners() {
        binding.profileIcon.setOnClickListener {
            startActivity(Intent(context, ProfileActivity::class.java))
        }
    }

    private fun openChat(contact: Contact) {
        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra("friendId", contact.uid)
            putExtra("friendName", contact.name)
            putExtra("profileImageUrl", contact.profileImage) // Pass URL, not the actual image
        }
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
