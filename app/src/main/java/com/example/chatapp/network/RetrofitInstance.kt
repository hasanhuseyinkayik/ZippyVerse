package com.example.openrouterdeneme.network

import com.example.chatapp.network.OpenRouterApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://openrouter.ai/api/"

    fun getInstance(apiKey: String): OpenRouterApi {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://yourapp.com")
                .addHeader("X-Title", "FunFinder")
                .build()
            chain.proceed(request)
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(OpenRouterApi::class.java)
    }
}
