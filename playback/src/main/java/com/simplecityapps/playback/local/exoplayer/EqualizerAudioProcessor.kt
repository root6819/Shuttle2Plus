package com.simplecityapps.playback.local.exoplayer

import androidx.core.math.MathUtils.clamp
import com.google.android.exoplayer2.audio.BaseAudioProcessor
import com.simplecityapps.playback.equalizer.BandProcessor
import com.simplecityapps.playback.equalizer.Equalizer
import com.simplecityapps.playback.equalizer.fromDb
import com.simplecityapps.playback.equalizer.toNyquistBand
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.max

class EqualizerAudioProcessor(enabled: Boolean) : BaseAudioProcessor() {

    var bandProcessors = emptyList<BandProcessor>()

    var preset: Equalizer.Presets.Preset = Equalizer.Presets.flat
        set(value) {
            field = value
            
            updateBandProcessors()
        }

    // Maximum allowed gain/cut for each band
    val maxBandGain = 12

    var enabled: Boolean = enabled
        set(value) {
            field = value

            Timber.v("Equalizer enabled: $value")
        }

    private fun updateBandProcessors() {

        Timber.v("Updating band processors: (${preset.bands.map { it.gain.toString() }.joinToString(",")})")

        if (channelCount <= 0) {
            return
        }

        bandProcessors = preset.bands.map { band ->
            BandProcessor(
                band.toNyquistBand(),
                sampleRate = sampleRateHz,
                channelCount = channelCount,
                referenceGain = 0
            )
        }.toList()
    }

    override fun configure(sampleRateHz: Int, channelCount: Int, encoding: Int): Boolean {
        val inputChanged = setInputFormat(sampleRateHz, channelCount, encoding)

        updateBandProcessors()

        return inputChanged
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (enabled) {
            var position = inputBuffer.position()
            val limit = inputBuffer.limit()
            val frameCount = (limit - position) / (2 * channelCount)
            val outputSize = frameCount * outputChannelCount * 2
            val buffer = replaceOutputBuffer(outputSize)

            while (position < limit) {
                for (channelIndex in 0 until outputChannelCount) {

                    val samplePosition = position + 2 * channelIndex
                    val originalSample = inputBuffer.getShort(samplePosition)
                    var alteredSample = originalSample.toFloat()

                    // Adjust overall gain to prevent clipping (create headroom). Used when max gain is above 3dB
                    val maxBandGain = max(0, (preset.bands.map { band -> band.gain }.max() ?: 0))
                    if (maxBandGain > 3) {
                        alteredSample = (alteredSample * ((-maxBandGain).fromDb())).toFloat()
                    }

                    for (band in bandProcessors) {
                        alteredSample = band.processSample(alteredSample, channelIndex)
                    }

                    buffer.putShort(clamp(alteredSample, Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toShort())
                }
                position += channelCount * 2
            }
            inputBuffer.position(limit)
            buffer.flip()
        } else {
            val remaining = inputBuffer.remaining()
            if (remaining == 0) {
                return
            }
            replaceOutputBuffer(remaining).put(inputBuffer).flip()
        }
    }
}