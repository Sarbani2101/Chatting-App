package com.example.chatting_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatting_app.R
import com.example.chatting_app.databinding.StoryListBinding
import com.example.chatting_app.dataclass.Contact

class StoriesAdapter(
    private val onItemClick: (Contact) -> Unit
) : ListAdapter<Contact, StoriesAdapter.StoryViewHolder>(DiffCallback()) {

    inner class StoryViewHolder(private val binding: StoryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.storyTitle.text = contact.name

            // ✅ Load profile image using Glide
            Glide.with(binding.root.context)
                .load(contact.profileImage)
                .placeholder(R.drawable.ic_default_story_image)
                .into(binding.storyImage)

            // ✅ Handle item click
            binding.root.setOnClickListener {
                onItemClick(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = StoryListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}
