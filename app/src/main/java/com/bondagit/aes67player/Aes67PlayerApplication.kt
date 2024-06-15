//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bondagit.aes67player.data.AppContainer
import com.bondagit.aes67player.data.DaemonsRepository
import com.bondagit.aes67player.data.DefaultAppContainer

private const val DAEMONS_KEY = "daemons"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DAEMONS_KEY
)

class Aes67PlayerApplication : Application() {
    /** AppContainer instance used by the rest of classes to obtain dependencies */
    lateinit var container: AppContainer
    lateinit var daemonsRepository: DaemonsRepository
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        daemonsRepository = DaemonsRepository(dataStore)
    }
}
