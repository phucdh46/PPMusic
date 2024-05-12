package com.dhp.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.player.ExoPlayerService
import com.dhp.musicplayer.player.PlayerConnection
import com.dhp.musicplayer.ui.App
import com.dhp.musicplayer.ui.rememberAppState
import com.dhp.musicplayer.ui.theme.ComposeTheme
import com.dhp.musicplayer.utils.Versioning
import com.dhp.musicplayer.utils.intent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val PERMISSION_READ_AUDIO get() = if (Versioning.isTiramisu()) {
        // READ_EXTERNAL_STORAGE was superseded by READ_MEDIA_AUDIO in Android 13
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var binder: ExoPlayerService.Binder? = null

     var playerConnection by mutableStateOf<PlayerConnection?>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("DHP","serviceConnection")
            if (service is ExoPlayerService.Binder) {
                this@MainActivity.binder = service
                playerConnection = PlayerConnection(this@MainActivity, service, lifecycleScope)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            playerConnection?.dispose()
        }
    }

    private fun doBindService() {
        bindService(intent<ExoPlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
    }
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val viewModel: MainActivityViewModel by viewModels()
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)

            LaunchedEffect(playerConnection) {
                Log.d("DHP","LaunchedEffect: ${getRepeatMode(uiState)}")
                playerConnection?.player?.repeatMode = getRepeatMode(uiState)
            }

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

            ComposeTheme(
                darkTheme = darkTheme
            ) {

                val readAudioPermissionState = rememberPermissionState(PERMISSION_READ_AUDIO)
                if (readAudioPermissionState.status.isGranted) {
                    doBindService()
                    Log.d("DHP","CompositionLocalProvider: ${playerConnection== null} - ${binder==null}")
                    CompositionLocalProvider(
                        LocalPlayerConnection provides playerConnection,
                    ) {
                        App(appState = appState)
                    }
                } else {
                    Column {
                        val textToShow = if (readAudioPermissionState.status.shouldShowRationale) {
                            "The camera is important for this app. Please grant the permission."
                        } else {
                            "Camera not available"
                        }

                        Text(textToShow)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { readAudioPermissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                    }
                }
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
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Success -> when (uiState.userData.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
}


private fun getRepeatMode(
    uiState: MainActivityUiState,
): Int = when (uiState) {
    MainActivityUiState.Loading -> Player.REPEAT_MODE_OFF
    is MainActivityUiState.Success -> when (uiState.userData.repeatMode) {
        RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        RepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
        else -> Player.REPEAT_MODE_OFF
    }
}
/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)


val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
