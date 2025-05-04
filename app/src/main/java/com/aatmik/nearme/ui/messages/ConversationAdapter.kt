// Create a new file: app/src/main/java/com/aatmik/nearme/ui/messages/ConversationAdapter.kt
package com.aatmik.nearme.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ItemConversationBinding
import com.aatmik.nearme.model.Conversation
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.DateTimeUtil
import com.aatmik.nearme.util.loadUserPhoto
import com.google.firebase.auth.FirebaseAuth

class ConversationAdapter(
    private val onConversationClicked: (Conversation, UserProfile) -> Unit
) : ListAdapter<Pair<Conversation, UserProfile>, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (conversation, userProfile) = getItem(position)
                    onConversationClicked(conversation, userProfile)
                }
            }
        }

        // Fix in ConversationAdapter.kt - in the bind method
        // Simplified ConversationAdapter.kt - in the bind method
        fun bind(item: Pair<Conversation, UserProfile>) {
            val (conversation, userProfile) = item

            // Set user name
            binding.tvName.text = userProfile.displayName

            // Get last message info from the match (now attached to the conversation)
            val lastMessage = conversation.lastMessage

            if (lastMessage != null) {
                binding.tvLastMessage.text = lastMessage.text
                binding.tvTime.text = DateTimeUtil.formatChatPreviewTime(lastMessage.sentAt)

                // Handle unread messages
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    val lastReadTime = conversation.lastReadBy[currentUserId] ?: 0
                    val lastMessageTime = lastMessage.sentAt

                    // Check if there are unread messages
                    val hasUnreadMessages = lastMessageTime > lastReadTime &&
                            lastMessage.senderId != currentUserId

                    if (hasUnreadMessages) {
                        binding.tvUnreadCount.visibility = View.VISIBLE
                        binding.tvUnreadCount.text = "1" // For simplicity, just show 1 indicator
                    } else {
                        binding.tvUnreadCount.visibility = View.GONE
                    }
                } else {
                    binding.tvUnreadCount.visibility = View.GONE
                }
            } else {
                // No last message available
                binding.tvLastMessage.text = "Start a conversation"
                binding.tvTime.text = DateTimeUtil.formatChatPreviewTime(conversation.createdAt)
                binding.tvUnreadCount.visibility = View.GONE
            }

            // Load profile photo
            binding.ivProfilePhoto.loadUserPhoto(
                userProfile.photos.firstOrNull { it.isPrimary }?.url,
                userProfile.uid
            )
        }
    }

    class ConversationDiffCallback : DiffUtil.ItemCallback<Pair<Conversation, UserProfile>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Conversation, UserProfile>,
            newItem: Pair<Conversation, UserProfile>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<Conversation, UserProfile>,
            newItem: Pair<Conversation, UserProfile>
        ): Boolean {
            return oldItem == newItem
        }
    }
}