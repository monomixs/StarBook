package com.starbook.features.playbackScreen.pixelplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starbook.core.data.*
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.BookmarkRepo
import com.starbook.core.data.sleeptimer.SleepTimerPreference
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.data.store.SleepTimerPreferenceStore
import com.starbook.core.logging.api.Logger
import com.starbook.core.playback.CurrentBookResolver
import com.starbook.core.playback.LivePlaybackState
import com.starbook.core.playback.PlayerController
import com.starbook.core.playback.misc.Decibel
import com.starbook.core.playback.misc.VolumeGain
import com.starbook.core.playback.playstate.PlayStateManager
import com.starbook.core.sleeptimer.SleepTimer
import com.starbook.core.sleeptimer.SleepTimerMode
import com.starbook.core.sleeptimer.SleepTimerState
import com.starbook.core.ui.formatTime
import com.starbook.features.playbackScreen.BookPlayDialogViewState
import com.starbook.features.playbackScreen.BookPlayViewState
import com.starbook.features.playbackScreen.VolumeGainFormatter
import com.starbook.features.sleepTimer.SleepTimerViewState
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class PixelPlayerUiState(
  val currentPlaybackQueue: List<Song> = emptyList(),
  val currentQueueSourceName: String = "Chapters",
  val preparingSongId: String? = null,
)

@Immutable
data class StablePlayerState(
  val currentSong: Song? = null,
  val isPlaying: Boolean = false,
  val playWhenReady: Boolean = false,
  val currentMediaItemIndex: Int = 0,
  val currentChapterName: String? = null,
  val totalDuration: Long = 0,
  val isBuffering: Boolean = false,
)

