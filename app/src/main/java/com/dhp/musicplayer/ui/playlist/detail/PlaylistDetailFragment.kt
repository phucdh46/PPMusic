package com.dhp.musicplayer.ui.playlist.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.MainViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseBottomSheetDialogFragment
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentLibraryBinding
import com.dhp.musicplayer.databinding.FragmentPlaylistDetailBinding
import com.dhp.musicplayer.dialogs.CreatePlaylistDialog
import com.dhp.musicplayer.dialogs.QueueAdapter
import com.dhp.musicplayer.dialogs.RenamePlaylistDialog
import com.dhp.musicplayer.extensions.extra
import com.dhp.musicplayer.extensions.playlistOptions
import com.dhp.musicplayer.models.Music
import com.dhp.musicplayer.models.Playlist
import com.dhp.musicplayer.models.PlaylistWithSongs
import com.dhp.musicplayer.models.PlaylistWithSongsPreview
import com.dhp.musicplayer.models.Song
import com.dhp.musicplayer.models.asMusic
import com.dhp.musicplayer.ui.local_music.adapter.LocalSongAdapter
import com.dhp.musicplayer.ui.local_music.adapter.LocalSongClickListener
import com.dhp.musicplayer.utils.MusicUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable

class PlaylistDetailFragment : BaseBottomSheetDialogFragment<FragmentPlaylistDetailBinding>(),
    MenuProvider {

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var playlistSongAdapter: LocalSongAdapter
    private var playlistWithSongs: PlaylistWithSongs ?= null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPlaylistDetailBinding.inflate(layoutInflater, container, false)

    override fun initUI() {
        super.initUI()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        val playlistId = extra<Long>(Constants.EXTRA_PLAYLIST_ID).value
        (activity as? MainActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.title = null
        playlistSongAdapter = LocalSongAdapter(object : LocalSongClickListener {
            override fun onClick(music: Music?) {

            }
        })
        playlistSongAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
        binding.recyclerView.apply {
            adapter = playlistSongAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        playlistId?.let {
            mainViewModel.playlistWithSongs(playlistId).observe(viewLifecycleOwner) { playlistWithSongs ->
                this.playlistWithSongs = playlistWithSongs
                playlistWithSongs ?.let {
                    Glide.with(this)
                        .load(PlaylistWithSongsPreview(playlistWithSongs))
                        .playlistOptions(requireContext())
                        .into(binding.image)
                }
                binding.title.text = playlistWithSongs?.playlist?.name
//                binding.subtitle.text = MusicUtil.getPlaylistInfoString(requireContext(), playlist.songs.toSongs())
                binding.subtitle.text = MusicUtils.getSongCountString(requireContext(),playlistWithSongs?.songs?.size ?: 0)
                binding.toolbar.title = playlistWithSongs?.playlist?.name
                playlistSongAdapter.submitData(playlistWithSongs?.songs?.map { it.asMusic() } ?: emptyList())
                binding.progressIndicator.hide()
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_playlist_detail, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.action_rename_playlist -> {
                playlistWithSongs?.let {
                    showRenameDialog(it.playlist)
                }
            }
            R.id.action_delete_playlist -> {

            }
        }
        return false
    }

    private fun showRenameDialog(playlist: Playlist) {
        RenamePlaylistDialog.create(playlist).show(requireActivity().supportFragmentManager, "Dialog")
    }


    private fun checkIsEmpty() {
        binding.empty.isVisible = playlistSongAdapter.itemCount == 0
        binding.emptyText.isVisible = playlistSongAdapter.itemCount == 0
    }

    override fun bindUI(lifecycleOwner: LifecycleOwner) {
        super.bindUI(lifecycleOwner)
        mainViewModel.playlist.observe(viewLifecycleOwner) {

        }
        mainViewModel.playlistWithSongs.observe(viewLifecycleOwner) {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(playlistId: Long) = PlaylistDetailFragment().apply {
            arguments = bundleOf(
                Constants.EXTRA_PLAYLIST_ID to playlistId
            )
        }
    }
}