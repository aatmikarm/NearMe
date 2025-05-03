package com.aatmik.nearme.ui.nearby.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.FragmentFilterBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilterViewModel by viewModels()

    companion object {
        const val TAG = "FilterBottomSheetFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSeekBars()
        setupCheckboxes()
        setupButtons()
        loadCurrentPreferences()
    }

    private fun setupSeekBars() {
        // Distance seek bar
        binding.seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateDistanceLabel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })

        // Age range seek bar
        binding.rangeSliderAge.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val minAge = values[0].toInt()
            val maxAge = values[1].toInt()
            updateAgeRangeLabel(minAge, maxAge)
        }
    }

    private fun setupCheckboxes() {
        // Gender preference checkboxes
        binding.cbMale.setOnCheckedChangeListener { _, _ ->
            updateApplyButtonState()
        }

        binding.cbFemale.setOnCheckedChangeListener { _, _ ->
            updateApplyButtonState()
        }

        binding.cbNonBinary.setOnCheckedChangeListener { _, _ ->
            updateApplyButtonState()
        }
    }

    private fun setupButtons() {
        // Reset button
        binding.btnReset.setOnClickListener {
            resetToDefaults()
        }

        // Apply button
        binding.btnApply.setOnClickListener {
            savePreferences()
            dismiss()
        }
    }

    private fun loadCurrentPreferences() {
        viewModel.loadPreferences()

        viewModel.preferences.observe(viewLifecycleOwner) { preferences ->
            // Set distance
            binding.seekBarDistance.progress = preferences.distanceProgress
            updateDistanceLabel(preferences.distanceProgress)

            // Set age range
            binding.rangeSliderAge.values = listOf(
                preferences.minAge.toFloat(),
                preferences.maxAge.toFloat()
            )
            updateAgeRangeLabel(preferences.minAge, preferences.maxAge)

            // Set gender preferences
            binding.cbMale.isChecked = preferences.genderPreferences.contains("male")
            binding.cbFemale.isChecked = preferences.genderPreferences.contains("female")
            binding.cbNonBinary.isChecked = preferences.genderPreferences.contains("non-binary")
        }
    }

    private fun updateDistanceLabel(progress: Int) {
        val distance = viewModel.progressToDistance(progress)
        binding.tvDistanceValue.text = if (distance < 1000) {
            "$distance meters"
        } else {
            String.format("%.1f km", distance / 1000.0)
        }
    }

    private fun updateAgeRangeLabel(minAge: Int, maxAge: Int) {
        binding.tvAgeRangeValue.text = "$minAge - $maxAge"
    }

    private fun updateApplyButtonState() {
        // Check if at least one gender is selected
        val hasGenderSelected = binding.cbMale.isChecked ||
                binding.cbFemale.isChecked ||
                binding.cbNonBinary.isChecked

        binding.btnApply.isEnabled = hasGenderSelected
    }

    private fun resetToDefaults() {
        viewModel.resetToDefaults()
    }

    private fun savePreferences() {
        // Get distance
        val distanceProgress = binding.seekBarDistance.progress

        // Get age range
        val values = binding.rangeSliderAge.values
        val minAge = values[0].toInt()
        val maxAge = values[1].toInt()

        // Get gender preferences
        val genderPreferences = mutableSetOf<String>()
        if (binding.cbMale.isChecked) genderPreferences.add("male")
        if (binding.cbFemale.isChecked) genderPreferences.add("female")
        if (binding.cbNonBinary.isChecked) genderPreferences.add("non-binary")

        // Save preferences
        viewModel.savePreferences(
            distanceProgress = distanceProgress,
            minAge = minAge,
            maxAge = maxAge,
            genderPreferences = genderPreferences
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
