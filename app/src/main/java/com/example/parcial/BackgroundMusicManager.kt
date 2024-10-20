package com.example.parcial

import android.content.Context
import android.media.MediaPlayer

class BackgroundMusicManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        @Volatile
        private var instance: BackgroundMusicManager? = null

        // Singleton para obtener una instancia de la clase
        fun getInstance(context: Context): BackgroundMusicManager {
            return instance ?: synchronized(this) {
                instance ?: BackgroundMusicManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Inicia la música de fondo en bucle
    fun startMusic(resourceId: Int) {
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    // Reproduce un sonido una sola vez
    fun playOneShot(resourceId: Int) {
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer?.isLooping = false
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
            mediaPlayer = null
        }
    }

    // Detiene la música actual
    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    // Pausa la música
    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    // Reanuda la música pausada
    fun resumeMusic() {
        mediaPlayer?.start()
    }
}