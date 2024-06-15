//
//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.data

import android.app.Application
import com.bondagit.aes67player.HttpPlayer
import com.bondagit.aes67player.Player
import com.bondagit.aes67player.network.DaemonApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val sinksRepository: SinksRepository
    val player: Player
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */
class DefaultAppContainer(app: Application) : AppContainer {
    private val baseUrl = "http://localhost:8080/api/"

    object RequestInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            println("Outgoing request to ${request.url}")

            val response = chain.proceed(request)
            //println("Incoming response $response")
            return response
        }
    }

    private val okHttpClient = OkHttpClient().newBuilder()
        //.addInterceptor(RequestInterceptor)
        .build()

    /**
     * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     */
    private val json = Json { ignoreUnknownKeys = true }
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl).client(okHttpClient).build()

    /**
     * Retrofit service object for creating api calls
     */
    private val retrofitService: DaemonApiService by lazy {
        retrofit.create(DaemonApiService::class.java)
    }

    /**
     * DI implementation for Sinks repository
     */
    override val sinksRepository: SinksRepository by lazy {
        NetworkSinksRepository(baseUrl, retrofitService)
    }

    /**
     * DI implementation for Player
     */
    override val player: Player by lazy {
        HttpPlayer(app)
    }

}

