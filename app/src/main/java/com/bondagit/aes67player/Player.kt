//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//

package com.bondagit.aes67player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.bondagit.aes67player.model.StreamerInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.net.URL


interface Player {
    fun play(baseUrl: String, sinkId: Int)
    fun stop()
    fun playingSink(): Int
    fun playingBaseUrl(): String
    fun setCallbacks(
        onPlayStarted: (url: String, sinkId: Int) -> Unit,
        onPlayBuffering: (url: String, sinkId: Int) -> Unit,
        onPlayEnd: (url: String, sinkId: Int, isError: Boolean, message: String) -> Unit
    )
}

class HttpPlayer(context: Context) : Player {
    private val _maxDurationSecs = 30
    private var _context = context
    private var _sampleSize: Int = 0
    private var _playingSink: Int = -1
    private var _playingBaseUrl: String = ""
    private lateinit var _playJob: Job
    private lateinit var _audioTrack: AudioTrack
    private var _onPlayStarted: (url: String, sinkId: Int) -> Unit = { _: String, _: Int -> }
    private var _onPlayBuffering: (url: String, sinkId: Int) -> Unit = { _: String, _: Int -> }
    private var _onPlayEnd: (url: String, sinkId: Int, isError: Boolean, message: String) -> Unit =
        { _: String, _: Int, _: Boolean, _: String -> }

    override fun setCallbacks(
        onPlayStarted: (url: String, sinkId: Int) -> Unit,
        onPlayBuffering: (url: String, sinkId: Int) -> Unit,
        onPlayEnd: (url: String, sinkId: Int, isError: Boolean, message: String) -> Unit
    ) {
        _onPlayStarted = onPlayStarted
        _onPlayBuffering = onPlayBuffering
        _onPlayEnd = onPlayEnd
    }

    override fun playingSink(): Int {
        return _playingSink
    }

    override fun playingBaseUrl(): String {
        return _playingBaseUrl
    }


    private fun getStreamerInfo(baseUrl: String, sinkId: Int): StreamerInfo? {
        val info: StreamerInfo
        try {
            val infoUrl = URL("$baseUrl/api/streamer/info/$sinkId")
            info = Json.decodeFromString<StreamerInfo>(infoUrl.readText())
            return info
        } catch (e: Exception) {
            _onPlayEnd(
                baseUrl, sinkId, true, _context.resources.getString(R.string.failed_to_fetch_info)
            )
        }
        return null
    }

    private fun openAudioTrack(baseUrl: String, sinkId: Int, info: StreamerInfo): Boolean {
        val audioFormat: Int
        when (info.format) {
            "s16" -> {
                audioFormat = AudioFormat.ENCODING_PCM_16BIT
                _sampleSize = 2
            }/*
            "s24" -> {
                audioFormat = AudioFormat.ENCODING_PCM_24BIT_PACKED
               _ sampleSize = 3
            }
            "s32" -> {
                audioFormat = AudioFormat.ENCODING_PCM_32BIT
                _sampleSize = 3
            }*/
            else -> {
                Log.e("player", "unsupported audio format $info.format")
                _onPlayEnd(
                    baseUrl,
                    sinkId,
                    true,
                    _context.resources.getString(R.string.unsupported_audio_format)
                )
                return false
            }
        }
        val channelsOut: Int = when (info.channels) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            3 -> AudioFormat.CHANNEL_OUT_STEREO or AudioFormat.CHANNEL_OUT_FRONT_CENTER
            4 -> AudioFormat.CHANNEL_OUT_QUAD
            5 -> AudioFormat.CHANNEL_OUT_QUAD or AudioFormat.CHANNEL_OUT_FRONT_CENTER
            6 -> AudioFormat.CHANNEL_OUT_5POINT1
            7 -> AudioFormat.CHANNEL_OUT_5POINT1 or AudioFormat.CHANNEL_OUT_BACK_CENTER
            8 -> AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
            else -> {
                Log.e("player", "unsupported channels count $info.channels")
                _onPlayEnd(
                    baseUrl,
                    sinkId,
                    true,
                    _context.resources.getString(R.string.unsupported_channels)
                )
                return false
            }
        }

        var bufferSize: Int = info.rate * _sampleSize * info.channels * info.fileDurationSec
        val minBufferSize: Int = AudioTrack.getMinBufferSize(
            info.rate, channelsOut, audioFormat
        )
        if (bufferSize < minBufferSize) bufferSize = minBufferSize
        Log.i("player", "min buffer size $minBufferSize buffer size $bufferSize")

