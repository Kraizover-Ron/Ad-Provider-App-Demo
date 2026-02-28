package com.example.engagementsdk

data class EngagementConfig(
    val baseUrl: String,
    val appKey: String,
    val windowMs: Long = 10_000L,
    val clicksPerWindow: Int = 20,
    val eligibleTtlSeconds: Int = 300
)
