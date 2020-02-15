package com.simplecityapps.shuttle.ui.screens.settings;

import com.simplecityapps.shuttle.R

enum class SettingsMenuItem {

    Shuffle, SleepTimer, Settings, Equalizer;

    val icon: Int
        get() {
            return when (this) {
                Shuffle -> R.drawable.ic_shuffle_black_24dp
                SleepTimer -> R.drawable.ic_sleep_black_24dp
                Equalizer -> R.drawable.ic_equalizer_black_24dp
                Settings -> R.drawable.ic_settings_black_24dp
            }
        }

    val title: String
        get() {
            return when (this) {
                SleepTimer -> "Sleep Timer"
                Shuffle -> "Shuffle All"
                Equalizer -> "Equalizer"
                Settings -> "Settings"
            }
        }
}