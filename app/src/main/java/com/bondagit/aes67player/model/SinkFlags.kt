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
{
  "sink_flags":
  {
    "rtp_seq_id_error": false,
    "rtp_ssrc_error": false,
    "rtp_payload_type_error": false,
    "rtp_sac_error": false,
    "receiving_rtp_packet": true,
    "some_muted": false,
    "all_muted": false,
    "muted": false
  },
  "sink_min_time": 0
}
*/

@Serializable
data class SinkFlags(
    @SerialName(value = "rtp_seq_id_error") val rtpSeqIdError: Boolean,
    @SerialName(value = "rtp_ssrc_error") val rtpSsrcError: Boolean,
    @SerialName(value = "rtp_payload_type_error") val rtpPayloadTypeError: Boolean,
    @SerialName(value = "rtp_sac_error") val rtpSacError: Boolean,
    @SerialName(value = "receiving_rtp_packet") val receivingRtpPacket: Boolean,
    @SerialName(value = "some_muted") val someMuted: Boolean,
    @SerialName(value = "all_muted") val allMuted: Boolean,
    @SerialName(value = "muted") val muted: Boolean
)
