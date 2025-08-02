package com.aatmik.nearme.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aatmik.nearme.databinding.FragmentFriendsBinding
import com.aatmik.nearme.ui.messages.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by viewModels()

    private lateinit var receivedRequestsAdapter: RequestAdapter
    private lateinit var sentRequestsAdapter: RequestAdapter
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAllData()
    }

    private fun setupAdapters() {
        // Received requests
        receivedRequestsAdapter = RequestAdapter(
            type = RequestAdapter.RequestType.RECEIVED_REQUESTS,
            onAcceptClicked = { match, _ -> viewModel.acceptRequest(match.id) },
            onRejectClicked = { match, _ -> viewModel.rejectRequest(match.id) }
        )
        binding.rvReceivedRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receivedRequestsAdapter
        }

        // Sent requests
        sentRequestsAdapter = RequestAdapter(
            type = RequestAdapter.RequestType.SENT_REQUESTS
        )
        binding.rvSentRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sentRequestsAdapter
        }

        // Friends
        friendsAdapter = FriendsAdapter { match, _ ->
            val intent = ChatActivity.createIntent(requireContext(), match.id)
            startActivity(intent)
        }
        binding.rvFinalMatches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.tvReceivedRequestsLabel.setOnClickListener {
            toggleSection(binding.rvReceivedRequests)
        }

        binding.tvSentRequestsLabel.setOnClickListener {
            toggleSection(binding.rvSentRequests)
        }

        binding.tvFinalMatchesLabel.setOnClickListener {
            toggleSection(binding.rvFinalMatches)
        }
    }

    private fun toggleSection(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        recyclerView.visibility = if (recyclerView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun observeViewModel() {
        viewModel.receivedRequests.observe(viewLifecycleOwner) { requests ->
            receivedRequestsAdapter.submitList(requests)

            val hasRequests = requests.isNotEmpty()
            binding.tvReceivedRequestsLabel.visibility = if (hasRequests) View.VISIBLE else View.GONE
            binding.rvReceivedRequests.visibility = if (hasRequests) View.VISIBLE else View.GONE
        }

        viewModel.sentRequests.observe(viewLifecycleOwner) { requests ->
            sentRequestsAdapter.submitList(requests)

            val hasRequests = requests.isNotEmpty()
            binding.tvSentRequestsLabel.visibility = if (hasRequests) View.VISIBLE else View.GONE
            binding.rvSentRequests.visibility = if (hasRequests) View.VISIBLE else View.GONE
        }

        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.submitList(friends)

            val hasFriends = friends.isNotEmpty()
            binding.tvFinalMatchesLabel.visibility = if (hasFriends) View.VISIBLE else View.GONE
            binding.rvFinalMatches.visibility = if (hasFriends) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}