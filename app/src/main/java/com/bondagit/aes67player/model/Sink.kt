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
"id": 0,
"name": "ALSA Sink 0",
"io": "Audio Device",
"use_sdp": true,
"source": "http://10.0.0.13:8080/api/source/sdp/0",
"sdp": "v=0\r\no=- 996538 997275 IN IP4 10.0.0.23\r\ns=AVIOUSB-503633 : 2\r\nc=IN IP4 239.69.126.220/32\r\nt=0 0\r\na=keywds:Dante\r\nm=audio 5004 RTP/AVP 97\r\ni=2 channels: Left, Right\r\na=recvonly\r\na=rtpmap:97 L24/48000/2\r\na=ptime:1\r\na=ts-refclk:ptp=IEEE1588-2008:00-1D-C1-FF-FE-50-36-33:0\r\na=mediaclk:direct=354295027\r\n",
"delay": 576,
"ignore_refclk_gmid": true,
"map": [ 0, 1 ]
*/

@Serializable
data class Sink(
    val id: Int,
    val name: String,
    val io: String,
    @SerialName(value = "use_sdp") val useSdp: Boolean,
    val source: String,
    val sdp: String,
    val delay: Int,
    @SerialName(value = "ignore_refclk_gmid") val ignoreRefclkGmid: Boolean,
    val map: IntArray
)