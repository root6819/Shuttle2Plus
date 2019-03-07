package com.simplecityapps.shuttle.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simplecityapps.mediaprovider.repository.AlbumArtistRepository
import com.simplecityapps.mediaprovider.repository.AlbumRepository
import com.simplecityapps.mediaprovider.repository.SongRepository
import com.simplecityapps.playback.queue.QueueChangeCallback
import com.simplecityapps.playback.queue.QueueManager
import com.simplecityapps.shuttle.R
import com.simplecityapps.shuttle.ui.screens.playback.PlaybackFragment
import com.simplecityapps.shuttle.ui.screens.playback.mini.MiniPlaybackFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector, QueueChangeCallback {

    private val compositeDisposable = CompositeDisposable()

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject lateinit var queueMager: QueueManager

    @Inject lateinit var songRepository: SongRepository
    @Inject lateinit var albumsRepository: AlbumRepository
    @Inject lateinit var albumArtistsRepository: AlbumArtistRepository


    // Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setupWithNavController(findNavController(R.id.navHostFragment))

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            onHasPermission()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.sheet1Container, PlaybackFragment(), "PlaybackFragment")
                .add(R.id.sheet1PeekView, MiniPlaybackFragment(), "MiniPlaybackFragment")
                .commit()
        }

        // Update visible state of mini player
        queueMager.addCallback(this)
        onQueueChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
    }


    // Permissions

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onHasPermission()
    }

    private fun onHasPermission() {
        compositeDisposable.add(songRepository.populate().subscribe())
        compositeDisposable.add(albumsRepository.populate().subscribe())
        compositeDisposable.add(albumArtistsRepository.populate().subscribe())
    }


    // HasSupportFragmentInjector Implementation

    override fun supportFragmentInjector() = dispatchingAndroidInjector


    // QueueChangeCallback

    override fun onQueueChanged() {
        if (queueMager.getSize() == 0) {
            multiSheetView.hide(collapse = true, animate = false)
        } else {
            multiSheetView.unhide(true)
        }
    }

    override fun onQueuePositionChanged() {
    }

    override fun onShuffleChanged() {
    }

    override fun onRepeatChanged() {
    }


    // Static

    companion object {
        const val TAG = "MainActivity"
    }
}
