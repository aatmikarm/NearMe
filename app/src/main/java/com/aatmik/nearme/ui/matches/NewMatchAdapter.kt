// Create a new file: app/src/main/java/com/aatmik/nearme/ui/matches/NewMatchAdapter.kt
package com.aatmik.nearme.ui.matches

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ItemNewMatchBinding
import com.aatmik.nearme.model.Match
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.DateTimeUtil
import com.aatmik.nearme.util.loadUserPhoto

class NewMatchAdapter(
    private val onMatchClicked: (Match, UserProfile) -> Unit
) : ListAdapter<Pair<Match, UserProfile>, NewMatchAdapter.NewMatchViewHolder>(NewMatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMatchViewHolder {
        val binding = ItemNewMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewMatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewMatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NewMatchViewHolder(private val binding: ItemNewMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (match, userProfile) = getItem(position)
                    onMatchClicked(match, userProfile)
                }
            }
        }

        fun bind(item: Pair<Match, UserProfile>) {
            val (match, userProfile) = item

            // Set user name
            binding.tvName.text = userProfile.displayName

            // Set match time
            val matchTime = DateTimeUtil.formatMatchTime(match.matchedAt)
            binding.tvTime.text = matchTime

            // Load profile photo with our new method
            binding.ivProfilePhoto.loadUserPhoto(
                userProfile.photos.firstOrNull { it.isPrimary }?.url,
                userProfile.uid
            )
        }
    }

    class NewMatchDiffCallback : DiffUtil.ItemCallback<Pair<Match, UserProfile>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Match, UserProfile>,
            newItem: Pair<Match, UserProfile>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<Match, UserProfile>,
            newItem: Pair<Match, UserProfile>
        ): Boolean {
            return oldItem == newItem
        }
    }
}