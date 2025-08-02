package com.aatmik.nearme.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aatmik.nearme.databinding.ItemRequestBinding
import com.aatmik.nearme.model.FriendRequest
import com.aatmik.nearme.model.UserProfile
import com.aatmik.nearme.util.DateTimeUtil
import com.aatmik.nearme.util.loadUserPhoto

class RequestAdapter(
    private val type: RequestType,
    private val onAcceptClicked: ((FriendRequest, UserProfile) -> Unit)? = null,
    private val onRejectClicked: ((FriendRequest, UserProfile) -> Unit)? = null,
    private val onItemClicked: ((FriendRequest, UserProfile) -> Unit)? = null
) : ListAdapter<Pair<FriendRequest, UserProfile>, RequestAdapter.RequestViewHolder>(RequestDiffCallback()) {

    enum class RequestType {
        SENT_REQUESTS,      // Requests I sent to others
        RECEIVED_REQUESTS   // Requests others sent to me
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (request, userProfile) = getItem(position)
                    onItemClicked?.invoke(request, userProfile)
                }
            }

            binding.btnAccept.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (request, userProfile) = getItem(position)
                    onAcceptClicked?.invoke(request, userProfile)
                }
            }

            binding.btnReject.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (request, userProfile) = getItem(position)
                    onRejectClicked?.invoke(request, userProfile)
                }
            }
        }

        fun bind(item: Pair<FriendRequest, UserProfile>) {
            val (request, userProfile) = item

            // Set user name and age
            binding.tvName.text = "${userProfile.displayName}, ${userProfile.age}"

            // Set request time
            val requestTime = DateTimeUtil.formatMatchTime(request.requestedAt)
            binding.tvTime.text = requestTime

            // Load profile photo
            binding.ivProfilePhoto.loadUserPhoto(
                userProfile.photos.firstOrNull { it.isPrimary }?.url,
                userProfile.uid
            )

            // Show/hide buttons based on request type
            when (type) {
                RequestType.SENT_REQUESTS -> {
                    // For sent requests, hide accept/reject buttons
                    binding.btnAccept.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "Waiting for response..."
                }
                RequestType.RECEIVED_REQUESTS -> {
                    // For received requests, show accept/reject buttons
                    binding.btnAccept.visibility = View.VISIBLE
                    binding.btnReject.visibility = View.VISIBLE
                    binding.tvStatus.visibility = View.GONE
                }
            }
        }
    }

    class RequestDiffCallback : DiffUtil.ItemCallback<Pair<FriendRequest, UserProfile>>() {
        override fun areItemsTheSame(
            oldItem: Pair<FriendRequest, UserProfile>,
            newItem: Pair<FriendRequest, UserProfile>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<FriendRequest, UserProfile>,
            newItem: Pair<FriendRequest, UserProfile>
        ): Boolean {
            return oldItem == newItem
        }
    }
}