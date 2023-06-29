package com.dhp.musicplayer.extensions

import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import com.dhp.musicplayer.R
import com.dhp.musicplayer.model.Music
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


fun Long.toContentUri(): Uri = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    this
)

fun Long.toFormattedDuration(isAlbum: Boolean, isSeekBar: Boolean) = try {

    val defaultFormat = if (isAlbum) "%02dm:%02ds" else "%02d:%02d"

    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this)

    if (minutes < 60) {
        String.format(
            Locale.getDefault(), defaultFormat,
            minutes,
            seconds - TimeUnit.MINUTES.toSeconds(minutes)
        )
    } else {
        // https://stackoverflow.com/a/9027379
        when {
            isSeekBar -> String.format(
                "%02d:%02d:%02d",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours),
                seconds - TimeUnit.MINUTES.toSeconds(minutes)
            )
            else -> String.format(
                "%02dh:%02dm",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours)
            )
        }
    }

} catch (e: Exception) {
    e.printStackTrace()
    ""
}

fun Int.toFormattedDate(): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val netDate = Date(this.toLong() * 1000)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun Music?.toName(): String? {
//    if (GoPreferences.getPrefsInstance().songsVisualization == GoConstants.FN) {
//        return this?.displayName?.toFilenameWithoutExtension()
//    }
    return this?.title
}

//https://codereview.stackexchange.com/a/97819
fun String?.toFilenameWithoutExtension() = try {
    Pattern.compile("(?<=.)\\.[^.]+$").matcher(this!!).replaceAll("")
} catch (e: Exception) {
    e.printStackTrace()
    this
}

fun Long.waitForCover(context: Context, onDone: (Bitmap?, Boolean) -> Unit) {
    Coil.imageLoader(context).enqueue(
        ImageRequest.Builder(context)
            //.data(if (GoPreferences.getPrefsInstance().isCovers) toAlbumArtURI() else null)
            .target(
                onSuccess = { onDone(it.toBitmap(), false) },
                onError = { onDone(null, true) }
            )
            .build()
    )
}

fun List<Music>.findIndex(song: Music?) = indexOfFirst {
    it.id == song?.id && it.albumId == song?.albumId
}

fun Uri.toBitrate(context: Context): Pair<Int, Int>? {
    val mediaExtractor = MediaExtractor()
    return try {
        mediaExtractor.setDataSource(context, this, null)
        val mediaFormat = mediaExtractor.getTrackFormat(0)
        val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        // Get bitrate in bps, divide by 1000 to get Kbps
        val bitrate = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE) / 1000
        Pair(first = sampleRate, second = bitrate)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        mediaExtractor.release()
    }
}

fun Int.toFormattedYear(resources: Resources): String {
    if (this != 0) return toString()
    return resources.getString(R.string.unknown_year)
}

