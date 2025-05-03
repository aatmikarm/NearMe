// Create a new file: app/src/main/java/com/aatmik/nearme/ui/onboarding/OnboardingPagerAdapter.kt
package com.aatmik.nearme.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aatmik.nearme.R
import com.aatmik.nearme.databinding.OnboardingSlideBinding

class OnboardingPagerAdapter(private val context: Context) :
    RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder>() {

    private val slides = listOf(
        OnboardingSlide(
            R.drawable.ic_logo,
            context.getString(R.string.app_name),
            "Find people near you"
        ),
        OnboardingSlide(
            R.drawable.ic_nearby,
            "Connect when you're nearby",
            "Get notified when someone is within 100m"
        ),
        OnboardingSlide(
            R.drawable.ic_instagram,
            "Share Instagram",
            "Connect your Instagram for social verification"
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = OnboardingSlideBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val slide = slides[position]
        holder.bind(slide)
    }

    override fun getItemCount(): Int = slides.size

    inner class OnboardingViewHolder(private val binding: OnboardingSlideBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(slide: OnboardingSlide) {
            binding.ivSlideImage.setImageResource(slide.imageResId)
            binding.tvSlideTitle.text = slide.title
            binding.tvSlideDescription.text = slide.description
        }
    }

    data class OnboardingSlide(
        val imageResId: Int,
        val title: String,
        val description: String
    )
}