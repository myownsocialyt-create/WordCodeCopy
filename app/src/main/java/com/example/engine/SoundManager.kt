package com.example.engine

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Lightweight global sound effects manager.
 * Uses [ToneGenerator] so no bundled audio assets are required.
 */
object SoundManager {

    private const val TAG = "SoundManager"

    /** Global mute toggle used by the UI. */
    @Volatile
    var isMuted: Boolean = false

    enum class SoundType {
        TAP,        // button / cell tap
        SWOOSH,     // drag selection grows
        CHIME,      // word found
        SPARKLE,    // bonus word / reward
        FANFARE,    // level completed
        BUZZ,       // wrong selection
        TICK,       // countdown warning
        GAME_OVER   // time ran out
    }

    private var toneGenerator: ToneGenerator? = null

    private fun generator(): ToneGenerator? {
        toneGenerator?.let { return it }
        return try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 55).also { toneGenerator = it }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ToneGenerator", e)
            null
        }
    }

    fun play(type: SoundType) {
        if (isMuted) return
        try {
            val (tone, durationMs) = when (type) {
                SoundType.TAP -> ToneGenerator.TONE_PROP_BEEP to 50
                SoundType.SWOOSH -> ToneGenerator.TONE_CDMA_PIP to 40
                SoundType.CHIME -> ToneGenerator.TONE_PROP_ACK to 120
                SoundType.SPARKLE -> ToneGenerator.TONE_PROP_BEEP2 to 120
                SoundType.FANFARE -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 220
                SoundType.BUZZ -> ToneGenerator.TONE_PROP_NACK to 120
                SoundType.TICK -> ToneGenerator.TONE_CDMA_PIP to 30
                SoundType.GAME_OVER -> ToneGenerator.TONE_CDMA_ABBR_ALERT to 300
            }
            generator()?.startTone(tone, durationMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound $type", e)
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ToneGenerator", e)
        } finally {
            toneGenerator = null
        }
    }
}
