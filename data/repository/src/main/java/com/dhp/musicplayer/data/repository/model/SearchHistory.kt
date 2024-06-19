package com.dhp.musicplayer.data.repository.model

import com.dhp.musicplayer.core.model.music.SearchHistory
import com.dhp.musicplayer.data.database.model.SearchHistoryEntity

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