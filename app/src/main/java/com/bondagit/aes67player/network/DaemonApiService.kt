//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.network

import com.bondagit.aes67player.model.Config
import com.bondagit.aes67player.model.SinkStatus
import com.bondagit.aes67player.model.Sinks
import com.bondagit.aes67player.model.StreamerInfo
import com.bondagit.aes67player.model.Version
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * A public interface that exposes the daemon APi services used
 */
interface DaemonApiService {
    /**
     * Returns the [Sinks] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "sinks" endpoint will be requested with the GET
     * HTTP method
     */
    @GET
    suspend fun getSinks(@Url url: String): Sinks

    /**
     * Returns the [SinkStatus] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "sinks/status" endpoint will be requested with the GET
     * HTTP method
     */
    @GET
    suspend fun getSinkStatus(@Url url: String): SinkStatus

    /**
     * Returns the [StreamerInfo] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "streamer/info" endpoint will be requested with the GET
     * HTTP method
     */
    @GET
    suspend fun getStreamerInfo(@Url url: String): StreamerInfo

    /**
     * Returns a [Version] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "version" endpoint will be requested with the GET
     * HTTP method
     */
    @GET
    suspend fun getVersion(@Url url: String): Version

    /**
     * Returns a [Config] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "config" endpoint will be requested with the GET
     * HTTP method
     */
    @GET
    suspend fun getConfig(@Url url: String): Config
}
