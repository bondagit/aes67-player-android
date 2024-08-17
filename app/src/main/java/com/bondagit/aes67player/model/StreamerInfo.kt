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
package com.bondagit.aes67player.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
"status": 0,
"file_duration": 2,
"files_num": 4,
"start_file_id": 3,
"current_file_id": 3,
"player_buffer_files_num": 1,
"channels": 2,
"rate": 48000
"format": "s16"
 */

@Serializable
data class StreamerInfo(
    val status: Int,
    @SerialName(value = "file_duration") val fileDuration: Int,
    @SerialName(value = "files_num") val filesNum: Int,
    @SerialName(value = "start_file_id") val startFileId: Int,
    @SerialName(value = "current_file_id") val currentFileId: Int,
    @SerialName(value = "player_buffer_files_num") val playerBufferFilesMum: Int,
    val channels: Int,
    val rate: Int,
    val format: String
) {

    fun statusText(): String {
        val str: String = when (status) {
            0 -> "OK"
            1 -> "PTP clock not locked"
            2 -> "Channel/s not captured"
            3 -> "Buffering"
            4 -> "Streamer not enabled"
            5 -> "Invalid Sink"
            6 -> "Cannot retrieve Sink"
            else -> "Internal error"
        }
        return str
    }
}