package com.aatmik.nearme.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aatmik.nearme.databinding.ItemNewMatchBinding
import com.aatmik.nearme.model.Friend
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.DateTimeUtil
import com.aatmik.nearme.util.loadUserPhoto

class FriendsAdapter(
    private val onFriendClicked: (Friend, UserProfile) -> Unit
) : ListAdapter<Pair<Friend, UserProfile>, FriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemNewMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FriendViewHolder(private val binding: ItemNewMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (friend, userProfile) = getItem(position)
                    onFriendClicked(friend, userProfile)
                }
            }
        }

        fun bind(item: Pair<Friend, UserProfile>) {
            val (friend, userProfile) = item

            binding.tvName.text = userProfile.displayName

            val friendsTime = DateTimeUtil.formatMatchTime(friend.friendsSince)
            binding.tvTime.text = friendsTime

            binding.ivProfilePhoto.loadUserPhoto(
                userProfile.photos.firstOrNull { it.isPrimary }?.url,
                userProfile.uid
            )
        }
    }

    class FriendDiffCallback : DiffUtil.ItemCallback<Pair<Friend, UserProfile>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Friend, UserProfile>,
            newItem: Pair<Friend, UserProfile>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<Friend, UserProfile>,
            newItem: Pair<Friend, UserProfile>
        ): Boolean {
            return oldItem == newItem
        }
    }
}