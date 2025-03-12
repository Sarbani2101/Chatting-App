import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatting_app.databinding.ChatListBinding
import com.example.chatting_app.dataclass.Chat

class ChatListAdapter(private val onChatClick: (Chat) -> Unit) :
    ListAdapter<Chat, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ChatListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            binding.chatUserName.text = chat.receiverName
            binding.chatMessagePreview.text = chat.lastMessage

            // ✅ Show unread count if not read
            if (!chat.isRead) {
                binding.unreadIndicator.visibility = View.VISIBLE
            } else {
                binding.unreadIndicator.visibility = View.GONE
            }

            // ✅ Load profile image using Glide
            Glide.with(binding.userProfileImage.context)
                .load(chat.profileImageUrl)
                .into(binding.userProfileImage)

            // ✅ Timestamp formatting
            binding.chattime.text = android.text.format.DateUtils.getRelativeTimeSpanString(
                chat.timestamp, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS
            )

            binding.root.setOnClickListener { onChatClick(chat) }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat) = oldItem.chatId == newItem.chatId
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat) = oldItem == newItem
    }
}
