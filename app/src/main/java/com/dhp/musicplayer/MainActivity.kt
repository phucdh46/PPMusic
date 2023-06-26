package com.dhp.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
import com.dhp.musicplayer.ui.now_playing.NowPlaying
import com.dhp.musicplayer.utils.Dialogs
import com.dhp.musicplayer.utils.MusicUtils
import com.dhp.musicplayer.utils.Permissions
import com.dhp.musicplayer.utils.Theming
import java.util.concurrent.ScheduledExecutorService

class MainActivity : BaseActivity<ActivityMainBinding>(), MediaControlInterface, UIControlInterface {
    private lateinit var mPlayerService: PlayerService
    private var sBound = false
//    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mMusicViewModel: MusicViewModel by viewModels()
    private lateinit var mBindingIntent: Intent
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    private var mAllMusicFragment: AllMusicFragment? = null
    private lateinit var mPlayerControlsPanelBinding: PlayerControlsPanelBinding

    // Preferences
    private val mPreferences get() = Preferences.getPrefsInstance()

    // Sleep timer dialog
    private var mSleepTimerDialog: RecyclerSheet? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Log.d("DDD","onServiceConnected")
            // get bound service and instantiate MediaPlayerHolder
            val binder = service as PlayerService.LocalBinder
            mPlayerService = binder.getService()
            sBound = true

            mMediaPlayerHolder.mediaPlayerInterface = mMediaPlayerInterface

            // load music and setup UI
            mMusicViewModel.getDeviceMusic()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            sBound = false
        }
    }

    private var mNpDialog: NowPlaying? = null
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
            mPlayerControlsPanelBinding.songProgress.setProgressCompat(position, true)
            mNpDialog?.updateProgress(position)
        }

        override fun onStateChanged() {
            updatePlayingStatus()
            mNpDialog?.updatePlayingStatus()
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

        val selectedSong = mMediaPlayerHolder.currentSongFM ?: mMediaPlayerHolder.currentSong

        mPlayerControlsPanelBinding.songProgress.progress = 0
        mPlayerControlsPanelBinding.songProgress.max = selectedSong?.duration!!.toInt()

        updatePlayingSongTitle(selectedSong)

        mPlayerControlsPanelBinding.playingArtist.text =
            getString(R.string.artist_and_album, selectedSong.artist, selectedSong.album)

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

    private fun updatePlayingSongTitle(currentSong: Music) {
        var songTitle = currentSong.title
        //if (GoPreferences.getPrefsInstance().songsVisualization == GoConstants.FN) {
//        songTitle = currentSong.displayName.toFilenameWithoutExtension()
        //}
        mPlayerControlsPanelBinding.playingSong.text = songTitle
    }

    private fun doBindService() {
        Log.d("DDD","doBindService")
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
        //setTheme(newTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            binding.root.applyEdgeToEdge()
        }
        mPlayerControlsPanelBinding = PlayerControlsPanelBinding.bind(binding.root)

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
                    preparePlayback(restoredSong)
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
        mPlayerControlsPanelBinding.playPauseButton.setOnClickListener { mMediaPlayerHolder.resumeOrPause() }

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
            mNpDialog == null) {
            mNpDialog = NowPlaying.newInstance().apply {
                show(supportFragmentManager, null)
                onNowPlayingCancelled = {
                    mNpDialog = null
                }
            }
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
        super.onDestroy()
        if (!mMediaPlayerHolder.isPlaying && ::mPlayerService.isInitialized && mPlayerService.isRunning && !mMediaPlayerHolder.isSongFromPrefs) {
            Log.d("DHP","onDestroy")
            ServiceCompat.stopForeground(mPlayerService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopService(mBindingIntent)
            if (sBound) unbindService(connection)
        }
    }

    override fun onSongSelected(song: Music?, songs: List<Music>?, songLaunchedBy: String) {
        with(mMediaPlayerHolder) {
            if (!isPlay) isPlay = true
            if (isSongFromPrefs) isSongFromPrefs = false

            val albumSongs = songs ?: MusicUtils.getAlbumSongs(
                song?.artist,
                song?.album,
                mMusicViewModel.deviceAlbumsByArtist
            )
            updateCurrentSong(song, albumSongs, songLaunchedBy)

        }
        preparePlayback(song)
    }

    private fun preparePlayback(song: Music?) {
        if (::mPlayerService.isInitialized && !mPlayerService.isRunning) {
            Log.d("DDD","preparePlayback : startService")
            startService(mBindingIntent)
        }
        mMediaPlayerHolder.initMediaPlayer(song, forceReset = false)
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
        mPlayerControlsPanelBinding.songProgress.setProgressCompat(position, true)
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

    override fun onAppearanceChanged(isThemeChanged: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onOpenNewDetailsFragment() {
        TODO("Not yet implemented")
    }

    override fun onArtistOrFolderSelected(artistOrFolder: String, launchedBy: String) {
        TODO("Not yet implemented")
    }

    override fun onFavoritesUpdated(clear: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onFavoriteAddedOrRemoved() {
        TODO("Not yet implemented")
    }

    override fun onCloseActivity() {
        if (mMediaPlayerHolder.isPlaying) {
            Dialogs.stopPlaybackDialog(this)
            return
        }
        finishAndRemoveTask()
    }

    override fun onAddToFilter(stringsToFilter: List<String>?) {
        TODO("Not yet implemented")
    }

    override fun onFiltersCleared() {
        TODO("Not yet implemented")
    }

    override fun onDenyPermission() {
        TODO("Not yet implemented")
    }

    override fun onOpenPlayingArtistAlbum() {
        TODO("Not yet implemented")
    }

    override fun onOpenEqualizer() {
        TODO("Not yet implemented")
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
//                    updateSleepTimerIcon(isEnabled = enabled)
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

    override fun onEnableEqualizer() {
        TODO("Not yet implemented")
    }

    override fun onUpdateSortings() {
        TODO("Not yet implemented")
    }

}