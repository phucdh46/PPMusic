package com.dhp.musicplayer.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Parcelable
import android.util.Size
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhp.musicplayer.extensions.toContentUri
import kotlinx.parcelize.Parcelize
import java.io.IOException

@Parcelize
@Entity
data class Song (
    @PrimaryKey val id: String,
    val idLocal: Long = 0L,
    val title: String,
    val artistsText: String? = null,
    val durationText: String?,
    val thumbnailUrl: String?,
    val likedAt: Long? = null,
    val totalPlayTimeMs: Long = 0,
    val isOffline: Boolean = false
): Parcelable {

    fun getBitmap(context: Context): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                context.contentResolver.loadThumbnail(
                    idLocal.toContentUri(), Size(640, 480), null)
            } catch(e: IOException) {
                null
            }
        } else {
            null
        }
    }
}
