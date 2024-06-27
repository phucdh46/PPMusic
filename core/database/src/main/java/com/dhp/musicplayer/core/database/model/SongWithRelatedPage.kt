package com.dhp.musicplayer.core.database.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SongWithRelatedPage(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            SongWithSongRelated::class,
            parentColumn = "songId",
            entityColumn = "relatedSongId"
        )
    )
    val relatedSongs: List<SongEntity>?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            SongAlbumRelated::class,
            parentColumn = "songId",
            entityColumn = "albumId"
        )
    )
    val albums: List<AlbumEntity>?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(SongArtistRelated::class, parentColumn = "songId", entityColumn = "artistId")
    )
    val artists: List<ArtistEntity>?,
)