// Create a new file: app/src/main/java/com/aatmik/nearme/ui/matches/AllMatchesAdapter.kt
package com.aatmik.nearme.ui.matches

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ItemMatchBinding
import com.aatmik.nearme.model.Match
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.DateTimeUtil
import com.aatmik.nearme.util.loadUserPhoto

class AllMatchesAdapter(
    private val onMatchClicked: (Match, UserProfile) -> Unit
) : ListAdapter<Pair<Match, UserProfile>, AllMatchesAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) :
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
            val photoUrl = userProfile.photos.firstOrNull { it.isPrimary }?.url
            Log.d("PhotoDebug", "AllMatchesAdapter - User ID: ${userProfile.uid}, Photo URL: $photoUrl")
            Log.d("PhotoDebug", "AllMatchesAdapter - All photos: ${userProfile.photos}")


            // Set user name and age
            binding.tvName.text = "${userProfile.displayName}, ${userProfile.age}"

            // Set match time
            val matchTime = DateTimeUtil.formatMatchTime(match.matchedAt)
            binding.tvTime.text = matchTime

            // Load profile photo with our new method
            binding.ivProfilePhoto.loadUserPhoto(
                userProfile.photos.firstOrNull { it.isPrimary }?.url,
                userProfile.uid
            )

            // Show Instagram icon if shared
            val instagramShared = match.instagramShared[userProfile.uid] == true
            binding.ivInstagram.visibility = if (instagramShared && userProfile.instagramConnected)
                View.VISIBLE else View.GONE
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<Pair<Match, UserProfile>>() {
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