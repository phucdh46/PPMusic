package com.dhp.musicplayer.ui.all_music

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.*
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentAllMusicBinding
import com.dhp.musicplayer.extensions.handleViewVisibility
import com.dhp.musicplayer.extensions.setTitleColor
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.ui.all_music.adapter.AllMusicAdapter
import com.dhp.musicplayer.ui.all_music.adapter.AllMusicClickListener
import com.dhp.musicplayer.utils.Lists
import com.dhp.musicplayer.utils.Theming

class RingBuffer<T>(val size: Int, init: (index: Int) -> T) {
    private val list = MutableList(size, init)

    private var index = 0

    fun getOrNull(index: Int): T? = list.getOrNull(index)

    fun append(element: T) = list.set(index++ % size, element)
}

@UnstableApi
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
//            mMediaControlInterface = activity as MediaControlInterface
            mUIControlInterface = activity as UIControlInterface

        } catch (e: ClassCastException) {
            e.printStackTrace()
        }

    }

    private fun checkUI() {
        if (Constants.HOST.isNotEmpty()) {
            binding.allMusicRv.handleViewVisibility(true)
            binding.layoutDeveloperMode.handleViewVisibility(false)
        } else  {
            binding.allMusicRv.handleViewVisibility(false)
            binding.layoutDeveloperMode.handleViewVisibility(true)
            binding.editTextHost.setText(Constants.HOST);
            binding.editTextHeaderName.setText(Constants.HEADER_NAME);
            binding.editTextHeaderKey.setText(Constants.HEADER_KEY);
            binding.editTextHostPlayer.setText(Constants.HOST_PLAYER);
            binding.editTextHeaderMask.setText(Constants.HEADER_MASK);
            binding.editTextVisitorData.setText(Constants.visitorData);
            binding.editTextUserAgentAndroid.setText(Constants.userAgentAndroid);
            binding.editTextEmbedUrl.setText(Constants.embedUrl);
            binding.btnSubmit.setOnClickListener {
                Constants.HOST = binding.editTextHost.text.toString()
                Constants.HEADER_NAME = binding.editTextHeaderName.text.toString()
                Constants.HEADER_KEY = binding.editTextHeaderKey.text.toString()
                Constants.HOST_PLAYER = binding.editTextHostPlayer.text.toString()
                Constants.HEADER_MASK = binding.editTextHeaderMask.text.toString()
                Constants.visitorData = binding.editTextVisitorData.text.toString()
                Constants.userAgentAndroid = binding.editTextUserAgentAndroid.text.toString()
                Constants.embedUrl = binding.editTextEmbedUrl.text.toString()
                checkUI()
            }
        }
    }

    override fun initUI() {
        super.initUI()
        checkUI()
        adapterAllMusic = AllMusicAdapter(object : AllMusicClickListener {
            override fun onClick(music: Innertube.SongItem?) {
                music?.asMediaItem?.let {
                    Log.d("DHP","click media: ${music}")
                    val binder = (activity as? MainActivity)?.binder?: return
//                        binder.exoPlayerService.setExo1()
//                        binder.player.addMediaSource(binder.exoPlayerService.createMediaSourceFactory().createMediaSource(it))
                    binder.exoPlayerService.isOfflineSong = false
                    binder.player.forcePlay(music.asMediaItem)

                }
            }

        })
        finishSetup()
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






    }

    private fun finishSetup() {
        binding.run {
            allMusicRv.adapter = adapterAllMusic
//            allMusicRv.adapter = localSongAdapter
//            mAllMusic?.let { localSongAdapter.submitData(it) }
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