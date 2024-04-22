package com.dhp.musicplayer.ui.local_music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.R
import com.dhp.musicplayer.databinding.MusicItemBinding
import com.dhp.musicplayer.extensions.toFormattedDate
import com.dhp.musicplayer.extensions.toFormattedDuration
import com.dhp.musicplayer.extensions.toName
import com.dhp.musicplayer.model.Music

interface LocalSongClickListener {
    fun onClick(music: Music?)
}

class LocalSongAdapter(private val localSongClickListener: LocalSongClickListener) :
    RecyclerView.Adapter<LocalSongsHolder>() {
    private var mAllMusic = arrayListOf<Music>()
    fun submitData(newList: List<Music>) {
        mAllMusic.clear()
        mAllMusic.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalSongsHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalSongsHolder(binding)
    }

    override fun getItemCount(): Int {
        return mAllMusic.size
    }

    override fun onBindViewHolder(holder: LocalSongsHolder, position: Int) {
        holder.bindItems(mAllMusic[position], mAllMusic, localSongClickListener)
    }

}

class LocalSongsHolder(private val binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bindItems(songItem: Music?, mAllMusic: List<Music>?, localSongClickListener: LocalSongClickListener) {

        with(binding) {
            val itemSong = songItem
            val formattedDuration = itemSong?.duration?.toFormattedDuration(
                isAlbum = false,
                isSeekBar = false
            )

            duration.text = itemView.context.getString(
                R.string.duration_date_added, formattedDuration,
                itemSong?.dateAdded?.toFormattedDate())
            title.text = itemSong.toName()
            subtitle.text = itemView.context.getString(R.string.artist_and_album, itemSong?.artist, itemSong?.album)

            root.setOnClickListener {
                localSongClickListener.onClick(songItem)
            }

            root.setOnLongClickListener {

                return@setOnLongClickListener true
            }
        }
    }
}