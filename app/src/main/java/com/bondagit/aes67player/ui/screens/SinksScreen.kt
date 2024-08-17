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
package com.bondagit.aes67player.ui.screens

import android.app.Activity
import android.util.Log
import android.view.WindowManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bondagit.aes67player.Player
import com.bondagit.aes67player.R
import com.bondagit.aes67player.model.Config
import com.bondagit.aes67player.model.Sink
import com.bondagit.aes67player.model.SinkStatus
import com.bondagit.aes67player.model.StreamerInfo


@Composable
fun PlayerAlertDialog(
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(icon = {
        Icon(icon, contentDescription = "playback finished")
    }, title = {
        Text(text = dialogTitle)
    }, text = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_small))
        ) {
            Text(text = dialogText)
        }
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {}, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text(stringResource(R.string.dismiss))
        }
    })
}


@Composable
fun PlayStopButton(
    baseUrl: String,
    sink: Sink,
    streamerInfo: StreamerInfo,
    player: Player,
    playerUiState: PlayerUIState
) {
    val isPlaying: Boolean = playerUiState.playingSink != -1
    val isSinkPlaying: Boolean =
        playerUiState.playingUrl == baseUrl && playerUiState.playingSink == sink.id
    val isBuffering: Boolean = isSinkPlaying && !playerUiState.isPlayStarted
    val isPlayStarted: Boolean = isSinkPlaying && playerUiState.isPlayStarted

    IconButton(
        onClick = {
            if (!isPlaying) player.play(baseUrl, sink.id)
            else player.stop()
        },
        modifier = Modifier,
        enabled = streamerInfo.status == 0 && streamerInfo.channels <= 6 && (!isPlaying || isPlayStarted)
    ) {
        Icon(
            imageVector = if (streamerInfo.status != 0 || streamerInfo.channels > 6) Icons.Filled.PlayDisabled
            else if (!isPlaying) Icons.Filled.PlayCircle
            else if (!isSinkPlaying) Icons.Filled.PlayDisabled
            else if (isBuffering) Icons.Filled.Downloading
            else Icons.Filled.StopCircle,
            contentDescription = "playback control",
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun SinkName(
    sink: Sink, streamerInfo: StreamerInfo, modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = sink.name,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (streamerInfo.status != 0 || sink.map.size > 6) {
            Text(
                text = if (streamerInfo.status != 0) streamerInfo.statusText() else stringResource(R.string.unsupported_channels),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            val channels = if (sink.map.size > 1) stringResource(R.string.channels) else stringResource(R.string.channel)
            Text(
                text = sink.map.size.toString() + " " + channels,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SinkInfo(
    sink: Sink, sinkStatus: SinkStatus, modifier: Modifier = Modifier
) {
    val errors: MutableList<String> = mutableListOf()
    if (sinkStatus.sinkFlags.rtpSeqIdError) errors += "SEQID"
    if (sinkStatus.sinkFlags.rtpSsrcError) errors += "SSRC"
    if (sinkStatus.sinkFlags.rtpPayloadTypeError) errors += "PT"
    if (sinkStatus.sinkFlags.rtpSacError) errors += "SAC"

    val status: MutableList<String> = mutableListOf()
    if (sinkStatus.sinkFlags.receivingRtpPacket) status += "receiving"
    if (sinkStatus.sinkFlags.someMuted) status += "some muted"
    if (sinkStatus.sinkFlags.muted) status += "muted"

    Column(
        modifier = modifier
    ) {

        Text(buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.rtp_status) + " ")
            }
            if (status.isNotEmpty()) {
                append(status.joinToString(", "))
            } else {
                append(stringResource(R.string.idle))
            }
        })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.rtp_errors) + " ")
                }
                if (errors.isNotEmpty()) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(errors.joinToString(", "))
                    }
                } else {
                    append(stringResource(R.string.none))
                }
                status.joinToString(",")
            }, modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.channels_map) + " ")
                }
                append(sink.map.joinToString(","))
            }, modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.sdp))
                }
            }, modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = sink.sdp,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp)
        )
    }
}

