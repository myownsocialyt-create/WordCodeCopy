package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var isInitialized = false

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()
            isInitialized = true
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize SoundPool", e)
        }
    }

    fun playClickSound() {
        // Safe sound playback placeholder
        if (!isInitialized || soundPool == null) return
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            isInitialized = false
            Log.d("SoundManager", "SoundManager resources successfully released")
        } catch (e: Exception) {
            Log.e("SoundManager", "Error releasing sound pool", e)
        }
    }
}
