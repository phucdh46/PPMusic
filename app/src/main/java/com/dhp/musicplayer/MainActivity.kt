package com.dhp.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import coil.imageLoader
import coil.request.ImageRequest
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.designsystem.extractThemeColor
import com.dhp.musicplayer.core.designsystem.theme.ColorSaver
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.core.model.settings.UserData
import com.dhp.musicplayer.core.services.download.DownloadUtil
import com.dhp.musicplayer.core.services.extensions.toSong
import com.dhp.musicplayer.core.services.player.PlayerConnection
import com.dhp.musicplayer.core.datastore.DynamicThemeKey
import com.dhp.musicplayer.core.common.extensions.intent
import com.dhp.musicplayer.core.services.player.PlaybackService
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.ui.App
import com.dhp.musicplayer.ui.rememberAppState
import com.dhp.musicplayer.utils.InAppUpdateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var downloadUtil: DownloadUtil

    var playerConnection by mutableStateOf<PlayerConnection?>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlaybackService.MusicBinder) {
                playerConnection = PlayerConnection(this@MainActivity, service, lifecycleScope)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
        }
    }

    private fun doBindService() {
        bindService(intent<PlaybackService>(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        inAppUpdateManager = InAppUpdateManager(this@MainActivity)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val viewModel: MainActivityViewModel by viewModels()
        var uiState: UiState<UserData> by mutableStateOf(UiState.Loading)
        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
//                MainActivityUiState.Loading -> true
                is UiState.Success -> false
                else -> true
            }
        }

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }

            val appState = rememberAppState()
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                inAppUpdateManager.checkForUpdate(onShowSnackBar = { onActionClick ->
                    scope.launch {
                        val result = appState.snackBarHostState
                            .showSnackbar(
                                message = this@MainActivity.getString(R.string.update_was_downloaded_snack_bar),
                                actionLabel = this@MainActivity.getString(R.string.restart_string),
                                // Defaults to SnackbarDuration.Short
                                duration = SnackbarDuration.Indefinite
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            onActionClick()
                        }
                    }
                })
            }
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val enableDynamicTheme by rememberPreference(DynamicThemeKey, defaultValue = false)
            val color = androidx.compose.ui.graphics.Color.Unspecified

            var themeColor by rememberSaveable(stateSaver = ColorSaver) {
                mutableStateOf(color)
            }
            LaunchedEffect(playerConnection, enableDynamicTheme, isSystemInDarkTheme) {
                if (!enableDynamicTheme || playerConnection == null) {
                    themeColor = color
                    return@LaunchedEffect
                }
                playerConnection?.currentMediaItem?.collectLatest { mediaItem ->
                    themeColor = if (mediaItem != null) {
                        withContext(Dispatchers.IO) {
                            val result = imageLoader.execute(
                                ImageRequest.Builder(this@MainActivity)
                                    .data(mediaItem.toSong().thumbnailUrl)
                                    .allowHardware(false)
                                    .build()
                            )
                            (result.drawable as? BitmapDrawable)?.bitmap?.extractThemeColor()
                                ?: color
                        }
                    } else color
                }
            }

            com.dhp.musicplayer.core.designsystem.theme.ComposeTheme(
                darkTheme = darkTheme,
                themeColor = themeColor,
                enableDynamicTheme = enableDynamicTheme,
            ) {
                App(appState = appState, playerConnection, downloadUtil)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        doBindService()
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }
}

@Composable
private fun shouldUseDarkTheme(
    uiState: UiState<UserData>,
): Boolean = when (uiState) {
//    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is UiState.Success -> when (uiState.data.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
    else -> isSystemInDarkTheme()
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
