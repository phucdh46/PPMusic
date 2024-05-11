package com.dhp.musicplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dhp.musicplayer.Constants.Companion.EXTRA_PLAYLISTS
import com.dhp.musicplayer.MainViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.databinding.DialogCreatePlaylistBinding
import com.dhp.musicplayer.extensions.extraNotNull
import com.dhp.musicplayer.models.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RenamePlaylistDialog : DialogFragment() {
    private var _binding: DialogCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel by activityViewModels<MainViewModel>()

    companion object {
        fun create(playlist: Playlist): RenamePlaylistDialog {
            return RenamePlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_PLAYLISTS to playlist
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlist = extraNotNull<Playlist>(EXTRA_PLAYLISTS).value
        _binding = DialogCreatePlaylistBinding.inflate(layoutInflater)
        binding.actionNewPlaylist.setText(playlist.name)
        return MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.rename_playlist_title)
            .setView(binding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_rename) { _, _ ->
                val name = binding.actionNewPlaylist.text.toString()
                if (name.isNotEmpty()) {
                    mainViewModel.renameRoomPlaylist(playlist, name)
                } else {
                    binding.actionNewPlaylistContainer.error = "Playlist name should'nt be empty"
                }
            }
            .create()
    }
}
