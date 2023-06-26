package com.dhp.musicplayer.utils

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dhp.musicplayer.Constants
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

    @JvmStatic
    fun getSelectedSortingForMusic(sorting: Int, menu: Menu): MenuItem {
        return when (sorting) {
            Constants.ASCENDING_SORTING -> menu.findItem(R.id.ascending_sorting)
            Constants.DESCENDING_SORTING -> menu.findItem(R.id.descending_sorting)
            Constants.DATE_ADDED_SORTING -> menu.findItem(R.id.date_added_sorting)
            Constants.DATE_ADDED_SORTING_INV -> menu.findItem(R.id.date_added_sorting_inv)
            Constants.ARTIST_SORTING -> menu.findItem(R.id.artist_sorting)
            Constants.ARTIST_SORTING_INV -> menu.findItem(R.id.artist_sorting_inv)
            Constants.ALBUM_SORTING -> menu.findItem(R.id.album_sorting)
            Constants.ALBUM_SORTING_INV -> menu.findItem(R.id.album_sorting_inv)
            else -> menu.findItem(R.id.default_sorting)
        }
    }

    @JvmStatic
    fun getSortedMusicListForAllMusic(id: Int, list: List<Music>?): List<Music>? {
        return when (id) {
            Constants.ASCENDING_SORTING -> getSortedListBySelectedVisualization(list)
            Constants.DESCENDING_SORTING -> getSortedListBySelectedVisualization(list)?.asReversed()
            Constants.TRACK_SORTING -> list?.sortedBy { it.track }
            Constants.TRACK_SORTING_INVERTED -> list?.sortedBy { it.track }?.asReversed()
            Constants.DATE_ADDED_SORTING -> list?.sortedBy { it.dateAdded }?.asReversed()
            Constants.DATE_ADDED_SORTING_INV -> list?.sortedBy { it.dateAdded }
            Constants.ARTIST_SORTING -> list?.sortedBy { it.artist }
            Constants.ARTIST_SORTING_INV -> list?.sortedBy { it.artist }?.asReversed()
            Constants.ALBUM_SORTING -> list?.sortedBy { it.album }
            Constants.ALBUM_SORTING_INV -> list?.sortedBy { it.album }?.asReversed()
            else -> list
        }
    }

    private fun getSortedListBySelectedVisualization(list: List<Music>?) = list?.sortedBy {
//        if (GoPreferences.getPrefsInstance().songsVisualization == GoConstants.FN) {
//            it.displayName
//        } else {
            it.title
//        }
    }

}