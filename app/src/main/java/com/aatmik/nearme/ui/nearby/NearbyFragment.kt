package com.aatmik.nearme.ui.nearby

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.aatmik.nearme.databinding.FragmentNearbyBinding
import com.aatmik.nearme.ui.matches.MatchConfirmationActivity
import com.aatmik.nearme.ui.nearby.filter.FilterBottomSheetFragment
import com.aatmik.nearme.ui.profile.UserProfileActivity
import com.aatmik.nearme.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NearbyFragment : Fragment() {

    private var _binding: FragmentNearbyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NearbyViewModel by viewModels()
    private lateinit var nearbyAdapter: NearbyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNearbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        nearbyAdapter = NearbyAdapter(
            onConnectClicked = { userId ->
                viewModel.connectWithUser(userId)
            },
            onSkipClicked = { userId ->
                viewModel.skipUser(userId)
            },
            onItemClicked = { user ->
                openUserProfile(user)
            }
        )

        // Use Grid Layout with 2 columns
        binding.rvNearbyUsers.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = nearbyAdapter

            // Add item spacing
            addItemDecoration(GridSpacingItemDecoration(2, 12, true))
        }
    }

    private fun setupListeners() {
        // Filter button
        binding.btnFilter.setOnClickListener {
            openFilterBottomSheet()
        }

        // Swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadNearbyUsers()
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && nearbyAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        // Observe nearby users
        viewModel.nearbyUsers.observe(viewLifecycleOwner) { users ->
            nearbyAdapter.submitList(users)
            updateEmptyState(users.isEmpty())
        }

        // Observe active proximity events
        viewModel.activeProximityEvents.observe(viewLifecycleOwner) { events ->
            // Handle proximity events if needed
            // For example, you could show a notification or open a match screen
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.root.showSnackbar(it)
            }
        }

        // Observe connect result
        viewModel.connectResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    val matchId = it.getOrNull()
                    if (matchId != null) {
                        // Show success message
                        binding.root.showSnackbar("Successfully connected!")

                        // Optionally navigate to match confirmation
                        val intent = MatchConfirmationActivity.createIntent(requireContext(), matchId)
                        startActivity(intent)
                    }
                } else {
                    binding.root.showSnackbar("Failed to connect: ${it.exceptionOrNull()?.message}")
                }
            }
        }

        // Observe skip result
        viewModel.skipResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.root.showSnackbar("User skipped")
            }
        }

        // Observe matchToShow
        viewModel.matchToShow.observe(viewLifecycleOwner) { match ->
            match?.let {
                // Navigate to match confirmation
                val intent = MatchConfirmationActivity.createIntent(requireContext(), it.id)
                startActivity(intent)

                // Clear the notification so it doesn't show again
                viewModel.clearMatchNotification()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the fragment
        viewModel.loadNearbyUsers()
        viewModel.loadActiveProximityEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset the session flag when fragment is destroyed
        viewModel.onFragmentDestroyed()
        _binding = null
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvNearbyUsers.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openUserProfile(user: NearbyUserModel) {
        val intent = UserProfileActivity.createIntent(requireContext(), user.userId)
        startActivity(intent)
    }

    private fun openFilterBottomSheet() {
        val filterBottomSheet = FilterBottomSheetFragment()
        filterBottomSheet.show(childFragmentManager, FilterBottomSheetFragment.TAG)
    }
}