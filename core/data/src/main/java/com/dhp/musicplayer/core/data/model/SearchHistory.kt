package com.dhp.musicplayer.core.data.model

import com.dhp.musicplayer.core.model.music.SearchHistory
import com.dhp.musicplayer.core.database.model.SearchHistoryEntity

fun SearchHistoryEntity.asExternalModel(): SearchHistory {
    return SearchHistory(
        query = query,
        timestamp = timestamp,
    )
}

fun SearchHistory.asEntity(): SearchHistoryEntity {
    return SearchHistoryEntity(
        query = query,
        timestamp = timestamp,
    )
}