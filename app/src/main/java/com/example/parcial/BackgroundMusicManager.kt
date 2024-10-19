package com.example.parcial

import android.content.Context
import android.media.MediaPlayer

class BackgroundMusicManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        @Volatile
        private var instance: BackgroundMusicManager? = null

        fun getInstance(context: Context): BackgroundMusicManager {
            return instance ?: synchronized(this) {
                instance ?: BackgroundMusicManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun startMusic(resourceId: Int) {
        stopMusic() // Detener cualquier música anterior
        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun playOneShot(resourceId: Int) {
        stopMusic() // Detener cualquier música anterior
        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer?.isLooping = false
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
            mediaPlayer = null
        }
    }

    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun resumeMusic() {
        mediaPlayer?.start()
    }
}