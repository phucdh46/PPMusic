package com.dhp.musicplayer.ui.all_music

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.*
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentAllMusicBinding
import com.dhp.musicplayer.extensions.setTitleColor
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.ui.all_music.adapter.AllMusicAdapter
import com.dhp.musicplayer.ui.all_music.adapter.AllMusicClickListener
import com.dhp.musicplayer.ui.local_music.adapter.LocalSongAdapter
import com.dhp.musicplayer.ui.local_music.adapter.LocalSongClickListener
import com.dhp.musicplayer.utils.Lists
import com.dhp.musicplayer.utils.Theming
import kotlinx.coroutines.launch

class RingBuffer<T>(val size: Int, init: (index: Int) -> T) {
    private val list = MutableList(size, init)

    private var index = 0

    fun getOrNull(index: Int): T? = list.getOrNull(index)

    fun append(element: T) = list.set(index++ % size, element)
}

@UnstableApi @Suppress("DEPRECATION")
class AllMusicFragment: BaseFragment<FragmentAllMusicBinding>(), SearchView.OnQueryTextListener {

    private val allMusicViewModel: AllMusicViewModel by activityViewModels()
    private lateinit var mMusicViewModel: MusicViewModel
    private var mAllMusic: List<Music>? = null
    private lateinit var mMediaControlInterface: MediaControlInterface
    private lateinit var mUIControlInterface: UIControlInterface

    // sorting
    private lateinit var mSortMenuItem: MenuItem
    private var mSorting = Preferences.getPrefsInstance().allMusicSorting

    private lateinit var adapterAllMusic : AllMusicAdapter
    private lateinit var localSongAdapter: LocalSongAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DHP", "AllMusicFragment onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAllMusicBinding.inflate(layoutInflater, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mMediaControlInterface = activity as MediaControlInterface
            mUIControlInterface = activity as UIControlInterface
            adapterAllMusic = AllMusicAdapter(object : AllMusicClickListener {
                override fun onClick(music: Innertube.SongItem?) {
                    music?.asMediaItem?.let {
                        Log.d("DHP","click media: ${music}")
                        val binder = (activity as? MainActivity)?.binder?: return
//                        binder.exoPlayerService.setExo1()
//                        binder.player.addMediaSource(binder.exoPlayerService.createMediaSourceFactory().createMediaSource(it))
                        binder.exoPlayerService.isOfflineSong = false
                        binder.player.forcePlay(music.asMediaItem)

//                        (activity as? MainActivity)?.binder?.player?.forcePlay(
//                            it
//                        )
                    }
                }

            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }

        localSongAdapter = LocalSongAdapter((object : LocalSongClickListener{
            override fun onClick(music: Music?) {
                if (music == null) return
                val binder = (activity as? MainActivity)?.binder?: return
                binder.player.forcePlay(music.asMediaItem)

//                music.id?.toContentUri()?.let {
//                    val id = music.id ?: return
//                    val mediaItem =  MediaItem.Builder()
//                        .setMediaId(id.toString())
//                        .setUri(it)
//                        .setCustomCacheKey(id.toString())
//                        .build()
//
//                    (activity as? MainActivity)?.binder?.player?.forcePlay(mediaItem)
//                }
            }

        }))
    }

    override fun bindUI(lifecycleOwner: LifecycleOwner) {
        super.bindUI(lifecycleOwner)

        allMusicViewModel.getRelated().observe(viewLifecycleOwner) { realtedPage ->
            Log.d("DHP","Result: $realtedPage")
            realtedPage?.let { item ->
//                item.songs?.get(0)?.let { it1 -> player.forcePlay( it1.asMediaItem) }
                val musics = item.songs?.map { it.asMusic() }
                musics?.let { adapterAllMusic.submitData(item.songs) }
            }


        }
        mMusicViewModel =
            ViewModelProvider(requireActivity())[MusicViewModel::class.java].apply {
                deviceMusic.observe(viewLifecycleOwner) { returnedMusic ->
                    if (!returnedMusic.isNullOrEmpty()) {
                        mAllMusic = //returnedMusic
                            Lists.getSortedMusicListForAllMusic(
                            mSorting,
                            mMusicViewModel.deviceMusicFiltered
                        )
                        Log.d("DHP","all fra: $returnedMusic")
                        finishSetup()
                    }
                }
            }
        val playerConnection = (activity as? MainActivity)?.playerConnection
        lifecycleScope.launch {
            Log.d("DHP","bindUI ${playerConnection == null}")
            playerConnection?.isPlaying?.collect{
                Log.d("DHP","StateFlow: $it")
            }

        }

    }

    private fun finishSetup() {
        binding.run {
            allMusicRv.adapter = adapterAllMusic
//            allMusicRv.adapter = localSongAdapter
            mAllMusic?.let { localSongAdapter.submitData(it) }
            searchToolbar.let { stb ->
                stb.inflateMenu(R.menu.menu_music_search)
                stb.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sort)
                stb.setNavigationOnClickListener {
                    mUIControlInterface.onCloseActivity()
                }
                with(stb.menu) {

                    mSortMenuItem = Lists.getSelectedSortingForMusic(mSorting, this)

                    with (findItem(R.id.action_search).actionView as SearchView) {
                        setOnQueryTextListener(this@AllMusicFragment)
                        setOnQueryTextFocusChangeListener { _, hasFocus ->
                            stb.menu.setGroupVisible(R.id.sorting, !hasFocus)
                            stb.menu.findItem(R.id.sleeptimer).isVisible = !hasFocus
                        }
                    }

                    setMenuOnItemClickListener(stb.menu)
                }
            }
        }
    }

    private fun setMenuOnItemClickListener(menu: Menu) {

        binding.searchToolbar.setOnMenuItemClickListener {

            if (it.itemId == R.id.default_sorting || it.itemId == R.id.ascending_sorting
                || it.itemId == R.id.descending_sorting || it.itemId == R.id.date_added_sorting
                || it.itemId == R.id.date_added_sorting_inv || it.itemId == R.id.artist_sorting
                || it.itemId == R.id.artist_sorting_inv || it.itemId == R.id.album_sorting
                || it.itemId == R.id.album_sorting_inv) {

                mSorting = it.order
                mAllMusic = Lists.getSortedMusicListForAllMusic(mSorting, mAllMusic)

                setMusicDataSource(mAllMusic)

                mSortMenuItem.setTitleColor(
                    Theming.resolveColorAttr(requireContext(), android.R.attr.textColorPrimary)
                )

                mSortMenuItem = Lists.getSelectedSortingForMusic(mSorting, menu).apply {
                    setTitleColor(Theming.resolveThemeColor(resources))
                }

                Preferences.getPrefsInstance().allMusicSorting = mSorting

            } else if (it.itemId != R.id.action_search) {
                mUIControlInterface.onOpenSleepTimerDialog()
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun setMusicDataSource(musicList: List<Music>?) {
//        musicList?.let { adapterAllMusic.submitData(it) }
    }

    fun tintSleepTimerIcon(enabled: Boolean) {
        binding.searchToolbar.run {
            Theming.tintSleepTimerMenuItem(this, enabled)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment AllMusicFragment.
         */
        @JvmStatic
        fun newInstance() = AllMusicFragment()
    }

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        setMusicDataSource(
            Lists.processQueryForMusic(newText,
                Lists.getSortedMusicListForAllMusic(mSorting, mMusicViewModel.deviceMusicFiltered)
            ) ?: mAllMusic)
        return false
    }

}