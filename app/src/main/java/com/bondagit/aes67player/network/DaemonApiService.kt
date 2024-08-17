//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
