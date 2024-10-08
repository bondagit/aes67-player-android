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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bondagit.aes67player.Aes67PlayerApplication
import com.bondagit.aes67player.data.DaemonsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * View model of DaemonsScreen
 */
class DaemonsViewModel(
    private val daemonsRepository: DaemonsRepository
) : ViewModel() {
    // UI states access for various [DessertReleaseUiState]
    val uiState: StateFlow<DaemonsUiState> = daemonsRepository.daemons.map { daemons ->
        DaemonsUiState(daemons.split(",").filterNot { it.isEmpty() })
    }.stateIn(
        scope = viewModelScope,
        // Flow is set to emits value for when app is on the foreground
        // 5 seconds stop delay is added to ensure it flows continuously
        // for cases such as configuration change
        started = SharingStarted.WhileSubscribed(5_000), initialValue = DaemonsUiState(emptyList())
    )

    fun saveDaemons(daemons: List<String>) {
        viewModelScope.launch {
            daemonsRepository.saveDaemons(daemons.joinToString(","))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Aes67PlayerApplication)
                DaemonsViewModel(application.daemonsRepository)
            }
        }
    }
}

/*
 * Data class containing various UI States for DaemonsScreen
 */
data class DaemonsUiState(
    val daemons: List<String>,
)