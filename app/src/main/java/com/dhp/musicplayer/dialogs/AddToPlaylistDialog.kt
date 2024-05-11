package com.dhp.musicplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dhp.musicplayer.Constants.Companion.EXTRA_PLAYLISTS
import com.dhp.musicplayer.Constants.Companion.EXTRA_SONG
import com.dhp.musicplayer.MainViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.extra
import com.dhp.musicplayer.extensions.extraNotNull
import com.dhp.musicplayer.models.Playlist
import com.dhp.musicplayer.models.Song
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddToPlaylistDialog : DialogFragment() {
    private val mainViewModel by activityViewModels<MainViewModel>()

    companion object {

        fun create(playlistEntities: List<Playlist>?, song: Song): AddToPlaylistDialog {
            return AddToPlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_SONG to song,
                    EXTRA_PLAYLISTS to playlistEntities
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlists = extraNotNull<List<Playlist>>(EXTRA_PLAYLISTS, listOf()).value
        val song = extra<Song>(EXTRA_SONG).value ?: return MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.add_playlist_title).create()
        val playlistNames = mutableListOf<String>()
        playlistNames.add(requireContext().resources.getString(R.string.create_playlist))
        for (entity: Playlist in playlists) {
            playlistNames.add(entity.name)
        }
        return MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.add_playlist_title)
            .setItems(playlistNames.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    showCreateDialog(song)
                } else {
                    val position = which + 1
                    mainViewModel.addToPlaylist(playlists[which], song, position)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
//            .colorButtons()
    }

    private fun showCreateDialog(song: Song) {
        CreatePlaylistDialog.create(song).show(requireActivity().supportFragmentManager, "Dialog")
    }
}
