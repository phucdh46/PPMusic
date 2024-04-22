package com.dhp.musicplayer.dialogs


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.R
import com.dhp.musicplayer.databinding.QueueItemBinding
import com.dhp.musicplayer.extensions.handleViewVisibility
import com.dhp.musicplayer.player.PlayerConnection
import com.dhp.musicplayer.utils.Log
import com.dhp.musicplayer.utils.shouldBePlaying

class WindowDiffCallBack : DiffUtil.ItemCallback<Timeline.Window>() {
    override fun areItemsTheSame(oldItem: Timeline.Window, newItem: Timeline.Window): Boolean {
        return oldItem.mediaItem.mediaId == newItem.mediaItem.mediaId
    }

    override fun areContentsTheSame(oldItem: Timeline.Window, newItem: Timeline.Window): Boolean {
        return oldItem == newItem
    }
}

class QueueAdapter(private val playerConnection: PlayerConnection?) : ListAdapter<Timeline.Window, QueueAdapter.QueueHolder>(WindowDiffCallBack()) {

    private var currentSelectedPosition = 0
    var currentSelectedMediaItem: MediaItem? = null

    var onQueueCleared: (() -> Unit)? = null

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Log.d("onItemMove: $fromPosition - $toPosition")
        playerConnection?.player?.moveMediaItem(fromPosition, toPosition)
        // Notify the adapter that the item has moved
        notifyItemMoved(fromPosition, toPosition)
    }

    fun notifyCurrentSelectedItem() {
        notifyItemChanged(currentSelectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueHolder {
        val binding = QueueItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QueueHolder(binding)
    }

    override fun onBindViewHolder(holder: QueueHolder, position: Int) {
        holder.bindItems(getItem(position), playerConnection?.player)
    }

    inner class QueueHolder(private val binding: QueueItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindItems(window: Timeline.Window, player: ExoPlayer?) {
            val song = window.mediaItem
            with(binding) {
                title.text = song.mediaMetadata.title
                subtitle.text = song.mediaMetadata.artist
                player?.let { player ->
                    if (player.currentMediaItemIndex == window.firstPeriodIndex) {
                        currentSelectedPosition = absoluteAdapterPosition
                        currentSelectedMediaItem = song
                        binding.root.setBackgroundColor(Color.GRAY)
                        if (player.shouldBePlaying) {
                            binding.imgPlayOverlap.handleViewVisibility(false)
                            binding.imgPauseOverlap.handleViewVisibility(true)
                        } else {
                            binding.imgPlayOverlap.handleViewVisibility(true)
                            binding.imgPauseOverlap.handleViewVisibility(false)
                        }
                    } else {
                        binding.root.setBackgroundColor(itemView.context.getColor(R.color.bg_item_queue))
                        binding.imgPlayOverlap.handleViewVisibility(false)
                        binding.imgPauseOverlap.handleViewVisibility(false)
                    }

                    root.setOnClickListener {
                        Log.d("setOnClickListener: $absoluteAdapterPosition - ${player.currentMediaItemIndex} - ${window.firstPeriodIndex}")
                        if (player.currentMediaItemIndex == window.firstPeriodIndex) {
                            if (player.isPlaying) player.pause() else player.play()
                        } else {
                            player.seekToDefaultPosition(window.firstPeriodIndex)
                            player.prepare()
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
}
