package com.dhp.musicplayer.ui.local_music

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.*
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentLocalMusicBinding
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.ui.local_music.adapter.LocalSongAdapter
import com.dhp.musicplayer.ui.local_music.adapter.LocalSongClickListener
import com.dhp.musicplayer.utils.Lists

@UnstableApi
class LocalMusicFragment : BaseFragment<FragmentLocalMusicBinding>() {

    private lateinit var localSongAdapter: LocalSongAdapter

    private lateinit var mMusicViewModel: MusicViewModel
    private var mAllMusic: List<Music>? = null

    private var mSorting = Preferences.getPrefsInstance().allMusicSorting

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentLocalMusicBinding.inflate(layoutInflater, container, false)

    override fun initUI() {
        super.initUI()
        localSongAdapter = LocalSongAdapter((object : LocalSongClickListener {
            override fun onClick(music: Music?) {
                if (music == null) return
                val binder = (activity as? MainActivity)?.binder?: return
                (activity as? MainActivity)?.playerConnection?.addMusicsToQueue(mAllMusic)
                binder.player.playQueue(music.asMediaItem)
            }
        }))
    }

    override fun bindUI(lifecycleOwner: LifecycleOwner) {
        super.bindUI(lifecycleOwner)
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
    }

    private fun finishSetup() {
        binding.run {
            allMusicRv.adapter = localSongAdapter
            mAllMusic?.let { localSongAdapter.submitData(it) }
            searchToolbar.let { stb ->
                stb.inflateMenu(R.menu.menu_music_search)
                stb.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sort)
                stb.setNavigationOnClickListener {
//                    mUIControlInterface.onCloseActivity()
                }
//                with(stb.menu) {
//
//                    mSortMenuItem = com.dhp.musicplayer.utils.Lists.getSelectedSortingForMusic(mSorting, this)
//
//                    with (findItem(com.dhp.musicplayer.R.id.action_search).actionView as SearchView) {
//                        setOnQueryTextListener(this@AllMusicFragment)
//                        setOnQueryTextFocusChangeListener { _, hasFocus ->
//                            stb.menu.setGroupVisible(com.dhp.musicplayer.R.id.sorting, !hasFocus)
//                            stb.menu.findItem(com.dhp.musicplayer.R.id.sleeptimer).isVisible = !hasFocus
//                        }
//                    }
//
//                    setMenuOnItemClickListener(stb.menu)
//                }
            }
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
        fun newInstance() = LocalMusicFragment()
    }

}