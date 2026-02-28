package com.example.engagementsdk

import android.content.Context
import android.os.Build
import com.example.engagementsdk.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class EngagementSdk private constructor(
    private val appContext: Context,
    private val config: EngagementConfig
) {
    private val prefs = appContext.getSharedPreferences("engagement_sdk", Context.MODE_PRIVATE)
    private val api = ApiClient.create(config)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val pending = mutableListOf<EventDto>()
    private val clickTimes = ArrayDeque<Long>()
    private val totalClicks = AtomicInteger(0)

    private var sessionId: String? = null
    private var sessionStartMs: Long = 0L
    private var eligible: Boolean = false
    private var eligibleUntilMs: Long? = null
    private var cooldownUntilMs: Long? = null
    private var lastServerTimeMs: Long? = null
    /* IMPORTANT: */
    /* We never want to "drop" user clicks. We only throttle network calls. */
    private var lastEvaluateRequestAtMs: Long = 0L
    private var flushScheduled = false
    private var onEligibilityChanged: ((Boolean) -> Unit)? = null

    val userId: String by lazy {
        val existing = prefs.getString("user_id", null)
        if (existing != null) return@lazy existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString("user_id", created).apply()
        created
    }

    fun setOnEligibilityChanged(listener: ((Boolean) -> Unit)?) {
        onEligibilityChanged = listener
        listener?.invoke(eligible)
    }

    fun getEligibleUntilMs(): Long? = eligibleUntilMs

    fun getCooldownUntilMs(): Long? = cooldownUntilMs

    fun getLastServerTimeMs(): Long? = lastServerTimeMs

    fun startSession(displayName: String? = null, appVersion: String? = null) {
        sessionStartMs = System.currentTimeMillis()
        scope.launch {
            runCatching {
                val device = "${Build.MANUFACTURER} ${Build.MODEL}"
                val res = api.startSession(
                    SessionStartRequest(
                        userId = userId,
                        displayName = displayName,
                        device = device,
                        appVersion = appVersion
                    )
                )
                sessionId = res.sessionId
            }
        }
    }

    fun endSession() {
        val sid = sessionId ?: return
        val duration = System.currentTimeMillis() - sessionStartMs
        val clicks = totalClicks.get()
        scope.launch {
            runCatching {
                api.endSession(SessionEndRequest(sessionId = sid, durationMs = duration, clicks = clicks))
                flush(true)
            }
        }
    }

    fun trackScreen(screenName: String) {
        enqueue("screen_view", mapOf("screenName" to screenName))
        flushIfNeeded(System.currentTimeMillis())
    }

    fun trackClick() {
        totalClicks.incrementAndGet()
        val now = System.currentTimeMillis()

        clickTimes.addLast(now)
        pruneOld(now)
        enqueue("click", emptyMap())
        /* Send clicks in batches (avoid hammering backend) but still quickly. */
        flushIfNeeded(now)
        evaluateIfNeeded(now)
    }

    fun requestAd(onResult: (AdData?) -> Unit) {
        val uid = userId
        scope.launch {
            val result = runCatching {
                val resp = api.nextAd(uid)
                if (resp.code() == 204) return@runCatching null
                if (!resp.isSuccessful) return@runCatching null
                val body = resp.body() ?: return@runCatching null
                applyStatus(false, null, body.cooldownUntil, null, body.serverTime)
                AdData(
                    adId = body.adId,
                    title = body.title.orEmpty(),
                    imageUrl = body.imageUrl.orEmpty(),
                    clickUrl = body.clickUrl.orEmpty()
                )
            }.getOrNull()
            onResult(result)
        }
    }

    fun isEligible(): Boolean = eligible

    private fun evaluateIfNeeded(now: Long) {
        val sid = sessionId ?: return
        /* Throttle ONLY the server evaluation call. */
        if (now - lastEvaluateRequestAtMs < 900) return
        lastEvaluateRequestAtMs = now

        pruneOld(now)
        val clicks = clickTimes.size
        if (clicks < config.clicksPerWindow) return

        scope.launch {
            val status = runCatching {
                api.evaluate(
                    EngagementEvaluateRequest(
                        userId = userId,
                        sessionId = sid,
                        windowMs = config.windowMs,
                        thresholdClicks = config.clicksPerWindow
                    )
                )
            }.getOrNull() ?: return@launch

            applyStatus(status.eligible, status.eligibleUntil, status.cooldownUntil, status.reason, status.serverTime)
        }
    }

    private fun enqueue(type: String, meta: Map<String, Any>) {
        val ts = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US)
            .format(java.util.Date())
        pending.add(EventDto(type = type, ts = ts, meta = meta))
    }

    private fun flushIfNeeded(now: Long) {
        /* If we have many events, flush immediately. */
        if (pending.size >= 12) {
            flush(false)
            return
        }

        /* Otherwise, flush at most once per ~1s. */
        if (flushScheduled) return
        flushScheduled = true
        scope.launch {
            kotlinx.coroutines.delay(900)
            flushScheduled = false
            flush(false)
        }
    }

    private fun flush(force: Boolean) {
        val sid = sessionId ?: return
        if (!force && pending.isEmpty()) return

        val batch = pending.toList()
        pending.clear()

        scope.launch {
            val resp = runCatching {
                api.batchEvents(EventsBatchRequest(sessionId = sid, events = batch))
            }.getOrNull()

            if (resp != null) {
                applyStatus(resp.eligible, resp.eligibleUntil, resp.cooldownUntil, resp.reason, resp.serverTime)
            } else {
                pending.addAll(0, batch)
            }
        }
    }

    private fun applyStatus(newEligible: Boolean, eligibleUntil: Long?, cooldownUntil: Long?, reason: String?, serverTime: Long?) {
        eligibleUntilMs = eligibleUntil
        cooldownUntilMs = cooldownUntil
        if (serverTime != null) lastServerTimeMs = serverTime
        /* Notify on *any* status update so the UI can refresh (cooldown/TTL changes). */
        eligible = newEligible
        onEligibilityChanged?.invoke(eligible)
    }

    private fun pruneOld(now: Long) {
        val cutoff = now - config.windowMs
        while (clickTimes.isNotEmpty() && clickTimes.first() < cutoff) {
            clickTimes.removeFirst()
        }
    }

    data class AdData(
        val adId: String,
        val title: String,
        val imageUrl: String,
        val clickUrl: String
    )

    companion object {
        @Volatile private var INSTANCE: EngagementSdk? = null

        fun init(context: Context, config: EngagementConfig): EngagementSdk {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EngagementSdk(context.applicationContext, config).also { INSTANCE = it }
            }
        }

        fun get(): EngagementSdk {
            return INSTANCE ?: error("EngagementSdk is not initialized")
        }
    }
}
