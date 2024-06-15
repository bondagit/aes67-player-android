//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.model

import kotlinx.serialization.Serializable

@Serializable
data class Sinks(
    val sinks: Array<Sink>
)