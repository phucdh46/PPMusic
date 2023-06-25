package com.dhp.musicplayer.ui.now_playing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseBottomSheetDialogFragment
import com.dhp.musicplayer.databinding.BottomSheetFragmentNowPlayingBinding
import com.dhp.musicplayer.databinding.NowPlayingControlsBinding
import com.dhp.musicplayer.databinding.NowPlayingCoverBinding
import com.dhp.musicplayer.databinding.NowPlayingVolControlBinding
import com.dhp.musicplayer.extensions.applyFullHeightDialog
import com.dhp.musicplayer.extensions.toBitrate
import com.dhp.musicplayer.extensions.toContentUri
import com.dhp.musicplayer.extensions.toFormattedDuration
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.utils.Lists
import com.dhp.musicplayer.utils.Theming
import com.dhp.musicplayer.utils.Versioning
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NowPlaying: BaseBottomSheetDialogFragment<BottomSheetFragmentNowPlayingBinding>() {

    private lateinit var mMediaControlInterface: MediaControlInterface
    private lateinit var mUIControlInterface: UIControlInterface
    private var _npCoverBinding: NowPlayingCoverBinding? = null
    private var _npControlsBinding: NowPlayingControlsBinding? = null
    private var _npExtControlsBinding: NowPlayingVolControlBinding? = null

    var onNowPlayingCancelled: (() -> Unit)? = null
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()


    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = BottomSheetFragmentNowPlayingBinding.inflate(layoutInflater, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mMediaControlInterface = activity as MediaControlInterface
            mUIControlInterface = activity as UIControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun initUI() {
        super.initUI()
        binding.apply {
            _npCoverBinding = NowPlayingCoverBinding.bind(root)
            _npControlsBinding = NowPlayingControlsBinding.bind(root)
            _npExtControlsBinding = NowPlayingVolControlBinding.bind(root)
        }
        setupView()
    }

    private fun setupView() {
        binding.run {
            npSong.isSelected = true
            npArtistAlbum.isSelected = true
            setupNPCoverLayout()
        }

        _npControlsBinding?.run {
            npSkipPrev.setOnClickListener { skip(isNext = false) }
            //npFastRewind.setOnClickListener { mMediaPlayerHolder.fastSeek(isForward = false) }
            npPlay.setOnClickListener { mMediaPlayerHolder.resumeOrPause() }
            npSkipNext.setOnClickListener { skip(isNext = true) }
            //npFastForward.setOnClickListener { mMediaPlayerHolder.fastSeek(isForward = true) }
        }
        setupSeekBarProgressListener()
        updateNpInfo()

        with(mMediaPlayerHolder) {
            (currentSongFM ?: currentSong)?.let { song ->
                //loadNpCover(song)
                binding.npSeek.text =
                    playerPosition.toLong().toFormattedDuration(isAlbum = false, isSeekBar = true)
                binding.npSeekBar.progress = playerPosition
                dialog.applyFullHeightDialog(requireActivity())
            }
        }
    }

    private fun setupNPCoverLayout() {
        if (!Theming.isDeviceLand(resources)) {
            binding.npArtistAlbum.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            binding.npSong.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        }

        _npCoverBinding?.npCover?.doOnPreDraw {
            val ratio = ResourcesCompat.getFloat(resources, R.dimen.cover_ratio)
            val dim = (it.width * ratio).toInt()
            it.layoutParams = LinearLayout.LayoutParams(dim, dim)
        }

        mMediaPlayerHolder.let { mph ->
            _npCoverBinding?.run {
                if (Versioning.isMarshmallow()) {
//                    setupNPCoverButtonsToasts(npPlaybackSpeed)
                    npPlaybackSpeed.setOnClickListener { view ->
//                        Popups.showPopupForPlaybackSpeed(requireActivity(), view)
                    }
                } else {
                    npPlaybackSpeed.visibility = View.GONE
                }

                npCover.background.alpha = Theming.getAlbumCoverAlpha(requireContext())
                //npSaveTime.setOnClickListener { saveSongPosition() }
                npEqualizer.setOnClickListener { mUIControlInterface.onOpenEqualizer() }
                npLove.setOnClickListener {
                    Lists.addToFavorites(
                        requireContext(),
                        mph.currentSong,
                        canRemove = true,
                        0,
                        mph.launchedBy)
                    mUIControlInterface.onFavoritesUpdated(clear = false)
                    //mph.onUpdateFavorites()
                    //updateNpFavoritesIcon()
                }

//                with(npRepeat) {
//                    setImageResource(
//                        Theming.getRepeatIcon(isNotification = false)
//                    )
//                    updateIconTint(
//                        if (mph.isRepeat1X || mph.isLooping) {
//                            Theming.resolveThemeColor(resources)
//                        } else {
//                            Theming.resolveWidgetsColorNormal(requireContext())
//                        }
//                    )
//                    setOnClickListener { setRepeat() }
//                    setupNPCoverButtonsToasts(npSaveTime, npLove, npEqualizer, this)
//                }

                with (npPauseOnEnd) {
                    isChecked = true//mGoPreferences.continueOnEnd
                    setOnCheckedChangeListener { _, isChecked ->
                        //mGoPreferences.continueOnEnd = isChecked
                        var msg = R.string.pause_on_end
                        if (isChecked) msg = R.string.pause_on_end_disabled
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT)
                            .show()
                    }
                    setOnLongClickListener { switch ->
                        Toast.makeText(requireContext(), switch.contentDescription, Toast.LENGTH_SHORT)
                            .show()
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    private fun skip(isNext: Boolean) {
        with(mMediaPlayerHolder) {
            if (!isPlay) isPlay = true
            if (isSongFromPrefs) isSongFromPrefs = false
            if (isNext) {
                skip(isNext = true)
                return
            }
            skip(isNext = false)
        }
    }

    private fun setupSeekBarProgressListener() {

        mMediaPlayerHolder.let { mph ->
            binding.run {
                npSeekBar.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {

                        val defaultPositionColor = npSeek.currentTextColor
                        val selectedColor = R.color.purple_200//Theming.resolveThemeColor(resources)
                        var userSelectedPosition = 0
                        var isUserSeeking = false

                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) userSelectedPosition = progress
                            npSeek.text =
                                progress.toLong().toFormattedDuration(isAlbum = false, isSeekBar = true)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            isUserSeeking = true
                            npSeek.setTextColor(selectedColor)
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            if (isUserSeeking) {
                                npSeek.setTextColor(defaultPositionColor)
                                mph.onPauseSeekBarCallback()
                                isUserSeeking = false
                            }
                            if (mph.state != Constants.PLAYING) {
                                mMediaControlInterface.onUpdatePositionFromNP(userSelectedPosition)
                                npSeekBar.progress = userSelectedPosition
                            }
                            mph.seekTo(
                                userSelectedPosition,
                                updatePlaybackStatus = mph.isPlaying,
                                restoreProgressCallBack = !isUserSeeking
                            )
                        }
                    })
            }
        }
    }


    fun updateNpInfo() {
        if (::mMediaControlInterface.isInitialized) {
            with(mMediaPlayerHolder) {
                (currentSongFM ?: currentSong)?.let { song ->
                    // load album cover
//                    if (mAlbumIdNp != song.albumId && mGoPreferences.isCovers) {
//                        loadNpCover(song)
//                    }
                    // load album/song info
                    var songTitle = song.title
//                    if (mGoPreferences.songsVisualization == GoConstants.FN) {
//                        songTitle = song.displayName.toFilenameWithoutExtension()
//                    }
                    binding.npSong.text = songTitle
                    binding.npArtistAlbum.text =
                        getString(
                            R.string.artist_and_album,
                            song.artist,
                            song.album
                        )

                    // load song's duration
                    val selectedSongDuration = song.duration
                    binding.npDuration.text =
                        selectedSongDuration.toFormattedDuration(isAlbum = false, isSeekBar = true)
                    binding.npSeekBar.max = song.duration.toInt()

                    song.id?.toContentUri()?.toBitrate(requireContext())?.let { (first, second) ->
                        binding.npRates.text =
                            getString(R.string.rates, first, second)
                    }
                    updatePlayingStatus()
                }
            }
        }
    }

    fun updatePlayingStatus() {
        with(mMediaPlayerHolder) {
            if (isPlaying) {
                _npControlsBinding?.npPlay?.setImageResource(R.drawable.ic_pause)
                return
            }
            _npControlsBinding?.npPlay?.setImageResource(R.drawable.ic_play)
        }
    }

    fun updateProgress(position: Int) {
        binding.npSeekBar.progress = position
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onNowPlayingCancelled?.invoke()
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ModalSheet.
         */
        @JvmStatic
        fun newInstance() = NowPlaying()
    }
}