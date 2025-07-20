package com.example.chatapp.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// 1. Gönderilecek mesaj yapısı
data class Message(
    val role: String,
    val content: String
)

// 2. İstek yapısı
data class ChatRequest(
    val model: String,
    val messages: List<Message>
)

// 3. Cevap yapısı
data class ChatResponse(val choices: List<Choice>) {
    data class Choice(val message: Message)
}

// 4. Retrofit arayüzü
interface OpenRouterApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatResponse(@Body request: ChatRequest): ChatResponse
}
