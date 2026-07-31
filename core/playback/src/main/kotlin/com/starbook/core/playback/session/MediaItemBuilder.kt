package com.starbook.core.playback.session

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MediaMetadata
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal enum class StarBookMediaType {
  AudioBook,
  AudioBookChapter,
  AudioBookRoot,
}

internal fun buildMediaItem(
  title: String,
  mediaId: MediaId,
  isPlayable: Boolean,
  browsable: Boolean,
  album: String? = null,
  artist: String? = null,
  genre: String? = null,
  sourceUri: Uri? = null,
  imageUri: Uri? = null,
  durationMs: Long? = null,
  clippingConfiguration: ClippingConfiguration = ClippingConfiguration.UNSET,
  mediaType: StarBookMediaType,
): MediaItem {
  val metadata =
    MediaMetadata.Builder()
      .setAlbumTitle(album)
      .setTitle(title)
      .setArtist(artist)
      .setGenre(genre)
      .setIsBrowsable(browsable)
      .setIsPlayable(isPlayable)
      .setArtworkUri(imageUri)
      .setDurationMs(durationMs)
      .setMediaType(
        when (mediaType) {
          StarBookMediaType.AudioBook -> MediaMetadata.MEDIA_TYPE_AUDIO_BOOK
          StarBookMediaType.AudioBookChapter -> MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER
          StarBookMediaType.AudioBookRoot -> MediaMetadata.MEDIA_TYPE_FOLDER_AUDIO_BOOKS
        },
      )
      .build()

  return MediaItem.Builder()
    .setMediaId(Json.encodeToString<MediaId>(MediaId.serializer(), mediaId))
    .setMediaMetadata(metadata)
    .setUri(sourceUri)
    .setClippingConfiguration(clippingConfiguration)
    .build()
}

fun String.toMediaIdOrNull(): MediaId? = try {
  Json.decodeFromString(MediaId.serializer(), this)
} catch (_: SerializationException) {
  null
}
