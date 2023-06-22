package com.dhp.musicplayer.ui.all_music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.dhp.musicplayer.databinding.MusicItemBinding
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface

class AllMusicAdapter(val mAllMusic: List<Music>?, val mMediaControlInterface: MediaControlInterface) : RecyclerView.Adapter<SongsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsHolder(binding)
    }

    override fun getItemCount(): Int {
        return mAllMusic?.size!!
    }

    override fun onBindViewHolder(holder: SongsHolder, position: Int) {
        holder.bindItems(mAllMusic?.get(position), mAllMusic, mMediaControlInterface)
    }

}