@Inject
class PixelPlayerViewModel(
  private val playerController: PlayerController,
  private val bookRepository: BookRepository,
  private val sleepTimer: SleepTimer,
  private val bookmarkRepository: BookmarkRepo,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
  private val navigator: Navigator,
  private val playStateManager: PlayStateManager,
  private val volumeGainFormatter: VolumeGainFormatter,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PixelPlayerUiState())
  val playerUiState = _uiState.asStateFlow()

  private val _sheetState = MutableStateFlow(PlayerSheetState.COLLAPSED)
  val sheetState = _sheetState.asStateFlow()

  private val _predictiveBackCollapseFraction = MutableStateFlow(0f)
  val predictiveBackCollapseFraction = _predictiveBackCollapseFraction.asStateFlow()

  private val _predictiveBackSwipeEdge = MutableStateFlow<Int?>(null)
  val predictiveBackSwipeEdge = _predictiveBackSwipeEdge.asStateFlow()

  val playerContentExpansionFraction = Animatable(0f)

  private val _stablePlayerState = MutableStateFlow(StablePlayerState())
  val stablePlayerState = _stablePlayerState.asStateFlow()

  private val _lastLivePlaybackState = MutableStateFlow<LivePlaybackState?>(null)

  val currentPlaybackPosition = _lastLivePlaybackState
    .map { state ->
      if (state == null) return@map 0L
      val book = bookRepository.get(state.bookId) ?: return@map state.positionMs
      val currentChapter = book.chapters.find { it.id == state.chapterId } ?: return@map state.positionMs
      val currentMark = currentChapter.markForPosition(state.positionMs)
      (state.positionMs - currentMark.startMs).coerceAtLeast(0L)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

  internal val dialogState = mutableStateOf<BookPlayDialogViewState?>(null)

  val audiobookViewState: StateFlow<BookPlayViewState?> = combine(
    _lastLivePlaybackState,
    _lastLivePlaybackState.flatMapLatest { state ->
      if (state == null) flowOf(null)
      else bookRepository.flow(state.bookId).filterNotNull()
    },
    sleepTimer.state
  ) { state, book, sleepTime ->
    if (state == null || book == null) return@combine null

    val currentChapter = book.chapters.find { it.id == state.chapterId } ?: book.currentChapter
    val currentMark = currentChapter.markForPosition(state.positionMs)

    BookPlayViewState(
      sleepTimerState = sleepTime.toViewState(),
      playing = state.isPlaying,
      title = book.content.name,
      showPreviousNextButtons = true,
      chapterName = currentMark.name ?: currentChapter.name,
      duration = currentChapter.duration.milliseconds,
      playedTime = state.positionMs.milliseconds,
      cover = book.content.coverUrl,
      skipSilence = book.content.skipSilence,
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Simplified theming for now
  val activePlayerColorSchemePair = MutableStateFlow<Pair<ColorScheme, ColorScheme>?>(null)
  val currentThemedAlbumArtUri = MutableStateFlow<String?>(null)

  // Dummy flows for compatibility
  val toastEvents = MutableSharedFlow<String>()
  val showNoInternetDialog = MutableSharedFlow<Unit>()
  val writePermissionRequest = MutableSharedFlow<android.content.IntentSender>()
  val deletePermissionRequest = MutableSharedFlow<android.content.IntentSender>()
  val remotePosition = MutableStateFlow(0L)
  val isRemotePlaybackActive = MutableStateFlow(false)
  val isCastConnecting = MutableStateFlow(false)

  // Config slice dummy
  data class PlayerConfigSlice(
    val navBarCornerRadius: Int = 12,
    val navBarStyle: String = NavBarStyle.DEFAULT,
    val carouselStyle: String = "NO_PEEK",
    val fullPlayerLoadingTweaks: FullPlayerLoadingTweaks = FullPlayerLoadingTweaks(),
    val tapBackgroundClosesPlayer: Boolean = false,
    val useSmoothCorners: Boolean = false,
    val playerThemePreference: String = "ALBUM_ART",
  )

  class FullPlayerLoadingTweaks(
    val delayAll: Boolean = false,
    val delayAlbumCarousel: Boolean = false,
    val showPlaceholders: Boolean = false,
    val applyPlaceholdersOnClose: Boolean = false,
    val switchOnDragRelease: Boolean = false,
    val contentAppearThresholdPercent: Int = 0,
    val contentCloseThresholdPercent: Int = 0,
    val transparentPlaceholders: Boolean = false,
    val delayControls: Boolean = false,
    val delayProgressBar: Boolean = false,
    val delaySongMetadata: Boolean = false,
  )

  val playerConfigSlice = MutableStateFlow(PlayerConfigSlice())

  // Full player slice dummy
  data class FullPlayerSlice(
    val currentSongArtists: List<Artist> = emptyList(),
    val lyricsSyncOffset: Int = 0,
    val albumArtQuality: String = "MEDIUM",
    val audioMetadata: AudioMetadata = AudioMetadata(),
    val showPlayerFileInfo: Boolean = false,
    val immersiveLyricsEnabled: Boolean = false,
    val immersiveLyricsTimeout: Long = 0,
    val isImmersiveTemporarilyDisabled: Boolean = false,
    val isRemotePlaybackActive: Boolean = false,
    val selectedRouteName: String? = null,
    val isBluetoothEnabled: Boolean = false,
    val bluetoothName: String? = null,
  )

  class AudioMetadata(
    val mediaId: String? = null,
    val mimeType: String? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
  )

  val fullPlayerSlice = MutableStateFlow(FullPlayerSlice())

  init {
    viewModelScope.launch {
      playerController.livePlaybackStateFlow().collect { state ->
        _lastLivePlaybackState.value = state
        updateStateFromLivePlayback(state)
      }
    }
    viewModelScope.launch {
      currentBookStoreId.data.collect { id ->
        if (id == null) {
          _stablePlayerState.value = StablePlayerState()
          _lastLivePlaybackState.value = null
        }
      }
    }
  }

  private suspend fun updateStateFromLivePlayback(state: LivePlaybackState?) {
    if (state == null) {
      _stablePlayerState.value = StablePlayerState()
      return
    }

    val book = bookRepository.get(state.bookId) ?: return
    val currentChapter = book.chapters.find { it.id == state.chapterId } ?: book.chapters.first()
    val currentMark = currentChapter.markForPosition(state.positionMs)

    val song = Song(
      id = state.bookId.value,
      title = book.content.name,
      artist = book.content.author ?: "Unknown",
      album = book.content.name,
      duration = currentMark.durationMs,
      albumArtUriString = book.content.coverUrl,
    )

    _stablePlayerState.value = _stablePlayerState.value.copy(
      currentSong = song,
      isPlaying = state.isPlaying,
      totalDuration = song.duration,
      currentMediaItemIndex = book.chapters.indexOf(currentChapter),
      currentChapterName = currentMark.name ?: currentChapter.name,
      isBuffering = state.isBuffering
    )

    _uiState.value = _uiState.value.copy(
      currentPlaybackQueue = book.chapters.map {
        Song(
          id = it.id.value,
          title = it.name ?: "Unknown",
          artist = book.content.author ?: "Unknown",
          album = book.content.name,
          duration = it.duration,
          albumArtUriString = book.content.coverUrl,
        )
      },
    )
  }

  private fun SleepTimerState.toViewState(): BookPlayViewState.SleepTimerViewState = when (this) {
    SleepTimerState.Disabled -> BookPlayViewState.SleepTimerViewState.Disabled
    is SleepTimerState.Enabled.WithDuration -> BookPlayViewState.SleepTimerViewState.Enabled.WithDuration(this.leftDuration)
    SleepTimerState.Enabled.WithEndOfChapter -> BookPlayViewState.SleepTimerViewState.Enabled.WithEndOfChapter
  }

  fun playPause() = playerController.playPause()
  fun fastForward() = playerController.fastForward()
  fun rewind() = playerController.rewind()
  fun onPrevious() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      val currentIndex = book.chapters.indexOfFirst { it.id == state.chapterId }
      if (currentIndex > 0) {
        playerController.setPosition(0, book.chapters[currentIndex - 1].id)
      } else {
        playerController.setPosition(0, state.chapterId)
      }
    }
  }

  fun onNext() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      val currentIndex = book.chapters.indexOfFirst { it.id == state.chapterId }
      if (currentIndex < book.chapters.lastIndex) {
        playerController.setPosition(0, book.chapters[currentIndex + 1].id)
      }
    }
  }
  fun seekTo(pos: Long) {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      val currentChapter = book.chapters.find { it.id == state.chapterId } ?: return@launch
      val currentMark = currentChapter.markForPosition(state.positionMs)

      val absolutePosInChapter = currentMark.startMs + pos
      playerController.setPosition(absolutePosInChapter, state.chapterId)
    }
  }

  fun togglePlayerSheetState() {
    _sheetState.value = if (_sheetState.value == PlayerSheetState.COLLAPSED) {
      PlayerSheetState.EXPANDED
    } else {
      PlayerSheetState.COLLAPSED
    }
  }

  fun expandPlayerSheet() {
    _sheetState.value = PlayerSheetState.EXPANDED
  }

  fun collapsePlayerSheet() {
    _sheetState.value = PlayerSheetState.COLLAPSED
  }

  fun updatePredictiveBackCollapseFraction(f: Float) {
    _predictiveBackCollapseFraction.value = f
  }

  fun updatePredictiveBackSwipeEdge(e: Int?) {
    _predictiveBackSwipeEdge.value = e
  }

  fun onWritePermissionResult(granted: Boolean) {}
  fun onDeletePermissionResult(granted: Boolean) {}
  fun refreshLocalConnectionInfo() {}
  fun setSliderUiMounted(mounted: Boolean) {}
  fun dismissPlaylistAndShowUndo() {}
  fun setMiniPlayerDismissing(d: Boolean) {}
  fun updateQueueSheetVisibility(v: Boolean) {}
  fun updateCastSheetVisibility(v: Boolean) {}

  // Audiobook Specific
  fun openChapterDialog() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      val currentChapter = book.chapters.find { it.id == state.chapterId } ?: book.currentChapter
      val currentMark = currentChapter.markForPosition(state.positionMs)

      dialogState.value = BookPlayDialogViewState.SelectChapterDialog(
        items = book.chapters.flatMapIndexed { chapterIndex, chapter ->
          chapter.chapterMarks.mapIndexed { markIndex, chapterMark ->
            val previousChapters = book.chapters.take(chapterIndex)
            BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
              number = previousChapters.sumOf { it.chapterMarks.count() } + markIndex + 1,
              name = chapterMark.name ?: chapter.name ?: "",
              active = chapterMark == currentMark && chapter == currentChapter,
              time = formatTime(chapterMark.endMs - chapterMark.startMs),
            )
          }
        },
      )
    }
  }

  fun playBook(id: BookId) {
    viewModelScope.launch {
      val currentState = _lastLivePlaybackState.value
      if (currentState?.bookId != id || !currentState.isPlaying) {
        // Update store for other components
        currentBookStoreId.updateData { id }
        // Directly trigger play with ID to bypass DataStore read latency
        playerController.play(id)
      }
    }
  }

  fun toggleSkipSilence() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      playerController.skipSilence(!book.content.skipSilence)
    }
  }

  fun onChapterClick(number: Int) {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      var currentIndex = -1
      book.chapters.forEach { chapter ->
        chapter.chapterMarks.forEach { mark ->
          currentIndex++
          if (currentIndex == number - 1) {
            playerController.setPosition(mark.startMs, chapter.id)
            dialogState.value = null
            return@launch
          }
        }
      }
    }
  }

  fun onSpeedClick() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      dialogState.value = BookPlayDialogViewState.SpeedDialog(book.content.playbackSpeed)
    }
  }

  fun onPlaybackSpeedChanged(speed: Float) {
    playerController.setSpeed(speed)
    dialogState.value = BookPlayDialogViewState.SpeedDialog(speed)
  }

  fun onSleepTimerClick() {
    viewModelScope.launch {
      if (sleepTimer.state.value.enabled) {
        sleepTimer.disable()
        dialogState.value = null
      } else {
        dialogState.value = BookPlayDialogViewState.SleepTimer(
          viewState = SleepTimerViewState(
            customSleepTime = sleepTimerPreferenceStore.data.first().duration.inWholeMinutes.toInt(),
          ),
        )
      }
    }
  }

  fun onAcceptSleepTime(time: Int) {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      bookmarkRepository.addBookmarkAtBookPosition(
        book = book,
        setBySleepTimer = true,
        title = null,
      )
      sleepTimer.enable(SleepTimerMode.TimedWithDuration(time.minutes))
      dialogState.value = null
    }
  }

  fun onAcceptSleepAtEndOfChapter() {
    sleepTimer.enable(SleepTimerMode.EndOfChapter)
    dialogState.value = null
  }

  fun incrementSleepTime() {
    viewModelScope.launch {
      val current = sleepTimerPreferenceStore.data.first()
      val newTime = current.duration.inWholeMinutes.toInt() + 1
      sleepTimerPreferenceStore.updateData { it.copy(duration = newTime.minutes) }
      updateSleepDialog(newTime)
    }
  }

  fun decrementSleepTime() {
    viewModelScope.launch {
      val current = sleepTimerPreferenceStore.data.first()
      val newTime = (current.duration.inWholeMinutes.toInt() - 1).coerceAtLeast(1)
      sleepTimerPreferenceStore.updateData { it.copy(duration = newTime.minutes) }
      updateSleepDialog(newTime)
    }
  }

  private fun updateSleepDialog(time: Int) {
    val current = dialogState.value
    if (current is BookPlayDialogViewState.SleepTimer) {
      dialogState.value = BookPlayDialogViewState.SleepTimer(current.viewState.copy(customSleepTime = time))
    }
  }

  fun onBookmarkClick() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      navigator.goTo(Destination.Bookmarks(state.bookId))
      collapsePlayerSheet()
    }
  }

  fun onBookmarkLongClick() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      bookmarkRepository.addBookmarkAtBookPosition(
        book = book,
        title = null,
        setBySleepTimer = false,
      )
      // Show toast somehow?
    }
  }

  fun onChapterSelection(song: Song, index: Int) {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      val targetChapter = book.chapters.getOrNull(index) ?: return@launch
      playerController.setPosition(0, targetChapter.id)
    }
  }

  fun dismissDialog() {
    dialogState.value = null
  }

  fun onVolumeGainIconClick() {
    viewModelScope.launch {
      val state = _lastLivePlaybackState.value ?: return@launch
      val book = bookRepository.get(state.bookId) ?: return@launch
      val gain = Decibel(book.content.gain)
      dialogState.value = BookPlayDialogViewState.VolumeGainDialog(
        gain = gain,
        maxGain = VolumeGain.MAX_GAIN,
        valueFormatted = volumeGainFormatter.format(gain),
      )
    }
  }

  fun onVolumeGainChanged(gain: Decibel) {
    playerController.setGain(gain)
    dialogState.value = BookPlayDialogViewState.VolumeGainDialog(
      gain = gain,
      maxGain = VolumeGain.MAX_GAIN,
      valueFormatted = volumeGainFormatter.format(gain),
    )
  }
}
