//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bondagit.aes67player.Aes67PlayerApplication
import com.bondagit.aes67player.ui.screens.DaemonsLoadScreen
import com.bondagit.aes67player.ui.screens.SinksLoadScreen


enum class Aes67PlayerScreen {
    Daemons, Sinks,
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Aes67PlayerApp(navController: NavHostController = rememberNavController()) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val currentScreen = Aes67PlayerScreen.valueOf(
        backStackEntry?.destination?.route ?: Aes67PlayerScreen.Daemons.name
    )
    val app = LocalContext.current.applicationContext as Aes67PlayerApplication

    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        AppTopBar(scrollBehavior = scrollBehavior,
            currentScreen == Aes67PlayerScreen.Sinks,
            onBackClicked = { navController.navigate(Aes67PlayerScreen.Daemons.name) })
    }) { innerPadding ->
        NavHost(
            navController = navController, startDestination = Aes67PlayerScreen.Daemons.name
        ) {
            composable(route = Aes67PlayerScreen.Daemons.name) {
                DaemonsLoadScreen(
                    onDaemonSelected = {
                        app.container.sinksRepository.setBaseUrl(it)
                        navController.navigate(Aes67PlayerScreen.Sinks.name)
                    },
                    onReloadClicked = { navController.navigate(Aes67PlayerScreen.Daemons.name) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(route = Aes67PlayerScreen.Sinks.name) {
                SinksLoadScreen(
                    app.container.player, modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}