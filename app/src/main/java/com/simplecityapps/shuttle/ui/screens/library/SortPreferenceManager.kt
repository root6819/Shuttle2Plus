package com.simplecityapps.shuttle.ui.screens.library

import android.content.SharedPreferences
import com.simplecityapps.mediaprovider.repository.AlbumSortOrder
import com.simplecityapps.mediaprovider.repository.SongSortOrder
import com.simplecityapps.shuttle.persistence.get
import com.simplecityapps.shuttle.persistence.put

class SortPreferenceManager(private val sharedPreferences: SharedPreferences) {

    var sortOrderSongList: SongSortOrder
        set(value) {
            sharedPreferences.put("sort_order_song_list", value.name)
        }
        get() {
            return try {
                SongSortOrder.valueOf(sharedPreferences.get("sort_order_song_list", SongSortOrder.SongName.name))
            } catch (e: IllegalArgumentException) {
                SongSortOrder.SongName
            }
        }

    var sortOrderAlbumList: AlbumSortOrder
        set(value) {
            sharedPreferences.put("sort_order_album_list", value.name)
        }
        get() {
            return try {
                AlbumSortOrder.valueOf(sharedPreferences.get("sort_order_album_list", AlbumSortOrder.AlbumName.name))
            } catch (e: IllegalArgumentException) {
                AlbumSortOrder.AlbumName
            }
        }
}