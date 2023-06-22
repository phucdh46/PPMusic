package com.dhp.musicplayer.ui.all_music.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.R
import com.dhp.musicplayer.databinding.MusicItemBinding
import com.dhp.musicplayer.extensions.toFormattedDate
import com.dhp.musicplayer.extensions.toFormattedDuration
import com.dhp.musicplayer.extensions.toName
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder

class SongsHolder(private val binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bindItems(itemSong: Music?, mAllMusic: List<Music>? ,mMediaControlInterface: MediaControlInterface) {

        with(binding) {

            val formattedDuration = itemSong?.duration?.toFormattedDuration(
                isAlbum = false,
                isSeekBar = false
            )

            duration.text = itemView.context.getString(
                R.string.duration_date_added, formattedDuration,
                itemSong?.dateAdded?.toFormattedDate())
            title.text = itemSong.toName()
            subtitle.text =
                itemView.context.getString(R.string.artist_and_album, itemSong?.artist, itemSong?.album)

            root.setOnClickListener {
                Log.d("DHP", "click")
                with(MediaPlayerHolder.getInstance()) {
//                    if (isCurrentSongFM) currentSongFM = null
                }
                mMediaControlInterface.onSongSelected(
                    itemSong,
                    mAllMusic,
                    Constants.ARTIST_VIEW
                )
            }

            root.setOnLongClickListener {
//                val vh = _allMusicFragmentBinding?.allMusicRv?.findViewHolderForAdapterPosition(absoluteAdapterPosition)
//                Popups.showPopupForSongs(
//                    requireActivity(),
//                    vh?.itemView,
//                    itemSong,
//                    GoConstants.ARTIST_VIEW
//                )
                return@setOnLongClickListener true
            }
        }
    }
}