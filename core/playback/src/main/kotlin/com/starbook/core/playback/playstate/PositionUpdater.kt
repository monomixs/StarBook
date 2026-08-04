package com.starbook.core.playback.playstate

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.DailyListeningRepo
import com.starbook.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import com.starbook.core.featureflag.FeatureFlag
import com.starbook.core.logging.api.Logger
import com.starbook.core.playback.di.PlaybackScope
import com.starbook.core.playback.session.bookId
import com.starbook.core.playback.session.positionInChapter
import com.starbook.core.playback.session.realChapterId
import com.starbook.core.playback.session.toMediaIdOrNull
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Inject
@SingleIn(PlaybackScope::class)
class PositionUpdater(
  private val bookRepo: BookRepository,
  private val dailyListeningRepo: DailyListeningRepo,
  private val scope: CoroutineScope,
  private val playStateManager: PlayStateManager,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
) : Player.Listener {

  private var player: Player? = null
  private var updateJob: Job? = null

  private var lastTickTime: Long = 0
  private var accumulatedListeningMs: Long = 0

  fun attachTo(player: Player) {
    this.player?.removeListener(this)
    this.player = player
    player.addListener(this)

    updateJob = scope.launch {
      playStateManager.playStateFlow
        .map { it == PlayStateManager.PlayState.Playing }
        .distinctUntilChanged()
        .collectLatest { playing ->
          if (playing) {
            lastTickTime = System.currentTimeMillis()
            while (true) {
              val delayDuration = if (experimentalPlaybackPersistenceFeatureFlag.get()) {
                5.minutes
              } else {
                1000.milliseconds // Update more frequently for accurate stats
              }
              delay(delayDuration)

              val now = System.currentTimeMillis()
              accumulatedListeningMs += (now - lastTickTime)
              lastTickTime = now

              flushPositionNow()
            }
          } else {
            // When paused, flush one last time to capture the remaining accumulated time
            flushPositionNow()
            accumulatedListeningMs = 0
          }
        }
    }
  }

  override fun onPositionDiscontinuity(
    oldPosition: Player.PositionInfo,
    newPosition: Player.PositionInfo,
    reason: Int,
  ) {
    flushPosition()
  }

  override fun onPlayWhenReadyChanged(
    playWhenReady: Boolean,
    reason: Int,
  ) {
    flushPosition()
  }

  override fun onPlaybackStateChanged(playbackState: Int) {
    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
      flushPosition()
    }
  }

  override fun onMediaItemTransition(
    mediaItem: MediaItem?,
    reason: Int,
  ) {
    flushPosition()
  }

  private fun flushPosition() {
    scope.launch {
      flushPositionNow()
    }
  }

  suspend fun flushPositionNow() {
    val player = player ?: return
    val mediaItem = player.currentMediaItem ?: return
    val currentPosition = player.currentPosition
      .takeIf { it >= 0 } ?: return
    val mediaId = mediaItem.mediaId.toMediaIdOrNull() ?: return
    val bookId = mediaId.bookId ?: return
    val chapterId = mediaId.realChapterId ?: return
    val positionInChapter = mediaId.positionInChapter(currentPosition) ?: return

    val listeningDelta = accumulatedListeningMs
    accumulatedListeningMs = 0

    if (listeningDelta > 0) {
      dailyListeningRepo.addTime(java.time.LocalDate.now(), listeningDelta)
    }

    bookRepo.updateBook(bookId) { content ->
      if (chapterId in content.chapters) {
        content.copy(
          currentChapter = chapterId,
          positionInChapter = positionInChapter,
          lastPlayedAt = Instant.now(),
          totalTimeListenedMs = content.totalTimeListenedMs + listeningDelta
        )
      } else {
        Logger.w("$mediaId not in $content")
        content
      }
    }
  }

  fun release() {
    player?.removeListener(this)
    updateJob?.cancel()
  }
}
