package com.simplecityapps.shuttle.ui.screens.library.albumartists

import com.simplecityapps.mediaprovider.model.removeArticles
import com.simplecityapps.mediaprovider.repository.AlbumArtistRepository
import com.simplecityapps.shuttle.ui.common.mvp.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class AlbumArtistListPresenter @Inject constructor(
    private val albumArtistRepository: AlbumArtistRepository
) : AlbumArtistListContract.Presenter, BasePresenter<AlbumArtistListContract.View>() {

    override fun loadAlbumArtists() {
        addDisposable(
            albumArtistRepository.getAlbumArtists()
                .map { albumArtists -> albumArtists.sortedBy { albumArtist -> albumArtist.name.removeArticles() } }
                .subscribe(
                    { albumArtists -> view?.setAlbumArtists(albumArtists) },
                    { error -> Timber.e(error, "Failed to retrieve album artists") })
        )
    }
}