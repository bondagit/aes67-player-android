//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
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
