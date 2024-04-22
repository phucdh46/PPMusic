package com.dhp.musicplayer.ui.all_music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.Innertube
import com.dhp.musicplayer.databinding.MusicItemBinding

interface AllMusicClickListener {
    fun onClick(music: Innertube.SongItem?)
}

//class AllMusicAdapter(private val mMediaControlInterface: MediaControlInterface) :
class AllMusicAdapter(private val allMusicClickListener: AllMusicClickListener) :
    RecyclerView.Adapter<SongsHolder>() {
    private var mAllMusic = arrayListOf<Innertube.SongItem>()
    fun submitData(newList: List<Innertube.SongItem>) {
        mAllMusic.clear()
        mAllMusic.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsHolder(binding)
    }

    override fun getItemCount(): Int {
        return mAllMusic.size
    }

    override fun onBindViewHolder(holder: SongsHolder, position: Int) {
        holder.bindItems(mAllMusic[position], mAllMusic, allMusicClickListener)
    }

}