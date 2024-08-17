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
package com.bondagit.aes67player.data

import android.util.Log
import com.bondagit.aes67player.model.Config
import com.bondagit.aes67player.model.SinkStatus
import com.bondagit.aes67player.model.Sinks
import com.bondagit.aes67player.model.StreamerInfo
import com.bondagit.aes67player.model.Version
import com.bondagit.aes67player.network.DaemonApiService

/**
 * Repository that fetch from daemonApi.
 */
interface SinksRepository {
    suspend fun getSinks(): Sinks
    suspend fun getSinksStatus(sinks: Sinks): Map<Int, SinkStatus>
    suspend fun getStreamerInfo(sinks: Sinks): Map<Int, StreamerInfo>
    suspend fun getConfig(): Config
    suspend fun getVersion(): Version
    fun setBaseUrl(url: String)
    fun getBaseUrl(): String
}

/**
 * Network Implementation of Repository that fetch from daemonApi.
 */
class NetworkSinksRepository(
    private var baseUrl: String, private val apiService: DaemonApiService
) : SinksRepository {
    /** Fetches list of Sinks from apiService*/
    override suspend fun getSinks(): Sinks = apiService.getSinks("$baseUrl/api/sinks")
    override suspend fun getSinksStatus(sinks: Sinks): Map<Int, SinkStatus> {
        val sinksStatus: MutableMap<Int, SinkStatus> = HashMap()
        for (sink in sinks.sinks) {
            sinksStatus[sink.id] = apiService.getSinkStatus("$baseUrl/api/sink/status/${sink.id}")
        }
        return sinksStatus
    }

    override suspend fun getStreamerInfo(sinks: Sinks): Map<Int, StreamerInfo> {
        val streamerStatus: MutableMap<Int, StreamerInfo> = HashMap()
        for (sink in sinks.sinks) {
            streamerStatus[sink.id] =
                apiService.getStreamerInfo("$baseUrl/api/streamer/info/${sink.id}")
        }
        return streamerStatus
    }

    override suspend fun getVersion(): Version = apiService.getVersion("$baseUrl/api/version")
    override suspend fun getConfig(): Config = apiService.getConfig("$baseUrl/api/config")
    override fun setBaseUrl(url: String) {
        Log.i("NetworkRepository", "daemon URL set to $url")
        baseUrl = url
    }

    override fun getBaseUrl(): String {
        return baseUrl
    }
}