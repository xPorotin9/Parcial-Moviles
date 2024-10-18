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

    fun startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.quiz_music)
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun resumeMusic() {
        mediaPlayer?.start()
    }
}