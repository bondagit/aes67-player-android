//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bondagit.aes67player.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    scrollBehavior: TopAppBarScrollBehavior, isSink: Boolean, onBackClicked: () -> Unit
) {
    CenterAlignedTopAppBar(scrollBehavior = scrollBehavior, title = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.aes67) + " ",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = if (isSink) stringResource(R.string.sinks) else stringResource(R.string.daemons),
                style = MaterialTheme.typography.headlineLarge,
            )
        }
    }, navigationIcon = {
        if (isSink) {
            IconButton(onClick = { onBackClicked() }, modifier = Modifier, content = {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,

                    contentDescription = "return to daemons screen",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.secondary
                )
            })
        }
    })
}
