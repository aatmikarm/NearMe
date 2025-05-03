package com.aatmik.nearme.ui.nearby

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ItemNearbyUserBinding
import com.aatmik.nearme.util.formatDistance

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

            binding.btnConnect.setOnClickListener {
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
            // Set user data
            binding.tvNameAge.text = "${user.name}, ${user.age}"
            binding.tvDistance.text = formatDistance(user.distance)

            // Load profile photo
            Glide.with(binding.ivUserPhoto.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivUserPhoto)
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
