package com.simplecityapps.shuttle.compose.ui.components.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.simplecityapps.shuttle.compose.ui.BottomSettings
import com.simplecityapps.shuttle.compose.ui.components.ThemedPreviewProvider
import com.simplecityapps.shuttle.compose.ui.components.onboarding.Onboarding
import com.simplecityapps.shuttle.compose.ui.components.settings.bottomsheet.SettingsBottomSheet
import com.simplecityapps.shuttle.compose.ui.theme.MaterialColors
import com.simplecityapps.shuttle.compose.ui.theme.Theme
import com.simplecityapps.shuttle.ui.mediaprovider.MediaProviderSelectionViewModel
import com.simplecityapps.shuttle.ui.onboarding.OnboardingViewModel
import com.simplecityapps.shuttle.ui.root.RootViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Root(viewModel: RootViewModel) {
    val hasOnboarded by viewModel.hasOnboarded.collectAsState()
    Root(hasOnboarded)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Root(hasOnboarded: Boolean) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    val backgroundColor = MaterialColors.background

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = useDarkIcons
        )
    }

    val navController = rememberNavController()

    val scope = rememberCoroutineScope()

    val settingsBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetContent = {
            SettingsBottomSheet(onItemSelected = { bottomSettings ->
                scope.launch {
                    settingsBottomSheetState.hide()
                }
                when (bottomSettings) {
                    BottomSettings.Shuffle -> {
                        // Todo:
                    }
                    BottomSettings.SleepTimer -> {
                        // Todo:
                    }
                    BottomSettings.Dsp -> {
                        navController.navigate(Screen.Root.Dsp.route)
                    }
                    BottomSettings.Settings -> {
                        navController.navigate(Screen.Root.Settings.route)
                    }
                }
            })
        },
        sheetState = settingsBottomSheetState
    ) {
        NavHost(
            navController = navController,
            startDestination = if (hasOnboarded) Screen.Root.Main.route else Screen.Root.MediaProviderSelection.route
        ) {
            composable(
                route = Screen.Root.MediaProviderSelection.route,
                arguments = listOf(navArgument(MediaProviderSelectionViewModel.ARG_ONBOARDING) { defaultValue = true })
            ) {
                Onboarding(hiltViewModel() as OnboardingViewModel, onboardingComplete = {
                    navController.navigate(Screen.Root.Main.route)
                })
            }
            composable(Screen.Root.Main.route) {
                Main(
                    onShowSettings = {
                        scope.launch {
                            settingsBottomSheetState.show()
                        }
                    }
                )
            }
            composable(Screen.Root.Dsp.route) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Blue.copy(alpha = 0.2f))
                )
            }
            composable(Screen.Root.Settings.route) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Green.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RootPreview(@PreviewParameter(ThemedPreviewProvider::class) darkTheme: Boolean) {
    Theme(isDark = darkTheme) {
        Root(hasOnboarded = true)
    }
}