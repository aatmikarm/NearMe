// Create a new file: app/src/main/java/com/aatmik/nearme/ui/onboarding/OnboardingPagerAdapter.kt
package com.aatmik.nearme.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Total number of onboarding screens

    override fun createFragment(position: Int): Fragment {
        return OnboardingSlideFragment.newInstance(position)
    }
}
