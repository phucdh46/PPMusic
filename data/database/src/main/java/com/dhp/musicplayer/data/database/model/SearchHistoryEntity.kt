package com.dhp.musicplayer.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchHistory")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long
)
