package com.dhp.musicplayer.core.services.player

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.dhp.musicplayer.core.common.extensions.toContentUri
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.R
import com.dhp.musicplayer.core.services.download.DownloadUtil
import com.dhp.musicplayer.core.services.extensions.asMediaItem
import com.dhp.musicplayer.core.services.extensions.toggleRepeatMode
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.plus
import javax.inject.Inject

@OptIn(UnstableApi::class)
class MediaLibrarySessionCallback
@Inject constructor(
    @ApplicationContext val context: Context,
    val musicRepository: MusicRepository,
    val networkMusicRepository: NetworkMusicRepository,
    val downloadUtil: DownloadUtil,
) : MediaLibraryService.MediaLibrarySession.Callback {
    private val scope = CoroutineScope(Dispatchers.Main) + Job()
    var toggleLike: () -> Unit = {}
    private var searchTempList = mutableListOf<Song>()

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        return MediaSession.ConnectionResult.accept(
            connectionResult.availableSessionCommands.buildUpon()
                .add(MediaSessionConstants.CommandToggleLike)
                .add(MediaSessionConstants.CommandToggleRepeatMode)
                .build(),
            connectionResult.availablePlayerCommands
        )
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            MediaSessionConstants.ACTION_TOGGLE_LIKE -> toggleLike()
            MediaSessionConstants.ACTION_TOGGLE_REPEAT_MODE -> session.player.toggleRepeatMode()
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(
            LibraryResult.ofItem(
                MediaItem.Builder()
                    .setMediaId(PlaybackService.ROOT)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsPlayable(false)
                            .setIsBrowsable(false)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                            .build()
                    )
                    .build(),
                params
            )
        )
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return scope.future(Dispatchers.IO) {
            LibraryResult.ofItemList(
                when (parentId) {
                    PlaybackService.ROOT -> listOf(
                        browsableMediaItem(
                            PlaybackService.SONG,
                            context.getString(R.string.songs),
                            null,
                            drawableUri(R.drawable.music_note),
                            MediaMetadata.MEDIA_TYPE_PLAYLIST
                        ),
                        browsableMediaItem(
                            PlaybackService.PLAYLIST,
                            context.getString(R.string.playlists),
                            null,
                            drawableUri(R.drawable.playlist),
                            MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS
                        )
                    )

                    PlaybackService.SONG -> musicRepository.getSongsAndroidAuto().first()
                        .map { it.toMediaItem(parentId) }

                    PlaybackService.PLAYLIST -> {
                        val likedSongCount = musicRepository.favorites().first().size
                        val downloadedSongCount = downloadUtil.downloads.value.size
                        listOf(
                            browsableMediaItem(
                                "${PlaybackService.PLAYLIST}/${PlaybackService.LIKED}",
                                context.getString(R.string.liked_songs),
                                context.resources.getQuantityString(
                                    R.plurals.n_song,
                                    likedSongCount,
                                    likedSongCount
                                ),
                                drawableUri(R.drawable.favorite),
                                MediaMetadata.MEDIA_TYPE_PLAYLIST
                            ),
                            browsableMediaItem(
                                "${PlaybackService.PLAYLIST}/${PlaybackService.DOWNLOADED}",
                                context.getString(R.string.downloaded_songs),
                                context.resources.getQuantityString(
                                    R.plurals.n_song,
                                    downloadedSongCount,
                                    downloadedSongCount
                                ),
                                drawableUri(R.drawable.download),
                                MediaMetadata.MEDIA_TYPE_PLAYLIST
                            )
                        ) + musicRepository.getAllPlaylistWithSongs().first().map { playlist ->
                            browsableMediaItem(
                                "${PlaybackService.PLAYLIST}/${playlist.playlist.id}",
                                playlist.playlist.name,
                                context.resources.getQuantityString(
                                    R.plurals.n_song,
                                    playlist.songs.size,
                                    playlist.songs.size
                                ),
                                playlist.songs.firstOrNull()?.thumbnailUrl?.toUri(),
                                MediaMetadata.MEDIA_TYPE_PLAYLIST
                            )
                        }
                    }

                    else -> when {
                        parentId.startsWith("${PlaybackService.PLAYLIST}/") ->
                            when (val playlistId =
                                parentId.removePrefix("${PlaybackService.PLAYLIST}/")) {
                                PlaybackService.LIKED -> musicRepository.favorites()
                                PlaybackService.DOWNLOADED -> {
                                    val downloads = downloadUtil.downloads.value
                                    musicRepository.getSongsAndroidAuto()
                                        .flowOn(Dispatchers.IO)
                                        .map { songs ->
                                            songs.filter {
                                                downloads[it.id]?.state == Download.STATE_COMPLETED
                                            }
                                        }
                                        .map { songs ->
                                            songs.map { it to downloads[it.id] }
                                                .sortedBy { it.second?.updateTimeMs ?: 0L }
                                                .map { it.first }
                                        }
                                }

                                else -> musicRepository.playlistWithSongs(playlistId.toLong())
                                    .map { it?.songs ?: emptyList() }
                            }.first().map {
                                it.toMediaItem(parentId)
                            }

                        else -> emptyList()
                    }
                },
                params
            )
        }
    }

    private fun browsableMediaItem(
        id: String,
        title: String,
        subtitle: String?,
        iconUri: Uri?,
        mediaType: Int = MediaMetadata.MEDIA_TYPE_MUSIC
    ) =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setArtist(subtitle)
                    .setArtworkUri(iconUri)
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .setMediaType(mediaType)
                    .build()
            )
            .build()

    private fun Song.toMediaItem(path: String) = MediaItem.Builder()
        .setMediaId("$path/$id")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(artistsText)
                .setArtist(artistsText)
                .setArtworkUri(
                    if (isOffline) id.toLongOrNull()?.toContentUri() else thumbnailUrl?.toUri()
                )
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .build()
        ).build()

    private fun drawableUri(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.resources.getResourcePackageName(id))
        .appendPath(context.resources.getResourceTypeName(id))
        .appendPath(context.resources.getResourceEntryName(id))
        .build()

    override fun onGetItem(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return scope.future(Dispatchers.IO) {
            musicRepository.song(mediaId).first()?.asMediaItem()?.let {
                LibraryResult.ofItem(it, null)
            } ?: LibraryResult.ofError(LibraryResult.RESULT_ERROR_UNKNOWN)
        }
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return scope.future {
            val defaultResult =
                MediaSession.MediaItemsWithStartPosition(emptyList(), startIndex, startPositionMs)
            val path = mediaItems.firstOrNull()?.mediaId?.split("/")
                ?: return@future defaultResult
            when (path.firstOrNull()) {
                PlaybackService.SONG -> {
                    val songId = path.getOrNull(1) ?: return@future defaultResult
                    val allSongs = musicRepository.getSongsAndroidAuto().first()
                    MediaSession.MediaItemsWithStartPosition(
                        allSongs.map { it.asMediaItem() },
                        allSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0,
                        startPositionMs
                    )
                }

                PlaybackService.SEARCH -> {
                    val songId = path.getOrNull(1) ?: return@future defaultResult
                    val allSongs = musicRepository.getSongsAndroidAuto().first()
                    if (allSongs.find { it.id == songId } == null) {
                        val song = searchTempList.find { it.id == songId }

                        song ?: return@future defaultResult
                        scope.future(Dispatchers.IO) {
                            musicRepository.insert(song)
                        }
                        val newAllSongs = allSongs.toMutableList()
                        newAllSongs.add(0, song)
                        MediaSession.MediaItemsWithStartPosition(
                            newAllSongs.map { it.asMediaItem() },
                            newAllSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0,
                            startPositionMs,
                        )
                    } else {
                        MediaSession.MediaItemsWithStartPosition(
                            allSongs.map { it.asMediaItem() },
                            allSongs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0,
                            startPositionMs
                        )
                    }
                }

                PlaybackService.PLAYLIST -> {
                    val songId = path.getOrNull(2) ?: return@future defaultResult
                    val playlistId = path.getOrNull(1) ?: return@future defaultResult
                    val songs = when (playlistId) {
                        PlaybackService.LIKED -> musicRepository.favorites()
                        PlaybackService.DOWNLOADED -> {
                            val downloads = downloadUtil.downloads.value
                            musicRepository.getSongsAndroidAuto()
                                .flowOn(Dispatchers.IO)
                                .map { songs ->
                                    songs.filter {
                                        downloads[it.id]?.state == Download.STATE_COMPLETED
                                    }
                                }
                                .map { songs ->
                                    songs.map { it to downloads[it.id] }
                                        .sortedBy { it.second?.updateTimeMs ?: 0L }
                                        .map { it.first }
                                }
                        }

                        else -> musicRepository.playlistWithSongs(playlistId.toLong()).map { list ->
                            list?.songs ?: emptyList()
                        }
                    }.first()
                    MediaSession.MediaItemsWithStartPosition(
                        songs.map { it.asMediaItem() },
                        songs.indexOfFirst { it.id == songId }.takeIf { it != -1 } ?: 0,
                        startPositionMs
                    )
                }

                else -> defaultResult
            }
        }
    }

    override fun onSearch(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> =
        scope.future(Dispatchers.IO) {
            val searchResult = networkMusicRepository.getSearchResultAndroidAuto(
                query = query
            )
            if (searchResult != null) {
                searchTempList.clear()
                searchTempList.addAll(searchResult)
                session.notifySearchResultChanged(browser, query, searchResult.size, params)
            }
            LibraryResult.ofVoid()
        }

    override fun onGetSearchResult(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return scope.future(Dispatchers.IO) {
            LibraryResult.ofItemList(
                searchTempList.map {
                    it.toMediaItem(PlaybackService.SEARCH)
                },
                params,
            )
        }
    }
}

object MediaSessionConstants {
    const val ACTION_TOGGLE_LIKE = "TOGGLE_LIKE"
    const val ACTION_TOGGLE_REPEAT_MODE = "TOGGLE_REPEAT_MODE"
    val CommandToggleLike = SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY)
    val CommandToggleRepeatMode = SessionCommand(ACTION_TOGGLE_REPEAT_MODE, Bundle.EMPTY)
}