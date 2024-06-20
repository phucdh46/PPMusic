package com.dhp.musicplayer.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchHistory")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long
)
