package com.simplecityapps.playback

import android.os.Handler
import android.os.Looper
import com.simplecityapps.mediaprovider.model.Song
import com.simplecityapps.playback.audiofocus.AudioFocusHelper
import com.simplecityapps.playback.queue.QueueManager
import timber.log.Timber

class PlaybackManager(
    private val queueManager: QueueManager,
    private val playback: Playback,
    private val audioFocusHelper: AudioFocusHelper
) : Playback.Callback,
    AudioFocusHelper.Listener {

    interface ProgressCallback {

        fun onProgressChanged(position: Int, total: Int)
    }

    private var handler: PlaybackManager.ProgressHandler = PlaybackManager.ProgressHandler()

    private var callbacks: MutableList<Playback.Callback> = mutableListOf()

    private var progressCallbacks: MutableList<ProgressCallback> = mutableListOf()

    init {
        playback.callback = this
        audioFocusHelper.listener = this
    }

    fun togglePlayback() {
        if (playback.isPlaying()) {
            playback.pause()
        } else {
            play()
        }
    }

    fun load(songs: List<Song>, queuePosition: Int = 0, seekPosition: Int = 0, playOnComplete: Boolean) {
        Timber.v("load() called, queuePosition: $queuePosition, seekPosition: $seekPosition, playOnComplete: $playOnComplete")
        queueManager.set(songs, queuePosition)
        playback.load(seekPosition, playOnComplete)
    }

    fun play() {
        Timber.v("play() called")

        if (audioFocusHelper.requestAudioFocus()) {
            playback.play()
        } else {
            Timber.w("play() failed, audio focus request denied.")
        }
    }

    override fun pause() {
        playback.pause()
    }

    fun skipToNext(ignoreRepeat: Boolean = false) {
        queueManager.skipToNext(ignoreRepeat)
        playback.load(0, true)
    }

    fun skipToPrev(force: Boolean = false) {
        if (force || playback.getPosition() ?: 0 < 2000) {
            queueManager.skipToPrevious()
            playback.load(0, true)
        } else {
            seekTo(0)
        }
    }

    fun isPlaying(): Boolean {
        return playback.isPlaying()
    }

    fun addCallback(callback: Playback.Callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun removeCallback(callback: Playback.Callback) {
        callbacks.remove(callback)
    }

    fun addProgressCallback(callback: ProgressCallback) {
        if (!progressCallbacks.contains(callback)) {
            progressCallbacks.add(callback)
        }

        monitorProgress(playback.isPlaying())
    }

    fun removeProgressCallback(callback: ProgressCallback) {
        progressCallbacks.remove(callback)

        monitorProgress(playback.isPlaying())
    }

    fun getPosition(): Int? {
        return playback.getPosition()
    }

    fun getDuration(): Int? {
        return playback.getDuration()
    }

    fun seekTo(position: Int) {
        playback.seek(position)
        updateProgress()
    }


    // Private

    private fun monitorProgress(isPlaying: Boolean) {
        if (isPlaying && progressCallbacks.isNotEmpty()) {
            handler.start { updateProgress() }
        } else {
            handler.stop()
        }
    }

    private fun updateProgress() {
        progressCallbacks.forEach { callback -> callback.onProgressChanged(playback.getPosition() ?: 0, playback.getDuration() ?: 0) }
    }


    // Playback.Callback Implementation

    override fun onPlaystateChanged(isPlaying: Boolean) {
        callbacks.forEach { callback -> callback.onPlaystateChanged(isPlaying) }

        monitorProgress(isPlaying)
    }

    override fun onPlaybackPrepared() {
        callbacks.forEach { callback -> callback.onPlaybackPrepared() }

        updateProgress()
    }

    override fun onPlaybackComplete(song: Song?) {
        callbacks.forEach { callback -> callback.onPlaybackComplete(song) }
    }


    // AudioFocusHelper.Listener Implementation

    override fun restoreVolumeAndplay() {
        playback.setVolume(1.0f)
        play()
    }

    override fun duck() {
        playback.setVolume(0.2f)
    }


    /**
     * A simple handler which executes continuously between start() and stop()
     */
    class ProgressHandler : Handler(Looper.getMainLooper()) {

        var callback: (() -> Unit)? = null

        private val runnable = object : Runnable {
            override fun run() {
                callback?.invoke()
                postDelayed(this, 100)
            }
        }

        fun start(callback: () -> Unit) {
            Timber.v("start()")
            this.callback = callback
            post(runnable)
        }

        fun stop() {
            Timber.v("stop()")
            this.callback = null
            removeCallbacks(runnable)
        }
    }
}