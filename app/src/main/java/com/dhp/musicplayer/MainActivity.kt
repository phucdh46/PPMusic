package com.dhp.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ServiceCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dhp.musicplayer.base.BaseActivity
import com.dhp.musicplayer.databinding.ActivityMainBinding
import com.dhp.musicplayer.databinding.PlayerControlsPanelBinding
import com.dhp.musicplayer.dialogs.RecyclerSheet
import com.dhp.musicplayer.extensions.*
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.*
import com.dhp.musicplayer.ui.all_music.AllMusicFragment
import com.dhp.musicplayer.ui.list_music.MusicContainersFragment
import com.dhp.musicplayer.ui.local_music.LocalMusicFragment
import com.dhp.musicplayer.ui.now_playing.NowPlaying
import com.dhp.musicplayer.ui.setting.SettingsFragment
import com.dhp.musicplayer.utils.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(), UIControlInterface,
    MusicProgressViewUpdateHelper.Callback {
    private lateinit var mPlayerService: PlayerService
//    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mMusicViewModel: MusicViewModel by viewModels()
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    // Fragments
    private var mArtistsFragment: MusicContainersFragment? = null
    private var mAllMusicFragment: AllMusicFragment? = null
    private var mLocalMusicFragment: LocalMusicFragment? = null
    private var mAlbumsFragment: MusicContainersFragment? = null
    private var mSettingsFragment: SettingsFragment? = null
    private var mFoldersFragment: MusicContainersFragment? = null
    private var mTabToRestore = -1

    private lateinit var mPlayerControlsPanelBinding: PlayerControlsPanelBinding

    // Preferences
    private val mPreferences get() = Preferences.getPrefsInstance()

    // Sleep timer dialog
    private var mSleepTimerDialog: RecyclerSheet? = null

     var binder: ExoPlayerService.Binder? = null

     var playerConnection: PlayerConnection? = null
     lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("DHP","serviceConnection")
            if (service is ExoPlayerService.Binder) {
                this@MainActivity.binder = service
                playerConnection = PlayerConnection(this@MainActivity, service, lifecycleScope)
                playerConnection!!.mediaPlayerInterface = mMediaPlayerInterface
//                progressViewUpdateHelper.start(playerConnection)

                lifecycleScope.launch {
                    playerConnection?.isPlaying?.collect { isPlaying ->
                        Log.d("DHP","isPlaying: $isPlaying")
                        progressViewUpdateHelper.apply {
                            if (isPlaying) start(playerConnection) else stop()
                        }
                        mPlayerControlsPanelBinding.playPauseButton.setImageResource(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        )
                    }
                }
                lifecycleScope.launch {
                    playerConnection?.currentMediaItem?.collect {
                        Log.d("DHP","currentMediaItem: $it")
                        mPlayerControlsPanelBinding.miniPlayer.handleViewVisibility(it != null)

                        mPlayerControlsPanelBinding.playingSong.text = it?.mediaMetadata?.title
                        mPlayerControlsPanelBinding.playingArtist.text = it?.mediaMetadata?.artist

                    }
                }

            }
            mMusicViewModel.getDeviceMusic()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            playerConnection?.dispose()
        }
    }


    private var nowPlayingFragment: NowPlaying? = null
    // Queue dialog
    private var mQueueDialog: RecyclerSheet? = null

    private var mAllMusic: List<Music>? = null

    // interface to let MediaPlayerHolder update the UI media player controls.
    private val mMediaPlayerInterface = object : MediaPlayerInterface {

        override fun onUpdateRepeatStatus() {
//            mNpDialog?.updateRepeatStatus(onPlaybackCompletion = false)
        }

        override fun onClose() {
            //finish activity if visible
            finishAndRemoveTask()
        }

        override fun onPositionChanged(position: Int) {
//            mPlayerControlsPanelBinding.songProgress.setProgressCompat(position, true)
//            nowPlayingFragment?.updateProgress(position)
        }

        override fun onStateChanged() {
            updatePlayingStatus()
            nowPlayingFragment?.updatePlayingStatus()
            if (mMediaPlayerHolder.state != Constants.RESUMED && mMediaPlayerHolder.state != Constants.PAUSED) {
                updatePlayingInfo(restore = false)
//                if (mMediaPlayerHolder.isQueue != null) {
//                    mQueueDialog?.swapQueueSong(mMediaPlayerHolder.currentSong)
//                }
//                if (sDetailsFragmentExpanded) {
//                    mDetailsFragment?.swapSelectedSong(
//                        mMediaPlayerHolder.currentSong?.id
//                    )
//                }
            }
        }

        override fun onQueueEnabled() {
//            mPlayerControlsPanelBinding.queueButton.updateIconTint(
//                ContextCompat.getColor(this@MainActivity, R.color.widgets_color)
//            )
        }

        override fun onQueueStartedOrEnded(started: Boolean) {
//            mPlayerControlsPanelBinding.queueButton.updateIconTint(
//                when {
//                    started -> Theming.resolveThemeColor(resources)
//                    mMediaPlayerHolder.queueSongs.isEmpty() -> {
//                        mQueueDialog?.dismissAllowingStateLoss()
//                        Theming.resolveWidgetsColorNormal(this@MainActivity)
//                    }
//                    else -> {
//                        mQueueDialog?.dismissAllowingStateLoss()
//                        ContextCompat.getColor(
//                            this@MainActivity,
//                            R.color.widgets_color
//                        )
//                    }
//                }
//            )
        }

        override fun onBackupSong() {
//            if (checkIsPlayer(showError = false) && !mMediaPlayerHolder.isPlaying) {
//                mGoPreferences.latestPlayedSong =
//                    mMediaPlayerHolder.currentSong?.copy(
//                        startFrom = mMediaPlayerHolder.playerPosition,
//                        launchedBy = mMediaPlayerHolder.launchedBy
//                    )
//            }
        }

        override fun onUpdateSleepTimerCountdown(value: Long) {
            mSleepTimerDialog?.run {
                if (sheetType == RecyclerSheet.SLEEPTIMER_ELAPSED_TYPE) {
                    val newValue = value.toFormattedDuration(isAlbum = false, isSeekBar = true)
                    updateCountdown(newValue)
                }
            }
        }

        override fun onStopSleepTimer() {
//            mSleepTimerDialog?.dismissAllowingStateLoss()
//            updateSleepTimerIcon(isEnabled = false)
        }

        override fun onUpdateFavorites() {
//            onFavoriteAddedOrRemoved()
        }

        override fun onRepeat(toastMessage: Int) {
            Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()
        }

        override fun onListEnded() {
            Toast.makeText(this@MainActivity, R.string.error_list_ended, Toast.LENGTH_SHORT).show()
        }

        override fun onPlayerReady() {
            com.dhp.musicplayer.utils.Log.d("onPlayerReady: ${playerConnection?.player?.duration!!.toInt()}")
//            mPlayerControlsPanelBinding.songProgress.progress = 0
//            mPlayerControlsPanelBinding.songProgress.max = playerConnection?.player?.duration!!.toInt()
            onUpdateProgressViews(playerConnection?.player?.currentPosition!!.toInt(), playerConnection?.player?.duration!!.toInt())
        }
    }

    private fun updatePlayingStatus() {
        if (mMediaPlayerHolder.isPlaying) {
            mPlayerControlsPanelBinding.playPauseButton.setImageResource(R.drawable.ic_pause)
            return
        }
        mPlayerControlsPanelBinding.playPauseButton.setImageResource(R.drawable.ic_play)
    }

    // method to update info on controls panel
    private fun updatePlayingInfo(restore: Boolean) {

//        val selectedSong = mMediaPlayerHolder.currentSongFM ?: mMediaPlayerHolder.currentSong
//
//        mPlayerControlsPanelBinding.songProgress.progress = 0
//        mPlayerControlsPanelBinding.songProgress.max = selectedSong?.duration!!.toInt()
//
//        mPlayerControlsPanelBinding.playingSong.text = selectedSong.title
//
//        mPlayerControlsPanelBinding.playingArtist.text = getString(R.string.artist_and_album, selectedSong.artist, selectedSong.album)

//            mNpDialog?.run {
//                updateRepeatStatus(onPlaybackCompletion = false)
//                updateNpInfo()
//            }

        if (restore) {

//                if (mMediaPlayerHolder.queueSongs.isNotEmpty() && !mMediaPlayerHolder.isQueueStarted) {
//                    mMediaPlayerInterface.onQueueEnabled()
//                } else {
//                    mMediaPlayerInterface.onQueueStartedOrEnded(started = mMediaPlayerHolder.isQueueStarted)
//                }

            updatePlayingStatus()

            if (::mPlayerService.isInitialized) {
                //stop foreground if coming from pause state
                with(mPlayerService) {
//                        if (isRestoredFromPause) {
//                            ServiceCompat.stopForeground(mPlayerService, ServiceCompat.STOP_FOREGROUND_DETACH)
//                            musicNotificationManager.updateNotification()
//                            isRestoredFromPause = false
//                        }
                }
            }
        }
    }

    private val requestReadStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
