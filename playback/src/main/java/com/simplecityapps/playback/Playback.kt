package com.simplecityapps.playback

import com.simplecityapps.mediaprovider.model.Song

interface Playback {

    var callback: Callback?

    fun load(playOnPrepared: Boolean)

    fun play()

    fun pause()

    fun isPlaying(): Boolean

    fun seek(position: Int)

    fun getPosition(): Int?

    fun getDuration(): Int?

    interface Callback {

        fun onPlaystateChanged(isPlaying: Boolean)

        fun onPlaybackPrepared()

        fun onPlaybackComplete(song: Song?)
    }

}