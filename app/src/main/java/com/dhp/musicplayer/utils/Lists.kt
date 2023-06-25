package com.dhp.musicplayer.utils

import android.content.Context
import android.widget.Toast
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.toFormattedDuration
import com.dhp.musicplayer.model.Music

object Lists {
    @JvmStatic
    fun addToFavorites(
        context: Context,
        song: Music?,
        canRemove: Boolean,
        playerPosition: Int,
        launchedBy: String
    ) {
        val favorites = //GoPreferences.getPrefsInstance().favorites?.toMutableList() ?:
            mutableListOf<Music>()
        song?.copy(startFrom = playerPosition, launchedBy = launchedBy)?.let { savedSong ->
            if (!favorites.contains(savedSong)) {
                favorites.add(savedSong)

                var msg = context.getString(
                    R.string.favorite_added,
                    savedSong.title,
                    playerPosition.toLong().toFormattedDuration(
                        isAlbum = false,
                        isSeekBar = false
                    )
                )
                if (playerPosition == 0) {
                    msg = msg.replace(context.getString(R.string.favorites_no_position), "")
                }

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            } else if (canRemove) {
                favorites.remove(savedSong)
            }
            //GoPreferences.getPrefsInstance().favorites = favorites
        }
    }
}