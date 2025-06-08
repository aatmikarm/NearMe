package com.aatmik.nearme.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aatmik.nearme.R

private const val ARG_POSITION = "position"

/** A fragment that represents a single onboarding slide. */
class OnboardingSlideFragment : Fragment() {
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { position = it.getInt(ARG_POSITION, 0) }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_slide, container, false)

        // Update title and description based on position
        updateContent(view)
        updateIndicatorDots(view)

        return view
    }

    private fun updateContent(view: View) {
        val titleTextView = view.findViewById<TextView>(R.id.title)
        val descriptionTextView = view.findViewById<TextView>(R.id.description)

        when (position) {
            0 -> {
                titleTextView.text = "Discover Meaningful\nConnections"
                descriptionTextView.text =
                        "Join Datify today and explore a world\nof meaningful connections. Swipe,\nMatch, and meet like-minded people."
            }
            1 -> {
                titleTextView.text = "Connect When You're\nNearby"
                descriptionTextView.text =
                        "Get notified when someone is within\n100m. Start conversations and make\nnew friends in your area."
            }
            2 -> {
                titleTextView.text = "Share Your Social\nProfile"
                descriptionTextView.text =
                        "Connect your Instagram for social\nverification. Build trust and create\nauthentic connections."
            }
        }
    }

    private fun updateIndicatorDots(view: View) {
        val dot1 = view.findViewById<View>(R.id.dot1)
        val dot2 = view.findViewById<View>(R.id.dot2)
        val dot3 = view.findViewById<View>(R.id.dot3)

        // Reset all dots to unselected state
        dot1.setBackgroundResource(R.drawable.indicator_unselected)
        dot2.setBackgroundResource(R.drawable.indicator_unselected)
        dot3.setBackgroundResource(R.drawable.indicator_unselected)

        // Set the current position dot to selected state (theme color)
        when (position) {
            0 -> {
                dot1.setBackgroundResource(R.drawable.indicator_selected)
                dot2.setBackgroundResource(R.drawable.indicator_unselected)
                dot3.setBackgroundResource(R.drawable.indicator_unselected)
            }
            1 -> {
                dot1.setBackgroundResource(R.drawable.indicator_unselected)
                dot2.setBackgroundResource(R.drawable.indicator_selected)
                dot3.setBackgroundResource(R.drawable.indicator_unselected)
            }
            2 -> {
                dot1.setBackgroundResource(R.drawable.indicator_unselected)
                dot2.setBackgroundResource(R.drawable.indicator_unselected)
                dot3.setBackgroundResource(R.drawable.indicator_selected)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int) =
                OnboardingSlideFragment().apply {
                    arguments = Bundle().apply { putInt(ARG_POSITION, position) }
                }
    }
}
