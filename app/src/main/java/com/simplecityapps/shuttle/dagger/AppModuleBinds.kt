package com.simplecityapps.shuttle.dagger

import android.app.Application
import com.simplecityapps.shuttle.ShuttleApplication
import com.simplecityapps.shuttle.appinitializers.AppInitializer
import com.simplecityapps.shuttle.appinitializers.PlaybackInitializer
import com.simplecityapps.shuttle.appinitializers.TimberInitializer
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class AppModuleBinds {

    @Binds
    abstract fun provideApplication(bind: ShuttleApplication): Application

    @Binds
    @IntoSet
    abstract fun provideTimberInitializer(bind: TimberInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun providePlaybackInitializer(bind: PlaybackInitializer): AppInitializer

}