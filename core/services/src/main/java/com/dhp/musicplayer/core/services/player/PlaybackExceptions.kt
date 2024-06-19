package com.dhp.musicplayer.core.services.player

import androidx.media3.common.PlaybackException

@androidx.media3.common.util.UnstableApi
class PlayableFormatNotFoundException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)

@androidx.media3.common.util.UnstableApi
class UnplayableException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)

@androidx.media3.common.util.UnstableApi
class LoginRequiredException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)

@androidx.media3.common.util.UnstableApi
class VideoIdMismatchException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