@Composable
private fun SinkItemExpand(
    expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick, modifier = modifier
    ) {
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = "expand sink info",
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun SinkStatus(
    sink: Sink, sinkStatus: SinkStatus, playerUiState: PlayerUIState
) {
    Icon(
        imageVector = if (playerUiState.playingSink == sink.id) Icons.Filled.Headset
        else if (sinkStatus.sinkFlags.receivingRtpPacket) Icons.AutoMirrored.Filled.VolumeUp
        else Icons.AutoMirrored.Filled.VolumeMute,
        contentDescription = "playback status",
        tint = MaterialTheme.colorScheme.secondary
    )
}

@Composable
fun SinkItem(
    baseUrl: String,
    sink: Sink,
    sinkStatus: SinkStatus?,
    streamerInfo: StreamerInfo?,
    player: Player,
    playerUiState: PlayerUIState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            if (sinkStatus != null && streamerInfo != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small))
                ) {
                    SinkItemExpand(
                        expanded = expanded,
                        onClick = { expanded = !expanded },
                    )
                    SinkStatus(sink, sinkStatus, playerUiState)
                    Column(modifier.weight(12f)) {
                        SinkName(sink, streamerInfo, modifier)
                    }
                    Column(modifier.weight(3f), horizontalAlignment = Alignment.End) {
                        PlayStopButton(baseUrl, sink, streamerInfo, player, playerUiState)
                    }
                }
                if (expanded) {
                    SinkInfo(sink, sinkStatus, modifier)
                }
            }
        }
    }
}

@Composable
fun SinksReloadButton(
    getSinks: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = { getSinks() },
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "refresh sinks list",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SinksDaemonInfo(config: Config, sinks: Array<Sink>) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(300.dp)
    ) {
        Text(
            text = "${config.ipAddress} - ${config.nodeId}",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (sinks.isEmpty()) {
        Text(
            text = stringResource(R.string.no_sinks),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
        )
    }
}

@Composable
fun SinksList(
    baseUrl: String,
    config: Config,
    sinks: Array<Sink>,
    sinksStatus: Map<Int, SinkStatus>,
    streamerInfo: Map<Int, StreamerInfo>,
    getSinks: () -> Unit,
    player: Player,
    playerUiState: PlayerUIState
) {
    SinksDaemonInfo(config, sinks)
    SinksReloadButton(getSinks)
    LazyColumn {
        items(sinks.size) {
            SinkItem(
                baseUrl,
                sinks[it],
                sinksStatus[sinks[it].id],
                streamerInfo[sinks[it].id],
                player,
                playerUiState,
                Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = "sinks list is loading"
    )
}

@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = "error loading sinks"
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
fun SinksLoadScreen(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val model: SinksViewModel = viewModel(factory = SinksViewModel.Factory)
    val playerUiState by model.playerUiState.collectAsState()
    val activity = LocalView.current.context as? Activity
    var screenOn by remember { mutableStateOf(false) }

    if (activity != null && activity.window != null) {
        if (playerUiState.isPlayStarted) {
            if (!screenOn) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.i("SinksLoadScreen", "adding FLAG_KEEP_SCREEN_ON")
                screenOn = true
            }
        } else if (screenOn) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.i("SinksLoadScreen", "removing FLAG_KEEP_SCREEN_ON")
            screenOn = false
        }
    }

    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val sinksUiState = model.sinksUiState) {
            is SinksUiState.Error -> {
                ErrorScreen(model::getSinks, modifier = modifier.fillMaxSize())
            }

            is SinksUiState.Loading -> {
                LoadingScreen(modifier = modifier.fillMaxSize())
            }

            is SinksUiState.Success -> {
                SinksList(
                    sinksUiState.baseUrl,
                    sinksUiState.config,
                    sinksUiState.sinks.sinks,
                    sinksUiState.sinksStatus,
                    sinksUiState.streamerInfo,
                    model::getSinks,
                    player,
                    playerUiState
                )

                if (playerUiState.isPlayError) {
                    PlayerAlertDialog(
                        onDismissRequest = { model.playerReset(); model.getSinks(); },
                        dialogTitle = "Player",
                        dialogText = playerUiState.playError,
                        icon = Icons.Default.Error
                    )
                }
            }
        }
    }
}


