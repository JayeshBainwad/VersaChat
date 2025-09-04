package com.jsb.versachat.data.api

import com.jsb.versachat.data.model.ChatRequest
import com.jsb.versachat.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GroqApi {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun createChatCompletion(@Body request: ChatRequest): ChatResponse
}