//            doBindService()
        }
    }

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = Theming.getOrientation()

        var newTheme = Theming.resolveTheme(this)

        setTheme(newTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            binding.root.applyEdgeToEdge()
        }
        mPlayerControlsPanelBinding = PlayerControlsPanelBinding.bind(binding.root)

        savedInstanceState?.run {
            mTabToRestore = getInt(Constants.RESTORE_FRAGMENT, -1)
        }

        if (intent.hasExtra(Constants.RESTORE_FRAGMENT) && mTabToRestore == -1) {
            mTabToRestore = intent.getIntExtra(Constants.RESTORE_FRAGMENT, -1)
        }

        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constants.RESTORE_FRAGMENT, binding.viewPager2.currentItem)
    }

    override fun bindUI() {
        super.bindUI()
        mMusicViewModel.deviceMusic.observe(this@MainActivity) { returnedMusic ->
            Log.d("DHP","music: $returnedMusic")
//                finishSetup(returnedMusic)
            // Be sure that prefs are initialized
            Preferences.initPrefs(this)
            mAllMusic = returnedMusic
            initMediaButtons()
            initViewPager()
            synchronized(handleRestore()) {
                binding.mainView.animate().run {
                    duration = 750
                    alpha(1.0F)
                }
            }

        }
    }

    private fun handleRestore() {
        synchronized(restorePlayerStatus()) {
//            if (sLaunchedByTile) {
//                finishAndRemoveTask()
//                mMediaPlayerHolder.resumeMediaPlayer()
//            }
        }
//        handleIntent(intent)
    }

    private fun restorePlayerStatus() {
        // If we are playing and the activity was restarted
        // update the controls panel
        with(mMediaPlayerHolder) {
            if (isMediaPlayer && isPlaying) {
                onRestartSeekBarCallback()
                updatePlayingInfo(restore = true)
                return
            }

            val song = mPreferences.latestPlayedSong
            isSongFromPrefs = song != null
//
//            if (!mGoPreferences.queue.isNullOrEmpty()) {
//                queueSongs = mGoPreferences.queue?.toMutableList()!!
//                setQueueEnabled(enabled = true, canSkip = false)
//            }
//
//            val preQueueSong = mGoPreferences.isQueue
//            if (preQueueSong != null) {
//                isQueue = preQueueSong
//                isQueueStarted = true
//                mediaPlayerInterface.onQueueStartedOrEnded(started = true)
//            }
//
            song?.let { restoredSong ->

//                val sorting = restoredSong.findRestoreSorting(restoredSong.launchedBy)
//                val songs = restoredSong.findRestoreSongs(sorting, mMusicViewModel)

                if (!mAllMusic.isNullOrEmpty()) {
                    isPlay = false
                    updateCurrentSong(restoredSong, mAllMusic, restoredSong.launchedBy)
                    updatePlayingInfo(restore = false)
                    mPlayerControlsPanelBinding.songProgress.setProgressCompat(
                       0, //if (isCurrentSongFM) 0 else restoredSong.startFrom,
                        true
                    )
                    return
                }
                //notifyError(GoConstants.TAG_SD_NOT_READY)
            }
        }
    }

    private fun initMediaButtons() {
        mPlayerControlsPanelBinding.playPauseButton.setOnClickListener {
//            mMediaPlayerHolder.resumeOrPause()
            playerConnection?.playOrPause()
        }

        with(mPlayerControlsPanelBinding.playingSongContainer) {
            safeClickListener {
                openNowPlayingFragment()
            }
            setOnLongClickListener {
                //onOpenPlayingArtistAlbum()
                return@setOnLongClickListener true
            }
        }
    }


    private fun openNowPlayingFragment() {
        if (//  checkIsPlayer(showError = true) &&
            //mMediaPlayerHolder.isCurrentSong &&
            nowPlayingFragment == null) {
            val bundle = Bundle()
            bundle.putInt("progress", mPlayerControlsPanelBinding.songProgress.progress)
            bundle.putInt("max", mPlayerControlsPanelBinding.songProgress.max)
            nowPlayingFragment = NowPlaying.newInstance().apply {
                show(supportFragmentManager, null)
                arguments = bundle
                onNowPlayingCancelled = {
                    nowPlayingFragment = null
                }
            }
        }
    }

    private fun initFragmentAt(position: Int): Fragment {
        when (mPreferences.activeTabs.toList()[position]) {
//            Constants.SONGS_TAB -> mAllMusicFragment = AllMusicFragment.newInstance()
            Constants.LOCAL_SONG_TAB -> mLocalMusicFragment = LocalMusicFragment.newInstance()
            else -> mSettingsFragment = SettingsFragment.newInstance()
        }
        return handleOnNavigationItemSelected(position)
    }

    private fun handleOnNavigationItemSelected(index: Int) = when (mPreferences.activeTabs.toList()[index]) {
//        Constants.SONGS_TAB -> mAllMusicFragment ?: initFragmentAt(index)
        Constants.LOCAL_SONG_TAB -> mLocalMusicFragment ?: initFragmentAt(index)
        else -> mSettingsFragment ?: initFragmentAt(index)
    }


    override fun onStart() {
        super.onStart()
        if (Permissions.hasToAskForReadStoragePermission(this)) {
            Permissions.manageAskForReadStoragePermission(this, requestReadStoragePermissionLauncher)
            return
        }
        bindService(intent<ExoPlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
        progressViewUpdateHelper.start(playerConnection)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        progressViewUpdateHelper.stop()
         super.onStop()
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
            reduceDragSensitivity()
        }

        initTabLayout()
    }

    private fun initTabLayout() {

        val accent = Theming.resolveThemeColor(resources)
        val alphaAccentColor = ColorUtils.setAlphaComponent(accent, 200)

        with(mPlayerControlsPanelBinding.tabLayout) {

            tabIconTint = ColorStateList.valueOf(alphaAccentColor)

            TabLayoutMediator(this, binding.viewPager2) { tab, position ->
                val selectedTab = mPreferences.activeTabs.toList()[position]
                tab.setIcon(Theming.getTabIcon(selectedTab))
                tab.setContentDescription(Theming.getTabAccessibilityText(selectedTab))
            }.attach()

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.icon?.setTint(accent)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    //closeDetails()
                    tab.icon?.setTint(alphaAccentColor)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    //closeDetails()
                }
            })
        }

        binding.viewPager2.setCurrentItem(
            when {
                mTabToRestore != -1 -> mTabToRestore
                else -> 0
            },
            false
        )

        mPlayerControlsPanelBinding.tabLayout.getTabAt(binding.viewPager2.currentItem)?.run {
            select()
            icon?.setTint(Theming.resolveThemeColor(resources))
        }

    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        override fun getItemCount() = mPreferences.activeTabs.toList().size
        override fun createFragment(position: Int): Fragment = handleOnNavigationItemSelected(position)
    }

    override fun onAppearanceChanged(isThemeChanged: Boolean) {
        if (isThemeChanged) {
            AppCompatDelegate.setDefaultNightMode(
                Theming.getDefaultNightMode(this)
            )
            return
        }
        Theming.applyChanges(this, binding.viewPager2.currentItem)
    }

    override fun onOpenNewDetailsFragment() {
    }

    override fun onArtistOrFolderSelected(artistOrFolder: String, launchedBy: String) {
    }

    override fun onFavoritesUpdated(clear: Boolean) {
    }

    override fun onFavoriteAddedOrRemoved() {
    }

    override fun onCloseActivity() {
        if (mMediaPlayerHolder.isPlaying) {
            Dialogs.stopPlaybackDialog(this)
            return
        }
        finishAndRemoveTask()
    }

    override fun onAddToFilter(stringsToFilter: List<String>?) {
    }

    override fun onFiltersCleared() {
    }

    override fun onDenyPermission() {
    }

    override fun onOpenPlayingArtistAlbum() {
    }

    override fun onOpenEqualizer() {
    }

    override fun onOpenSleepTimerDialog() {
        if (mSleepTimerDialog == null) {
            mSleepTimerDialog = RecyclerSheet.newInstance(if (mMediaPlayerHolder.isSleepTimer) {
                RecyclerSheet.SLEEPTIMER_ELAPSED_TYPE
            } else {
                RecyclerSheet.SLEEPTIMER_TYPE
            }).apply {
                show(supportFragmentManager, RecyclerSheet.TAG_MODAL_RV)
                onSleepTimerEnabled = { enabled, value ->
                    updateSleepTimerIcon(isEnabled = enabled)
                    if (enabled) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.sleeptimer_enabled, value),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                onSleepTimerDialogCancelled = {
                    mSleepTimerDialog = null
                }
            }
        }
    }

    private fun updateSleepTimerIcon(isEnabled: Boolean) {
        mArtistsFragment?.tintSleepTimerIcon(enabled = isEnabled)
        mAlbumsFragment?.tintSleepTimerIcon(enabled = isEnabled)
        mAllMusicFragment?.tintSleepTimerIcon(enabled = isEnabled)
        mFoldersFragment?.tintSleepTimerIcon(enabled = isEnabled)
    }

    override fun onEnableEqualizer() {
    }

    override fun onUpdateSortings() {
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        com.dhp.musicplayer.utils.Log.d("onUpdateProgressViews: $progress - $total")
        if (mPlayerControlsPanelBinding.songProgress.max != total) {
            mPlayerControlsPanelBinding.songProgress.max = total
        }
        mPlayerControlsPanelBinding.songProgress.setProgressCompat(progress, true)
        nowPlayingFragment?.onUpdateProgressViews(progress, total)
    }

}