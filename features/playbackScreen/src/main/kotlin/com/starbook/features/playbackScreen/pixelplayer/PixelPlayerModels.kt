package com.starbook.features.playbackScreen.pixelplayer

import androidx.compose.runtime.Immutable
import android.net.Uri

@Immutable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long = 0,
    val albumArtUriString: String? = null,
    val path: String = "",
    val contentUriString: String = ""
) {
    val displayArtist: String get() = artist
}

@Immutable
data class Artist(
    val id: Long,
    val name: String
)

@Immutable
data class Lyrics(
    val plain: String = "",
    val synced: List<Line> = emptyList()
) {
    data class Line(val time: Long, val text: String)
}
