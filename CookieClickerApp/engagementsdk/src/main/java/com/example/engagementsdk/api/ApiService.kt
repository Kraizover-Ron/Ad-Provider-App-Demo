package com.example.engagementsdk.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("v1/session/start")
    suspend fun startSession(@Body body: SessionStartRequest): SessionStartResponse

    @POST("v1/session/end")
    suspend fun endSession(@Body body: SessionEndRequest): SessionEndResponse

    @POST("v1/events/batch")
    suspend fun batchEvents(@Body body: EventsBatchRequest): EventsBatchResponse

    @GET("v1/engagement/status")
    suspend fun getStatus(@Query("userId") userId: String): EngagementStatusResponse

    @POST("v1/engagement/evaluate")
    suspend fun evaluate(@Body body: EngagementEvaluateRequest): EngagementStatusResponse

    @GET("v1/ads/next")
    suspend fun nextAd(@Query("userId") userId: String): Response<AdResponse>
}
