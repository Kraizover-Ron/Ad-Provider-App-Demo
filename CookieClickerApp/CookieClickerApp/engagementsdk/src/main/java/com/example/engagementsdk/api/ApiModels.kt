package com.example.engagementsdk.api

data class SessionStartRequest(
    val userId: String,
    val displayName: String? = null,
    val device: String? = null,
    val appVersion: String? = null
)

data class SessionStartResponse(
    val sessionId: String,
    val startedAt: String
)

data class SessionEndResponse(
    val ok: Boolean
)

data class SessionEndRequest(
    val sessionId: String,
    val durationMs: Long,
    val clicks: Int
)

data class EventDto(
    val type: String,
    val ts: String,
    val meta: Map<String, Any> = emptyMap()
)

data class EventsBatchRequest(
    val sessionId: String,
    val events: List<EventDto>
)

data class EventsBatchResponse(
    val ok: Boolean,
    val received: Int,
    val clicksAdded: Int,
    val last10sClicks: Int,
    val eligible: Boolean,
    val eligibleUntil: Long?,
    val cooldownUntil: Long?,
    val reason: String?,
    val serverTime: Long
)

data class EngagementEvaluateRequest(
    val userId: String,
    val sessionId: String,
    val windowMs: Long,
    val thresholdClicks: Int
)

data class EngagementStatusResponse(
    val eligible: Boolean,
    val reason: String? = null,
    val eligibleUntil: Long? = null,
    val cooldownUntil: Long? = null,
    val score: Double = 0.0,
    val serverTime: Long? = null
)

data class AdResponse(
    val adId: String,
    val title: String?,
    val imageUrl: String?,
    val clickUrl: String?,
    val eligibleUntil: Long?,
    val cooldownUntil: Long?,
    val serverTime: Long?
)
