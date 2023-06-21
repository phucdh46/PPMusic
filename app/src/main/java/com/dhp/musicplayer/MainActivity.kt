package com.dhp.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.dhp.musicplayer.base.BaseActivity
import com.dhp.musicplayer.databinding.ActivityMainBinding
import com.dhp.musicplayer.service.PlayerService
import com.dhp.musicplayer.utils.Permissions
import com.dhp.musicplayer.utils.Versioning

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mPlayerService: PlayerService
    private var sBound = false
//    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mMusicViewModel: MusicViewModel by viewModels()
    private lateinit var mBindingIntent: Intent

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            // get bound service and instantiate MediaPlayerHolder
            val binder = service as PlayerService.LocalBinder
            mPlayerService = binder.getService()
            sBound = true

//            mMediaPlayerHolder.mediaPlayerInterface = mMediaPlayerInterface

            // load music and setup UI
            mMusicViewModel.getDeviceMusic()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            sBound = false
        }
    }

    private fun doBindService() {
        mBindingIntent = Intent(this, PlayerService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private val requestReadStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            doBindService()
        }
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun bindUI() {
        super.bindUI()
        mMusicViewModel.deviceMusic.observe(this@MainActivity) { returnedMusic ->
            Log.d("DHP","music: $returnedMusic")
//                finishSetup(returnedMusic)
        }
    }

    override fun onStart() {
        super.onStart()
        if (Permissions.hasToAskForReadStoragePermission(this)) {
            Permissions.manageAskForReadStoragePermission(this, requestReadStoragePermissionLauncher)
            return
        }
        doBindService()
    }

    override fun onDestroy() {
        if (sBound) unbindService(connection)
        super.onDestroy()
    }
}