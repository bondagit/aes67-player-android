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
