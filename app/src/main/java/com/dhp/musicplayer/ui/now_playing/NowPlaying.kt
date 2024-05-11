package com.dhp.musicplayer.ui.now_playing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.MainViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseBottomSheetDialogFragment
import com.dhp.musicplayer.databinding.BottomSheetFragmentNowPlayingBinding
import com.dhp.musicplayer.databinding.NowPlayingControlsBinding
import com.dhp.musicplayer.databinding.NowPlayingCoverBinding
import com.dhp.musicplayer.dialogs.AddToPlaylistDialog
import com.dhp.musicplayer.dialogs.RecyclerSheet
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.extensions.*
import com.dhp.musicplayer.player.*
import com.dhp.musicplayer.utils.Log
import com.dhp.musicplayer.utils.Theming
import com.dhp.musicplayer.utils.forceSeekToNext
import com.dhp.musicplayer.utils.forceSeekToPrevious
import com.dhp.musicplayer.utils.getRepeatMode
import com.dhp.musicplayer.utils.preferences
import com.dhp.musicplayer.utils.putRepeatMode
import com.dhp.musicplayer.utils.queueLoopEnabledKey
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch

class NowPlaying : BaseBottomSheetDialogFragment<BottomSheetFragmentNowPlayingBinding>(),
    CustomButtonClickListener{

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var mMediaControlInterface: MediaControlInterface
    private lateinit var mUIControlInterface: UIControlInterface
    private lateinit var _npCoverBinding: NowPlayingCoverBinding
    private lateinit var _npControlsBinding: NowPlayingControlsBinding

    var onNowPlayingCancelled: (() -> Unit)? = null
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    private var playConnection: PlayerConnection? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): BottomSheetFragmentNowPlayingBinding {
        return BottomSheetFragmentNowPlayingBinding.inflate(layoutInflater, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
//            mMediaControlInterface = activity as MediaControlInterface
            mUIControlInterface = activity as UIControlInterface
            playConnection = (activity as? MainActivity)?.playerConnection
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun initUI() {
        super.initUI()
        val bundle = arguments
        val progress = bundle?.getInt("progress") ?: 0
        val max = bundle?.getInt("max") ?: 1
//        onUpdateProgressViews(progress, max)
        binding.apply {
            _npCoverBinding = NowPlayingCoverBinding.bind(root)
            _npControlsBinding = NowPlayingControlsBinding.bind(root)
        }
        setupView()

        Log.d("bindUI: ${requireContext().preferences.getRepeatMode(queueLoopEnabledKey)}")
        _npControlsBinding.repeatBtn.setState(
            requireContext().preferences.getRepeatMode(
                queueLoopEnabledKey
            )
        )
        _npControlsBinding.repeatBtn.setCustomButtonClickListener(this)
        _npControlsBinding.npQueue.safeClickListener {
            openQueueFragment()
        }
        binding.imgMenu.setOnClickListener { openPlaylistFragment() }
    }

    private fun openPlaylistFragment() {
        val fragmentManager = (activity as? MainActivity)?.supportFragmentManager ?: return
        val song = playConnection?.currentMediaItem?.value?.toSong() ?: return
        Log.d("openPlaylistFragment: $song")
        mainViewModel.playlist.observe(viewLifecycleOwner) {
            AddToPlaylistDialog.create(it, song)
                .show(fragmentManager, "ADD_PLAYLIST")
        }


    }

    private fun openQueueFragment() {
        val fragmentManager = (activity as? MainActivity)?.supportFragmentManager ?: return
         RecyclerSheet.newInstance(RecyclerSheet.QUEUE_TYPE).apply {
            show(fragmentManager, RecyclerSheet.TAG_MODAL_RV)
            onQueueCancelled = {}
        }
    }

    private fun setupView() {
        binding.run {
            npSong.isSelected = true
            npArtistAlbum.isSelected = true
            setupNPCoverLayout()
        }

        _npControlsBinding.run {
            npSkipPrev.setOnClickListener { playConnection?.player?.forceSeekToPrevious() }
            //npFastRewind.setOnClickListener { mMediaPlayerHolder.fastSeek(isForward = false) }
            npPlay.setOnClickListener {
                Log.d("NowPlay OnClick: ${playConnection == null}")

                playConnection?.playOrPause()
            }
            npSkipNext.setOnClickListener { playConnection?.player?.forceSeekToNext() }
            //npFastForward.setOnClickListener { mMediaPlayerHolder.fastSeek(isForward = true) }
        }
        updateNpInfo()
        setUpProgressSlider()
        playConnection?.player?.let {
//            currentMediaItem.value.let { song ->

                //loadNpCover(song)
//                binding.npSeek.text = it.currentPosition.toFormattedDuration(isAlbum = false, isSeekBar = true)
//                binding.npSeekBar.progress = it.currentPosition.toInt()
                dialog.applyFullHeightDialog(requireActivity())
//            }
        }
    }

    private fun setupNPCoverLayout() {
        if (!Theming.isDeviceLand(resources)) {
            binding.npArtistAlbum.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            binding.npSong.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        }

        _npCoverBinding.npCover.doOnPreDraw {
            val ratio = ResourcesCompat.getFloat(resources, R.dimen.cover_ratio)
            val dim = (it.width * ratio).toInt()
            it.layoutParams = LinearLayout.LayoutParams(dim, dim)
        }

        mMediaPlayerHolder.let { mph ->
            _npCoverBinding.run {


                npCover.background.alpha = Theming.getAlbumCoverAlpha(requireContext())
                //npSaveTime.setOnClickListener { saveSongPosition() }


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

            }
        }
    }

    private fun setUpProgressSlider() {
        val progressSlider = binding.progressSlider
        val progressViewUpdateHelper = (activity as? MainActivity)?.progressViewUpdateHelper
        progressSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            Log.d("progressSlider OnChange: $value - $fromUser")
            if (fromUser) {
                (activity as? MainActivity)?.onUpdateProgressViews(
                    value.toInt(),
                    playConnection?.player?.duration?.toInt() ?: 0
                )
            }
        })
        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                Log.d("progressSlider onStartTrackingTouch")
                progressViewUpdateHelper?.stop()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Log.d("progressSlider onStopTrackingTouch: ${slider.value}")
                playConnection?.player?.seekTo(slider.value.toLong())
                progressViewUpdateHelper?.start(playConnection)
            }
        })
    }


    private fun updateNpInfo() {

        lifecycleScope.launch {
            playConnection?.currentMediaItem?.collect { mediaItem ->
                binding.npSong.text = mediaItem?.mediaMetadata?.title
                binding.npArtistAlbum.text = mediaItem?.mediaMetadata?.artist
                val songDuration = playConnection?.player?.duration ?: 0
                binding.npDuration.text = songDuration.toFormattedDuration(isAlbum = false, isSeekBar = true)
            }
        }
        updatePlayingStatus()

//        if (::mMediaControlInterface.isInitialized) {
//            with(mMediaPlayerHolder) {
                (playConnection?.currentMediaItem?.value)?.let { song ->
                    // load album cover
//                    if (mAlbumIdNp != song.albumId && mGoPreferences.isCovers) {
//                        loadNpCover(song)
//                    }

//                    binding.npSong.text = song.mediaMetadata.title
//                    binding.npArtistAlbum.text = song.mediaMetadata.artist

                    // load song's duration
//                    val songDuration = playConnection?.player?.duration ?: 0
//                    binding.npDuration.text = songDuration.toFormattedDuration(isAlbum = false, isSeekBar = true)
//                    binding.npSeekBar.max = selectedSongDuration.toInt()


//                    song.id?.toContentUri()?.toBitrate(requireContext())?.let { (first, second) ->
//
//                    }
                }
//            }
//        }
    }

    fun updatePlayingStatus() {
        lifecycleScope.launch {
            playConnection?.isPlaying?.collect {
                Log.d("NowPlay isPlaying: $it")
                _npControlsBinding.npPlay.setImageResource(
                    if (it) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        }
    }

    override fun onStateChange(newState: RepeatMode) {
        Log.d("onStateChange: $newState")
        requireContext().preferences.putRepeatMode(queueLoopEnabledKey, newState)
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

    fun onUpdateProgressViews(progress: Int, total: Int) {
        val progressSlider = binding.progressSlider
        if (progressSlider.valueTo != total.toFloat()) {
            progressSlider.valueTo = total.toFloat()
            binding.npDuration.text = total.toLong().toFormattedDuration(isAlbum = false, isSeekBar = true)
        }

        progressSlider.value = progress.toFloat().coerceIn(progressSlider.valueFrom, progressSlider.valueTo)
        binding.npSeek.text = progress.toLong().toFormattedDuration(isAlbum = false, isSeekBar = true)

    }
}