        try {
            _audioTrack =
                AudioTrack.Builder().setTransferMode(AudioTrack.MODE_STREAM).setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                ).setAudioFormat(
                    AudioFormat.Builder().setEncoding(audioFormat).setSampleRate(info.rate)
                        .setChannelMask(channelsOut).build()
                )
                    // buffer of 2 files
                    .setBufferSizeInBytes(minBufferSize).build()
        } catch (e: Exception) {
            _onPlayEnd(
                baseUrl, sinkId, true, _context.resources.getString(R.string.audio_init_failed)
            )
        }

        return true
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun play(baseUrl: String, sinkId: Int) {

        if (_playingSink != -1) {
            Log.w("player", "Player already started on sink")
            return
        }
        _playingBaseUrl = baseUrl
        _onPlayBuffering(baseUrl, sinkId)

        _playJob = GlobalScope.launch(Dispatchers.IO) {

            val info: StreamerInfo = getStreamerInfo(baseUrl, sinkId) ?: return@launch
            if (info.status != 0) {
                _onPlayEnd(baseUrl, sinkId, true, info.statusText())
                return@launch
            }
            Log.i("player", "received streamer info $info")

            if (!openAudioTrack(baseUrl, sinkId, info)) return@launch
            _playingSink = sinkId

            val streamChannel = Channel<ByteArray>(
                info.playerBufferFilesMum
            )

            val receiverJob = GlobalScope.launch(Dispatchers.IO) {
                var pos = info.startFileId
                var receiverSuspended = false
                var receiverSlow = false
                var receiverException = false

                while (_playingSink != -1) {
                    try {
                        val timestamp: Long = System.currentTimeMillis()

                        val url = "$baseUrl/api/streamer/stream/$sinkId/${pos}"
                        Log.i("player", "receiverJob: fetching file $url")
                        val samples = URL(url).readBytes()
                        Log.i(
                            "player",
                            "receiverJob: fetch time ${System.currentTimeMillis() - timestamp}"
                        )

                        if ((System.currentTimeMillis() - timestamp) > info.fileDurationSec * info.filesNum * 1000) {
                            Log.i(
                                "player",
                                "receiverJob: slept for more than {$info.fileDurationSec*$info.filesNum} secs, stopping"
                            )
                            receiverSuspended = true
                            break
                        }

                        if ((System.currentTimeMillis() - timestamp) > info.playerBufferFilesMum * 2 * 1000) {
                            Log.e("player", "receiverJob: fetching time longer than player buffer")
                            receiverSlow = true
                            break
                        }

                        streamChannel.send(samples)
                        ++pos
                        pos %= info.filesNum
                    } catch (e: Exception) {
                        Log.e("player", "receiverJob: ${e.message}")
                        if (_playingSink != -1) receiverException = true
                        break;
                    }
                }
                if (receiverException) {
                    _onPlayEnd(
                        baseUrl,
                        sinkId,
                        true,
                        _context.resources.getString(R.string.audio_fetch_failed)
                    )
                } else if (receiverSlow) {
                    _onPlayEnd(
                        baseUrl,
                        sinkId,
                        true,
                        _context.resources.getString(R.string.network_too_slow)
                    )
                } else if (receiverSuspended) {
                    _onPlayEnd(
                        baseUrl,
                        sinkId,
                        false,
                        _context.resources.getString(R.string.player_suspended)
                    )
                }
                _playingSink = -1
                streamChannel.cancel()
                Log.i("player", "receiverJob: finished")
            }

            val playerJob = GlobalScope.launch(Dispatchers.IO) {
                try {
                    var totalFrames = 0
                    var count = 0

                    while (_playingSink != -1) {

                        val samples = streamChannel.receive()

                        if (count == 0) {
                            _onPlayStarted(baseUrl, sinkId)
                            _audioTrack.play()
                        }

                        val timestamp: Long = System.currentTimeMillis()
                        val headPosition: Int = _audioTrack.playbackHeadPosition
                        val diff = totalFrames - headPosition
                        Log.i(
                            "player",
                            "playerJob: count $count audio position $headPosition samples $totalFrames diff $diff"
                        )

                        if (headPosition / info.rate > _maxDurationSecs) {
                            Log.i(
                                "player", "playerJob: reached max playback duration"
                            )
                            break
                        }

                        totalFrames += samples.size / _sampleSize / info.channels

                        if (_audioTrack.write(samples, 0, samples.size) < 0) {
                            Log.e("player", "playerJob: write samples failed")
                            break
                        }

                        Log.i(
                            "player",
                            "playerJob: playback time ${System.currentTimeMillis() - timestamp}"
                        )

                        count++
                    }

                    if (_audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                        if (_audioTrack.playbackHeadPosition / info.rate >= _maxDurationSecs)
                            _onPlayEnd(
                                baseUrl,
                                sinkId,
                                true,
                                _context.resources.getString(R.string.reached_max_samples)
                            )
                    } else {
                        _onPlayEnd(
                            baseUrl,
                            sinkId,
                            false,
                            _context.resources.getString(R.string.player_stopped)
                        )
                    }
                    _audioTrack.release()

                } catch (e: Exception) {
                    Log.e("player", "playerJob: ${e.message}")
                }

                _playingSink = -1
                streamChannel.cancel()

                Log.i("player", "playerJob: finished")
            }

            playerJob.join()
            receiverJob.join()
            _playingBaseUrl = ""
            streamChannel.close()
            Log.i("player", "playerMain: finished")
        }
    }

    override fun stop() {
        if (_playingSink != -1) {
            _playingSink = -1
            _playingBaseUrl = ""
            _audioTrack.stop()
        }
    }
}
