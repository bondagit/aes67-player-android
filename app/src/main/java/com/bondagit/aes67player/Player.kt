//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//

package com.bondagit.aes67player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.bondagit.aes67player.model.StreamerInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URL
import java.nio.ByteBuffer


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
    private val _maxDurationSecs = 60
    private var _context = context
    private var _sampleSize: Int = 0
    private var _playingSink: Int = -1
    private var _playingBaseUrl: String = ""
    private lateinit var _playJob: Job
    private lateinit var _audioTrack: AudioTrack
    private lateinit var _decoder: MediaCodec
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

    private fun makeAACCodecSpecificData(
        audioProfile: Int, sampleRate: Int, channelConfig: Int
    ): MediaFormat? {
        val format = MediaFormat()
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm")
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate)
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelConfig)
        //format.setInteger(MediaFormat.KEY_AAC_SBR_MODE, 1)
        //format.setInteger(MediaFormat.KEY_ENCODER_DELAY, 0)
        //format.setInteger(MediaFormat.KEY_ENCODER_PADDING, 0)
        format.setInteger(MediaFormat.KEY_IS_ADTS, 1)
        val samplingFreq = intArrayOf(
            96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000
        )

        // Search the Sampling Frequencies
        var sampleIndex = -1
        for (i in samplingFreq.indices) {
            if (samplingFreq[i] == sampleRate) {
                Log.d("makeAACCodecSpecificData", "SamplingFreq " + samplingFreq[i] + " i : " + i)
                sampleIndex = i
            }
        }
        if (sampleIndex == -1) {
            return null
        }
        val csd: ByteBuffer = ByteBuffer.allocate(2)
        csd.put((audioProfile shl 3 or (sampleIndex shr 1)).toByte())
        csd.position(1)
        csd.put(((sampleIndex shl 7 and 0x80) or ((channelConfig shl 3))).toByte())
        csd.flip()
        format.setByteBuffer("csd-0", csd) // add csd-0
        for (k in 0 until csd.capacity()) {
            Log.e("makeAACCodecSpecificData", "csd : " + csd.array()[k])
        }

        return format
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

        var bufferSize: Int = info.rate * _sampleSize * info.channels * info.fileDuration
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

            val format = makeAACCodecSpecificData(
                MediaCodecInfo.CodecProfileLevel.AACObjectLC, info.rate, info.channels
            )
            if (format == null) {
                _onPlayEnd(
                    baseUrl, sinkId, true, _context.resources.getString(R.string.audio_init_failed)
                )
                return@launch
            }

            _decoder = MediaCodec.createDecoderByType("audio/mp4a-latm")
            _decoder.configure(format, null, null, 0)

            if (!openAudioTrack(baseUrl, sinkId, info)) return@launch

            val streamChannel = Channel<ByteArray>(
                info.playerBufferFilesMum * 21 //ADTS frames by 1024 samples each per sec
            )

            _playingSink = sinkId

            val receiverJob = GlobalScope.launch(Dispatchers.IO) {
                var pos = info.startFileId
                var count = 0
                var restarts = 0
                var receiverSuspended = false
                var receiverSlow = false
                var receiverException = false

                while (_playingSink != -1) {
                    try {
                        val timestamp: Long = System.currentTimeMillis()
                        val url = "$baseUrl/api/streamer/stream/$sinkId/${pos}"
                        val connection = URL(url).openConnection()
                        val fileCount = connection.getHeaderField("X-File-Count").toInt()
                        val currFileId = connection.getHeaderField("X-File-Current-Id").toInt()
                        val startFileId = connection.getHeaderField("X-File-Start-Id").toInt()
                        val input = connection.inputStream.readBytes()

                        Log.i(
                            "player",
                            "receiverJob: fetching file $url time ${System.currentTimeMillis() - timestamp} ms"
                        )

                        if (count == 0) {
                            count = fileCount
                        }

                        if (fileCount < count || currFileId == pos) {
                            Log.w(
                                "player",
                                "receiverJob: player not in sync file count $fileCount count $count file id $currFileId pos $pos restarts $restarts re-buffering ..."
                            )
                            pos = startFileId
                            count = 0
                            if (restarts++ < 10) {
                                continue
                            }
                            receiverException = true
                            break
                        }

                        if ((System.currentTimeMillis() - timestamp) > info.fileDuration * 1000 * info.filesNum) {
                            Log.i(
                                "player",
                                "receiverJob: slept for more than {$info.fileDuration*$info.filesNum} ms, stopping"
                            )
                            receiverSuspended = true
                            break
                        }

                        if ((System.currentTimeMillis() - timestamp) > info.playerBufferFilesMum * 2 * info.fileDuration * 1000) {
                            Log.e("player", "receiverJob: fetching time longer than player buffer")
                            receiverSlow = true
                            break
                        }

                        //Parse ADTS header FF F1 4C [80 17 BF]
                        var start = 0
                        var end = 0
                        var offset = 0
                        var num = 0
                        while (offset < input.size) {
                            val adtsLen =
                                ((input[offset + 3].toUInt() and 0x3U) shl 11) + ((input[offset + 4].toUInt() and 0xFFU) shl 3) + ((input[offset + 5].toUInt() and 0xe0U) shr 5)
                            offset += adtsLen.toInt()
                            end += adtsLen.toInt()
                            if (++num > 1) {
                                streamChannel.send(input.copyOfRange(start, end))
                                start = end
                                num = 0
                            }
                        }

                        if (num != 0) {
                            streamChannel.send(input.copyOfRange(start, end))
                        }

                        ++pos
                        pos %= info.filesNum
                    } catch (e: Exception) {
                        Log.e("player", "receiverJob: ${e.message}")
                        if (_playingSink != -1) receiverException = true
                        break
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
                    val timeoutUs = 100000L
                    var totalFrames = 0
                    var count = 0

                    _decoder.start()
                    _audioTrack.play()

                    while (_playingSink != -1) {
                        val input = streamChannel.receive()
                        var timestamp: Long
                        val inIndex: Int = _decoder.dequeueInputBuffer(timeoutUs)
                        if (inIndex < 0) {
                            Log.d(
                                "DecodeActivity", "dequeueInputBuffer timed out!"
                            )
                            continue
                        }
                        val buffer = _decoder.getInputBuffer(inIndex)
                        if (buffer == null) {
                            Log.d(
                                "DecodeActivity", "dequeueInputBuffer is null!"
                            )
                            continue
                        }

                        //buffer.clear()
                        buffer.put(input)

                        _decoder.queueInputBuffer(
                            inIndex, 0, input.size, 0, 0
                        )
                        val bufferInfo = MediaCodec.BufferInfo()

                        val outIndex: Int = _decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                        when (outIndex) {
                            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                _audioTrack.playbackRate =
                                    format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

                                Log.d(
                                    "DecodeActivity",
                                    "format changed rate ${format.getInteger(MediaFormat.KEY_SAMPLE_RATE)} channels ${
                                        format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                                    }"
                                )
                            }

                            MediaCodec.INFO_TRY_AGAIN_LATER -> {
                                Log.d(
                                    "DecodeActivity", "dequeueOutputBuffer timed out!"
                                )
                            }

                            else -> {
                                val outBuffer = _decoder.getOutputBuffer(outIndex)
                                if (outBuffer == null) {
                                    Log.d(
                                        "DecodeActivity", "dequeueOutputBuffer is null!"
                                    )
                                    continue
                                }

                                val samples = ByteArray(bufferInfo.size)

                                outBuffer.get(samples)
                                outBuffer.clear()

                                timestamp = System.currentTimeMillis()
                                val headPosition: Int = _audioTrack.playbackHeadPosition
                                /*
                                val diff = totalFrames - headPosition
                                Log.i(
                                    "player",
                                    "playerJob: count $count audio position $headPosition samples $totalFrames diff $diff"
                                )*/

                                if (headPosition / info.rate > _maxDurationSecs) {
                                    Log.i(
                                        "player", "playerJob: reached max playback duration"
                                    )
                                    break
                                }

                                totalFrames += bufferInfo.size / _sampleSize / info.channels
                                _audioTrack.write(
                                    samples, bufferInfo.offset, bufferInfo.offset + bufferInfo.size
                                ) // AudioTrack write data

                                _decoder.releaseOutputBuffer(outIndex, false)

                                if (count == 0) {
                                    _onPlayStarted(baseUrl, sinkId)
                                    Log.i(
                                        "player",
                                        "playerJob: playback time ${System.currentTimeMillis() - timestamp}"
                                    )
                                }

                                // All decoded frames have been rendered, we can stop playing now
                                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                    Log.d(
                                        "DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM"
                                    )
                                    break
                                }
                                count++
                            }
                        }
                    }

                    if (_audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                        if (_audioTrack.playbackHeadPosition / info.rate >= _maxDurationSecs) _onPlayEnd(
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("player", "playerJob: exception ${e.message}")
                    if (e.message == null || e.message != "Channel was cancelled") {
                        _onPlayEnd(
                            baseUrl,
                            sinkId,
                            true,
                            _context.resources.getString(R.string.player_error)
                        )
                    }
                }
                _audioTrack.release()
                _playingSink = -1
                //streamChannel.cancel()
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
            //_audioTrack.setVolume(0.0f)
            //_audioTrack.flush()
            _playingSink = -1
            _playingBaseUrl = ""
            _audioTrack.stop()
        }
    }
}
