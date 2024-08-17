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
package com.bondagit.aes67player.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DaemonsRepository(
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val DAEMONS_KEY = stringPreferencesKey("daemons")
    }

    val daemons: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e("DaemonsRepository", "Error reading daemons.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { daemons ->
            (daemons[DAEMONS_KEY] ?: "")
        }

    suspend fun saveDaemons(daemons: String) {
        dataStore.edit { preferences ->
            preferences[DAEMONS_KEY] = daemons
        }
    }
}