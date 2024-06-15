//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.model

import kotlinx.serialization.Serializable

/*
{ "version": "bondagit-1.7.0" }
*/

@Serializable
data class Version(
    val version: String
)
