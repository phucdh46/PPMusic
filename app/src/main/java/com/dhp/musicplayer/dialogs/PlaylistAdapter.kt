package com.dhp.musicplayer.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.databinding.PlaylistItemBinding
import com.dhp.musicplayer.models.Playlist

class PlaylistDiffCallBack : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }
}

class PlaylistAdapter(
    private val onClick: (playlist: Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistHolder>(PlaylistDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        val binding = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        holder.bindItems(getItem(position), onClick)
    }

    inner class PlaylistHolder(private val binding: PlaylistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(playlist: Playlist,onClick:  (playlist: Playlist) -> Unit) {
            with(binding) {
                title.text = playlist.name
                subtitle.text = itemCount.toString()
                root.setOnClickListener {
                    onClick(playlist)
                }
            }
        }
    }
}