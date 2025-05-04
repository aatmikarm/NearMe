// Update MatchesFragment.kt
package com.aatmik.nearme.ui.matches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aatmik.nearme.databinding.FragmentMatchesBinding
import com.aatmik.nearme.ui.messages.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchesFragment : Fragment() {

    private var _binding: FragmentMatchesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchesViewModel by viewModels()
    private lateinit var newMatchesAdapter: NewMatchAdapter
    private lateinit var allMatchesAdapter: AllMatchesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh matches when coming back to this fragment
        viewModel.loadMatches()
    }

    private fun setupRecyclerViews() {
        // Setup new matches adapter
        newMatchesAdapter = NewMatchAdapter { match, _ ->
            navigateToChat(match.id)
        }

        binding.rvNewMatches.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = newMatchesAdapter
        }

        // Setup all matches adapter
        allMatchesAdapter = AllMatchesAdapter { match, _ ->
            navigateToChat(match.id)
        }

        binding.rvAllMatches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allMatchesAdapter
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe new matches
        viewModel.newMatches.observe(viewLifecycleOwner) { matches ->
            newMatchesAdapter.submitList(matches)

            // Show/hide new matches section based on data
            val hasNewMatches = matches.isNotEmpty()
            binding.tvNewMatchesLabel.visibility = if (hasNewMatches) View.VISIBLE else View.GONE
            binding.rvNewMatches.visibility = if (hasNewMatches) View.VISIBLE else View.GONE
        }

        // Observe all matches
        viewModel.allMatches.observe(viewLifecycleOwner) { matches ->
            allMatchesAdapter.submitList(matches)

            // Show empty state if no matches
            updateEmptyState(matches.isEmpty())
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvAllMatchesLabel.visibility = View.GONE
            binding.rvAllMatches.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.tvAllMatchesLabel.visibility = View.VISIBLE
            binding.rvAllMatches.visibility = View.VISIBLE
        }
    }

    private fun navigateToChat(matchId: String) {
        val intent = ChatActivity.createIntent(requireContext(), matchId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}