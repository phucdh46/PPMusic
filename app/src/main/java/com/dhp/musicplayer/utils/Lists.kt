package com.dhp.musicplayer.utils

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.toFormattedDuration
import com.dhp.musicplayer.model.Music
import java.util.*

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

    @JvmStatic
    fun processQueryForMusic(query: String?, musicList: List<Music>?): List<Music>? {
        // In real app you'd have it instantiated just once
        val filteredSongs = mutableListOf<Music>()
//        val isShowDisplayName = GoPreferences.getPrefsInstance().songsVisualization== GoConstants.FN
        return try {
            // Case insensitive search
            musicList?.iterator()?.let { iterate ->
                while (iterate.hasNext()) {
                    val filteredSong = iterate.next()
                    val toFilter =
//                        if (isShowDisplayName) {
//                        filteredSong.displayName
//                    } else {
                        filteredSong.title
//                    }
                    if (toFilter?.lowercase()!!.contains(query?.lowercase()!!)) {
                        filteredSongs.add(filteredSong)
                    }
                }
            }
            return filteredSongs
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSortedList(id: Int, list: MutableList<String>?) = when (id) {
        Constants.ASCENDING_SORTING -> list?.apply {
            Collections.sort(this, String.CASE_INSENSITIVE_ORDER)
        }
        Constants.DESCENDING_SORTING -> list?.apply {
            Collections.sort(this, String.CASE_INSENSITIVE_ORDER)
        }?.asReversed()
        else -> list
    }

    fun getSortedListWithNull(id: Int, list: MutableList<String?>?): MutableList<String>? {
        val withoutNulls = list?.map {
            transformNullToEmpty(it)
        }?.toMutableList()
        return getSortedList(id, withoutNulls)
    }

    private fun transformNullToEmpty(toTrans: String?): String {
        if (toTrans == null) return ""
        return toTrans
    }

    fun getSelectedSorting(sorting: Int, menu: Menu): MenuItem {
        return when (sorting) {
            Constants.ASCENDING_SORTING -> menu.findItem(R.id.ascending_sorting)
            Constants.DESCENDING_SORTING -> menu.findItem(R.id.descending_sorting)
            else -> menu.findItem(R.id.default_sorting)
        }
    }

    fun processQueryForStringsLists(query: String?, list: List<String>?): List<String>? {
        // In real app you'd have it instantiated just once
        val filteredStrings = mutableListOf<String>()

        return try {
            // Case insensitive search
            list?.iterator()?.let { iterate ->
                while (iterate.hasNext()) {
                    val filteredString = iterate.next()
                    if (filteredString.lowercase().contains(query?.lowercase()!!)) {
                        filteredStrings.add(filteredString)
                    }
                }
            }
            return filteredStrings
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}