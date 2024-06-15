//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SinkStatus(
    @SerialName(value = "sink_flags") val sinkFlags: SinkFlags,
    @SerialName(value = "sink_min_time") val sinMinTime: Int
)
