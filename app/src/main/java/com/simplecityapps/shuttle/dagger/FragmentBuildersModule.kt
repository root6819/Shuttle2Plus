package com.simplecityapps.shuttle.dagger

import com.simplecityapps.shuttle.ui.screens.library.albums.AlbumsFragment
import com.simplecityapps.shuttle.ui.screens.library.artists.AlbumArtistsFragment
import com.simplecityapps.shuttle.ui.screens.library.folders.FolderDetailFragment
import com.simplecityapps.shuttle.ui.screens.library.folders.FolderFragment
import com.simplecityapps.shuttle.ui.screens.library.songs.SongsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFolderFragment(): FolderFragment

    @ContributesAndroidInjector
    abstract fun contributeFolderDetailFragment(): FolderDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeSongsFragment(): SongsFragment

    @ContributesAndroidInjector
    abstract fun contributeAlbumsFragment(): AlbumsFragment

    @ContributesAndroidInjector
    abstract fun contributeAlbumArtistsFragment(): AlbumArtistsFragment

}