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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bondagit.aes67player.model.Sinks
import com.bondagit.aes67player.Aes67PlayerApplication
import com.bondagit.aes67player.Player
import com.bondagit.aes67player.data.SinksRepository
import com.bondagit.aes67player.model.Config
import com.bondagit.aes67player.model.SinkStatus
import com.bondagit.aes67player.model.StreamerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * UI state for the Sinks screen
 */
sealed interface SinksUiState {
    data class Success(
        val baseUrl: String,
        val config: Config,
        val sinks: Sinks,
        val sinksStatus: Map<Int, SinkStatus>,
        val streamerInfo: Map<Int, StreamerInfo>
    ) : SinksUiState

    data object Error : SinksUiState
    data object Loading : SinksUiState
}

/**
 * UI state for the Player used in Sinks screen
 */
data class PlayerUIState(
    val playingUrl: String = "",
    val playingSink: Int = -1,
    val isPlayStarted: Boolean = false,
    val isPlayError: Boolean = false,
    val playError: String = ""
)


class SinksViewModel(private val repository: SinksRepository, player: Player) : ViewModel() {
    /** The mutable State that stores the status of the Sinks */
    var sinksUiState: SinksUiState by mutableStateOf(SinksUiState.Loading)
        private set

    /** The mutable State that stores the status of the Player */
    private val _playerUiState = MutableStateFlow(PlayerUIState())
    val playerUiState: StateFlow<PlayerUIState> = _playerUiState.asStateFlow()

    fun playerReset() {
        _playerUiState.value = PlayerUIState("", -1,
            isPlayStarted = false,
            isPlayError = false,
            playError = ""
        )
    }

    private fun onPlayBuffering(url: String, id: Int) {
        _playerUiState.value = PlayerUIState(url, id,
            isPlayStarted = false,
            isPlayError = false,
            playError = ""
        )
    }

    private fun onPlayStarted(url: String, id: Int) {
        _playerUiState.value = PlayerUIState(url, id,
            isPlayStarted = true,
            isPlayError = false,
            playError = ""
        )
    }

    private fun onPlayEnd(url: String, id: Int, isError: Boolean, message: String) {
        _playerUiState.value = PlayerUIState(url, -1, false, isError, message)
    }

    /**
     * Call getSinks(), set player callbacks and update player UI state on init so we can display status immediately.
     */
    init {
        player.setCallbacks(::onPlayStarted, ::onPlayBuffering, ::onPlayEnd)
        val id = player.playingSink()
        val url = player.playingBaseUrl()
        _playerUiState.value = PlayerUIState(url, id, id >= 0, false, "")

        getSinks()
    }

    /**
     * Gets Sinks information from the Sinks API Retrofit service and updates the
     * [Sinks] [List] [MutableList].
     */
    fun getSinks() {
        viewModelScope.launch {
            sinksUiState = SinksUiState.Loading
            sinksUiState = try {
                val sinks = repository.getSinks()
                SinksUiState.Success(
                    repository.getBaseUrl(),
                    repository.getConfig(),
                    sinks,
                    repository.getSinksStatus(sinks),
                    repository.getStreamerInfo(sinks)
                )
            } catch (e: IOException) {
                SinksUiState.Error
            } catch (e: retrofit2.HttpException) {
                SinksUiState.Error
            } catch (e: Exception) {
                SinksUiState.Error
            }
        }
    }

    /**
     * Factory for [SinksViewModel] that takes [SinksRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Aes67PlayerApplication)
                val repository = application.container.sinksRepository
                val player = application.container.player
                SinksViewModel(repository, player)
            }
        }
    }
}
