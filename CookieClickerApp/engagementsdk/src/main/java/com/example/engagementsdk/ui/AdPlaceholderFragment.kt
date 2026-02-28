package com.example.engagementsdk.ui

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.engagementsdk.EngagementSdk
import com.example.engagementsdk.databinding.FragmentAdPlaceholderBinding

class AdPlaceholderFragment : Fragment() {
    private var _binding: FragmentAdPlaceholderBinding? = null
    private val binding get() = _binding!!

    private val sdk by lazy { EngagementSdk.get() }

    private var adRequested = false
    private var adVisible = false

    /* Cooldown countdown UI (ticks every second) */
    private var cooldownTimer: CountDownTimer? = null
    private var cooldownTargetMs: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.adClose.setOnClickListener {
            adVisible = false
            binding.adCreativeImage.visibility = View.GONE
            binding.adCreativeText.visibility = View.GONE
            binding.adClose.visibility = View.GONE
            renderState()
        }

        renderState()

        sdk.setOnEligibilityChanged {
            activity?.runOnUiThread {
                if (!sdk.isEligible()) adRequested = false
                if (!sdk.isEligible() && !adVisible) renderState()

                if (sdk.isEligible() && !adRequested && !adVisible) {
                    adRequested = true
                    binding.root.postDelayed({ fetchAd() }, 2000)
                }
            }
        }

        if (sdk.isEligible() && !adRequested && !adVisible) {
            adRequested = true
            binding.root.postDelayed({ fetchAd() }, 2000)
        }
    }

    private fun fetchAd() {
        binding.adFlag.visibility = View.VISIBLE
        binding.adStatus.text = "Fetching ad..."
        sdk.requestAd { ad ->
            activity?.runOnUiThread {
                if (ad == null) {
                    adRequested = false
                    renderState()
                } else {
                    adVisible = true
                    binding.adFlag.visibility = View.GONE
                    binding.adStatus.text = "Ad"

                    binding.adCreativeText.text = ad.title.ifBlank { "Sponsored" }
                    if (ad.imageUrl.isNotBlank()) {
                        Glide.with(binding.adCreativeImage)
                            .load(ad.imageUrl)
                            .into(binding.adCreativeImage)
                    }

                    binding.adCreativeImage.visibility = View.VISIBLE
                    binding.adCreativeText.visibility = View.VISIBLE
                    binding.adClose.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun renderState() {
        if (adVisible) return

        val now = System.currentTimeMillis()
        val cd = sdk.getCooldownUntilMs()
        val inCooldown = cd != null && now < cd
        if (inCooldown) {
            startCooldownCountdown(cd!!)
            binding.adFlag.visibility = View.GONE
            binding.adCreativeImage.visibility = View.GONE
            binding.adCreativeText.visibility = View.GONE
            binding.adClose.visibility = View.GONE
            return
        }

        stopCooldownCountdown()

        if (sdk.isEligible()) {
            binding.adStatus.text = "Eligible"
            binding.adFlag.visibility = View.VISIBLE
            binding.adCreativeImage.visibility = View.GONE
            binding.adCreativeText.visibility = View.GONE
            binding.adClose.visibility = View.GONE
        } else {
            binding.adStatus.text = "No ad yet"
            binding.adFlag.visibility = View.GONE
            binding.adCreativeImage.visibility = View.GONE
            binding.adCreativeText.visibility = View.GONE
            binding.adClose.visibility = View.GONE
        }
    }

    private fun startCooldownCountdown(targetMs: Long) {
        /* Avoid restarting timer every render() call if the target didn't change. */
        if (cooldownTargetMs == targetMs && cooldownTimer != null) {
            updateCooldownText((targetMs - System.currentTimeMillis()).coerceAtLeast(0))
            return
        }

        stopCooldownCountdown()
        cooldownTargetMs = targetMs

        val initialLeftMs = (targetMs - System.currentTimeMillis()).coerceAtLeast(0)
        updateCooldownText(initialLeftMs)

        cooldownTimer = object : CountDownTimer(initialLeftMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isAdded) return
                updateCooldownText(millisUntilFinished)
            }

            override fun onFinish() {
                cooldownTargetMs = null
                cooldownTimer = null
                if (!isAdded) return
                renderState()
            }
        }.start()
    }

    private fun updateCooldownText(msLeft: Long) {
        val totalSeconds = (msLeft / 1000L).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        binding.adStatus.text = "Cooldown: %02d:%02d".format(minutes, seconds)
    }

    private fun stopCooldownCountdown() {
        cooldownTimer?.cancel()
        cooldownTimer = null
        cooldownTargetMs = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sdk.setOnEligibilityChanged(null)
        stopCooldownCountdown()
        _binding = null
    }
}
