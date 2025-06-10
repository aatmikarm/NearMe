package com.aatmik.nearme.ui.nearby

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ItemNearbyUserBinding
import com.aatmik.nearme.util.formatDistance
import com.aatmik.nearme.util.loadUserPhoto

class NearbyAdapter(
    private val onConnectClicked: (String) -> Unit,
    private val onSkipClicked: (String) -> Unit,
    private val onItemClicked: (NearbyUserModel) -> Unit
) : ListAdapter<NearbyUserModel, NearbyAdapter.NearbyUserViewHolder>(NearbyUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyUserViewHolder {
        val binding = ItemNearbyUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NearbyUserViewHolder(binding)
    }


    override fun onBindViewHolder(holder: NearbyUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NearbyUserViewHolder(private val binding: ItemNearbyUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }

            binding.btnLike.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onConnectClicked(getItem(position).userId)
                }
            }

            binding.btnSkip.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSkipClicked(getItem(position).userId)
                }
            }
        }


        fun bind(user: NearbyUserModel) {
            Log.d("PhotoDebug", "NearbyAdapter - User ID: ${user.userId}, Photo URL: ${user.photoUrl}")

            // Set user data
            binding.tvName.text = "${user.name}"
            binding.tvAge.text = "${user.age} Years Old"
            binding.tvDistance.text = formatDistance(user.distance)
            binding.tvBioPreview.text = user.bio

            // Load profile photo with our new method
            binding.ivUserPhoto.loadUserPhoto(user.photoUrl, user.userId)
        }
    }

    class NearbyUserDiffCallback : DiffUtil.ItemCallback<NearbyUserModel>() {
        override fun areItemsTheSame(oldItem: NearbyUserModel, newItem: NearbyUserModel): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: NearbyUserModel, newItem: NearbyUserModel): Boolean {
            return oldItem == newItem
        }
    }
}
