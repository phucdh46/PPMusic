package com.dhp.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dhp.musicplayer.base.BaseActivity
import com.dhp.musicplayer.databinding.ActivityMainBinding
import com.dhp.musicplayer.extensions.applyEdgeToEdge
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.dhp.musicplayer.service.PlayerService
import com.dhp.musicplayer.ui.all_music.AllMusicFragment
import com.dhp.musicplayer.utils.Permissions
import com.dhp.musicplayer.utils.Theming
import com.dhp.musicplayer.utils.Versioning
import com.google.android.material.tabs.TabLayout

class MainActivity : BaseActivity<ActivityMainBinding>(), MediaControlInterface {
    private lateinit var mPlayerService: PlayerService
    private var sBound = false
//    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mMusicViewModel: MusicViewModel by viewModels()
    private lateinit var mBindingIntent: Intent
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    private var mAllMusicFragment: AllMusicFragment? = null

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

        requestedOrientation = Theming.getOrientation()

        var newTheme = R.style.BaseTheme_Transparent
        setTheme(newTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            binding.root.applyEdgeToEdge()
        }
    }

    override fun bindUI() {
        super.bindUI()
        mMusicViewModel.deviceMusic.observe(this@MainActivity) { returnedMusic ->
            Log.d("DHP","music: $returnedMusic")
//                finishSetup(returnedMusic)
            initViewPager()

        }
    }

    private fun initFragmentAt(position: Int): Fragment {

        mAllMusicFragment = AllMusicFragment.newInstance()
        return handleOnNavigationItemSelected(position)
    }

    private fun handleOnNavigationItemSelected(index: Int) =  mAllMusicFragment ?: initFragmentAt(index)


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

    override fun onSongSelected(song: Music?, songs: List<Music>?, songLaunchedBy: String) {
        with(mMediaPlayerHolder) {
           // preparePlayback(song)
        }
    }

    override fun onSongsShuffled(songs: List<Music>?, songLaunchedBy: String) {
        TODO("Not yet implemented")
    }

    override fun onAddToQueue(song: Music?) {
        TODO("Not yet implemented")
    }

    override fun onAddAlbumToQueue(songs: List<Music>?, forcePlay: Pair<Boolean, Music?>) {
        TODO("Not yet implemented")
    }

    override fun onUpdatePlayingAlbumSongs(songs: List<Music>?) {
        TODO("Not yet implemented")
    }

    override fun onPlaybackSpeedToggled() {
        TODO("Not yet implemented")
    }

    override fun onHandleCoverOptionsUpdate() {
        TODO("Not yet implemented")
    }

    override fun onUpdatePositionFromNP(position: Int) {
        TODO("Not yet implemented")
    }

    private fun initViewPager() {
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        with(binding.viewPager2) {
            offscreenPageLimit = 1//mGoPreferences.activeTabs.toList().size.minus(1)
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    //mArtistsFragment?.stopActionMode()
                   // mFoldersFragment?.stopActionMode()
                }
            })
           // reduceDragSensitivity()
        }

        initTabLayout()
    }

    private fun initTabLayout() {






        binding.viewPager2.setCurrentItem(
            when {
                else -> 0
            },
            false
        )


    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        override fun getItemCount() = 1
        override fun createFragment(position: Int): Fragment = handleOnNavigationItemSelected(position)
    }
}