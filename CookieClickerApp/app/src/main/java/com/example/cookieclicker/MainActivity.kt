package com.example.cookieclicker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cookieclicker.databinding.ActivityMainBinding
import com.example.engagementsdk.EngagementConfig
import com.example.engagementsdk.EngagementSdk
import com.example.engagementsdk.ui.AdPlaceholderFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EngagementSdk.init(
            this,
            EngagementConfig(
                baseUrl = BuildConfig.SDK_BASE_URL,
                appKey = BuildConfig.SDK_APP_KEY
            )
        )

        if (supportFragmentManager.findFragmentById(R.id.adContainer) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.adContainer, AdPlaceholderFragment())
                .commit()
        }

        binding.cookieButton.setOnClickListener {
            score += 1
            binding.scoreText.text = "Score: $score"
            EngagementSdk.get().trackClick()
        }
    }

    override fun onStart() {
        super.onStart()
        EngagementSdk.get().startSession(displayName = "Player", appVersion = BuildConfig.VERSION_NAME)
        EngagementSdk.get().trackScreen("main")
    }

    override fun onStop() {
        EngagementSdk.get().endSession()
        super.onStop()
    }
}
