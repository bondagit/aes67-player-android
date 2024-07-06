//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.ui.screens

import android.util.Log
import android.webkit.URLUtil
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bondagit.aes67player.Aes67PlayerApplication
import com.bondagit.aes67player.R
import com.bondagit.aes67player.model.Config
import com.bondagit.aes67player.model.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL


@Composable
fun SelectButton(
    baseUrl: String, onDaemonSelected: (daemonUrl: String) -> Unit
) {
    IconButton(onClick = {
        onDaemonSelected(baseUrl)
    }, modifier = Modifier, enabled = true) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "select the daemon address",
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}


@Composable
fun DaemonName(
    baseUrl: String, config: Config?, error: String, modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (config != null && error.isEmpty()) {
            Text(
                text = config.nodeId,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_small))
                    .offset((-10).dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = baseUrl,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_small))
                    .offset((-10).dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = baseUrl,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_small))
                    .offset((-10).dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = error.ifEmpty { stringResource(R.string.connecting) },
                style = MaterialTheme.typography.bodyLarge,
                color = if (error.isEmpty()) Color.Gray else Color.Red,
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_small))
                    .offset((-10).dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun checkDaemonVersion(ver: String): Boolean {
    if (!ver.contains("bondagit-")) {
        return false
    }
    val v = ver.substring(9).split('.').map { it.toInt() }
    return !(v.size < 3 || v[0] <= 1)
}


@Composable
fun DaemonItem(
    baseUrl: String,
    onDaemonRemoved: (daemonUrl: String) -> Unit,
    onDaemonSelected: (daemonUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var config by remember { mutableStateOf<Config?>(null) }
    var error by remember { mutableStateOf("") }
    val app = LocalContext.current.applicationContext as Aes67PlayerApplication

    LaunchedEffect(baseUrl) {
        launch(Dispatchers.IO) {
            var tmpConfig: Config? = null
            var tmpError: String
            try {
                val withUnknownKeys = Json { ignoreUnknownKeys = true }
                val version =
                    withUnknownKeys.decodeFromString<Version>(URL("$baseUrl/api/version").readText())
                if (!checkDaemonVersion(version.version)) {
                    tmpError = app.getString(R.string.wrong_daemon_version)
                } else {
                    tmpConfig =
                        withUnknownKeys.decodeFromString<Config>(URL("$baseUrl/api/config").readText())
                    tmpError = if (!tmpConfig.streamerEnabled) {
                        app.getString(R.string.streamer_not_enabled)
                    } else if (tmpConfig.streamerChannels == 0) {
                        app.getString(R.string.no_streamer_channels)
                    } else {
                        ""
                    }
                }
            } catch (e: Exception) {
                //Log.i("LaunchedEffect", e.printStackTrace().toString())
                Log.e("DaemonItem", "LaunchedEffect: ${e.message}")
                tmpError = e.message.toString()
            }
            withContext(Dispatchers.Main) {
                error = tmpError
                config = tmpConfig
            }
        }
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small))
            ) {
                IconButton(
                    onClick = { onDaemonRemoved(baseUrl) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "daemon delete",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Column(modifier.weight(12f)) {
                    DaemonName(baseUrl, config, error, modifier)
                }
                if (config != null && error.isEmpty()) {
                    Column(modifier.weight(3f), horizontalAlignment = Alignment.End) {
                        SelectButton(baseUrl, onDaemonSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun DaemonAddReloadButtons(
    onAddClicked: () -> Unit,
    onReloadClicked: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = { onAddClicked() },
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "add a daemon",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        IconButton(
            onClick = { onReloadClicked() },
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "refresh daemons list",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun DaemonAdd(
    onDaemonAdded: (daemon: String) -> Unit
) {
    var daemonUrl by remember { mutableStateOf("http://") }
    var isValidUrl by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = daemonUrl,
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        onValueChange = {
            daemonUrl = it
            isValidUrl =
                URLUtil.isHttpUrl(daemonUrl) && !daemonUrl.contains(',') && !daemonUrl.contains(' ')
        },
        label = {
            Text(stringResource(R.string.enter_daemon_address))
        },
        isError = !isValidUrl,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Uri,
            imeAction = if (isValidUrl) ImeAction.Go else ImeAction.None
        ),
        keyboardActions = KeyboardActions(onGo = { onDaemonAdded(daemonUrl) })
    )

}

@Composable
fun DaemonsList(
    daemons: List<String>,
    onAddDaemon: (daemonUrl: String) -> Unit,
    onRemoveDaemon: (daemonUrl: String) -> Unit,
    onDaemonSelected: (daemonUrl: String) -> Unit,
    onReloadClicked: () -> Unit,
) {
    var addDaemon by remember { mutableStateOf(false) }

    if (!addDaemon) DaemonAddReloadButtons({ addDaemon = true }, onReloadClicked)
    else DaemonAdd {
        onAddDaemon(it)
        addDaemon = false
    }

    if (daemons.isEmpty()) {
        Text(
            text = stringResource(R.string.no_daemons),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
        )
    }
    LazyColumn {
        items(daemons.size) {
            DaemonItem(
                daemons[it],
                onRemoveDaemon,
                onDaemonSelected,
                Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}

@Composable
fun DaemonAlertDialog(
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(icon = {
        Icon(icon, contentDescription = "daemon delete confirm")
    }, title = {
        Text(text = dialogTitle)
    }, text = {
        Column {
            Text(text = dialogText, textAlign = TextAlign.Center)
        }
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmRequest()
        }) {
            Text(stringResource(R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text(stringResource(R.string.cancel))
        }
    })
}

@Composable
fun DaemonsLoadScreen(
    onDaemonSelected: (daemonUrl: String) -> Unit,
    onReloadClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val model: DaemonsViewModel = viewModel(factory = DaemonsViewModel.Factory)
    val daemonsUiState = model.uiState.collectAsState().value
    val daemonUrls = daemonsUiState.daemons.toMutableList()
    var daemonToDelete by remember { mutableStateOf("") }
    val app = LocalContext.current.applicationContext as Aes67PlayerApplication

    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DaemonsList(daemonUrls, {
            var url = it
            while (url.endsWith("/")) {
                url = url.substring(0, url.length - 1)
            }
            if (!daemonUrls.contains(url)) {
                daemonUrls += url
                model.saveDaemons(daemonUrls)
            }
        }, {
            daemonToDelete = it
        }, onDaemonSelected, onReloadClicked
        )
    }

    if (daemonToDelete.isNotEmpty()) {
        DaemonAlertDialog({
            daemonUrls.remove(daemonToDelete)
            model.saveDaemons(daemonUrls)
            if (app.container.player.playingBaseUrl() == daemonToDelete) app.container.player.stop()
            daemonToDelete = ""
        },
            {
                daemonToDelete = ""
            },
            stringResource(R.string.remove_daemon),
            "Remove $daemonToDelete ?",
            icon = Icons.Default.Delete
        )
    }
}


