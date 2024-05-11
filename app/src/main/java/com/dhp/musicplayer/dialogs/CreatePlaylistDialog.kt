package com.dhp.musicplayer.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dhp.musicplayer.Constants.Companion.EXTRA_SONG
import com.dhp.musicplayer.MainViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.databinding.DialogCreatePlaylistBinding
import com.dhp.musicplayer.extensions.extra
import com.dhp.musicplayer.models.Playlist
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.dhp.musicplayer.models.Song
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreatePlaylistDialog : DialogFragment() {
    private var _binding: DialogCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel by activityViewModels<MainViewModel>()

    companion object {

        fun create(song: Song): CreatePlaylistDialog {
            return CreatePlaylistDialog().apply {
                arguments = bundleOf(EXTRA_SONG to song)
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreatePlaylistBinding.inflate(layoutInflater)

        val song: Song = extra<Song>(EXTRA_SONG).value ?: return MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.create_playlist).create()
        val playlistView: TextInputEditText = binding.actionNewPlaylist
        val playlistContainer: TextInputLayout = binding.actionNewPlaylistContainer
        return MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.create_playlist)
            .setView(binding.root)
            .setPositiveButton(
                R.string.create
            ) { _, _ ->
                val playlistName = playlistView.text.toString()
                if (!TextUtils.isEmpty(playlistName)) {
                    mainViewModel.createAndAddToPlaylist(playlistName, song)
                } else {
                    playlistContainer.error = "Playlist name can't be empty"
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}