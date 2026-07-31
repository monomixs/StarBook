package com.starbook.core.playback

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.core.data.ChapterId
import com.starbook.core.playback.session.bookId
import com.starbook.core.playback.session.positionInChapter
import com.starbook.core.playback.session.realChapterId
import com.starbook.core.playback.session.toMediaIdOrNull

data class LivePlaybackState(
  val bookId: BookId,
  val chapterId: ChapterId,
  val positionMs: Long,
  val isPlaying: Boolean,
  val playbackSpeed: Float,
  val playbackState: Int,
) {
  val isBuffering: Boolean get() = playbackState == Player.STATE_BUFFERING
}

internal fun MediaController.livePlaybackStateSnapshot(bookId: BookId? = null): LivePlaybackState? {
  val mediaId = currentMediaItem?.mediaId?.toMediaIdOrNull() ?: return null
  val mediaItemBookId = mediaId.bookId ?: return null
  if (bookId != null && mediaItemBookId != bookId) {
    return null
  }
  val positionMs = currentPosition.takeUnless { it == C.TIME_UNSET || it < 0 } ?: return null
  val chapterId = mediaId.realChapterId ?: return null
  val positionInChapter = mediaId.positionInChapter(positionMs) ?: return null
  return LivePlaybackState(
    bookId = mediaItemBookId,
    chapterId = chapterId,
    positionMs = positionInChapter,
    isPlaying = isPlaying,
    playbackSpeed = playbackParameters.speed,
    playbackState = playbackState,
  )
}

fun Book.overlay(livePlaybackState: LivePlaybackState): Book {
  return if (livePlaybackState.bookId == id) {
    update {
      it.copy(
        currentChapter = livePlaybackState.chapterId,
        positionInChapter = livePlaybackState.positionMs,
        playbackSpeed = livePlaybackState.playbackSpeed,
      )
    }
  } else {
    this
  }
}
