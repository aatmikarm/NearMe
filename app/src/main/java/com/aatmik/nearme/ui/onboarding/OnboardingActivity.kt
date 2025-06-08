// Create file: app/src/main/java/com/aatmik/nearme/ui/onboarding/OnboardingActivity.kt
package com.aatmik.nearme.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.ActivityOnboardingBinding
import com.aatmik.nearme.ui.auth.AuthActivity
import com.aatmik.nearme.util.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    @Inject lateinit var preferenceManager: PreferenceManager

    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        adapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == adapter.itemCount - 1) {
                            binding.btnNext.text = getString(R.string.continue_text)
                        } else {
                            binding.btnNext.text = getString(R.string.next)
                        }
                    }
                }
        )
    }

    private fun setupButtons() {
        binding.btnSkip.setOnClickListener { finishOnboarding() }

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }
    }

    fun navigateToNextScreen() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < adapter.itemCount - 1) {
            binding.viewPager.currentItem = currentItem + 1
        } else {
            finishOnboarding()
        }
    }

    fun navigateToLogin() {
        finishOnboarding()
    }

    private fun finishOnboarding() {
        preferenceManager.setFirstTimeLaunchCompleted()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
