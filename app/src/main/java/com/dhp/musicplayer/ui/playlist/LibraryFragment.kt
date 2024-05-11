package com.dhp.musicplayer.ui.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.MainViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentLibraryBinding
import com.dhp.musicplayer.databinding.FragmentLocalMusicBinding
import com.dhp.musicplayer.dialogs.AddToPlaylistDialog
import com.dhp.musicplayer.dialogs.PlaylistAdapter
import com.dhp.musicplayer.extensions.handleViewVisibility
import com.dhp.musicplayer.models.Playlist
import com.dhp.musicplayer.ui.local_music.LocalMusicFragment
import com.dhp.musicplayer.ui.playlist.detail.PlaylistDetailFragment

class LibraryFragment : BaseFragment<FragmentLibraryBinding>() {

    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentLibraryBinding.inflate(layoutInflater, container, false)


    override fun initUI() {
        super.initUI()
        playlistAdapter = PlaylistAdapter {
            openPlaylistDetail(it)
        }
        binding.playlistRv.adapter = playlistAdapter
    }

    private fun openPlaylistDetail(playlist: Playlist) {
        val playlistDetailFragment = PlaylistDetailFragment.newInstance(playlist.id)
        val fragmentManager = (activity as? MainActivity)?.supportFragmentManager ?: return
        playlistDetailFragment.apply {
//            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
            show(fragmentManager,"PlaylistDetailFragment")
        }
    }

    override fun bindUI(lifecycleOwner: LifecycleOwner) {
        super.bindUI(lifecycleOwner)
        mainViewModel.playlist.observe(viewLifecycleOwner) {
            binding.playlistRv.handleViewVisibility(!it.isNullOrEmpty())
            binding.txtEmptyPlaylist.handleViewVisibility(it.isNullOrEmpty())
            playlistAdapter.submitList(it)
        }
    }

    companion object {
        fun newInstance() = LibraryFragment()
//        fun newInstance(param1: String, param2: String) =
//            LibraryFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
    }
}