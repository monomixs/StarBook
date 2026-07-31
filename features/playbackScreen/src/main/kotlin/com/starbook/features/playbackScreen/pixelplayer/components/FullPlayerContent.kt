package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.features.playbackScreen.pixelplayer.Song
import com.starbook.features.playbackScreen.pixelplayer.PixelPlayerViewModel
import com.starbook.features.playbackScreen.pixelplayer.scoped.rememberSmoothProgress
import com.starbook.features.playbackScreen.pixelplayer.PixelPlayerIcons
import com.starbook.core.ui.icons.StarBookIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerContent(
  currentSong: Song,
  playerViewModel: PixelPlayerViewModel,
  expansionFractionProvider: () -> Float,
  onCollapse: () -> Unit,
  onOpenChapters: () -> Unit,
) {
  val stableState by playerViewModel.stablePlayerState.collectAsState()
  val isPlaying = stableState.isPlaying
  val currentPosition by playerViewModel.currentPlaybackPosition.collectAsState()
  val totalDuration = stableState.totalDuration
  val uiState by playerViewModel.playerUiState.collectAsState()

  var showOverflowMenu by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = Color.Transparent,
    modifier = Modifier.graphicsLayer {
      alpha = expansionFractionProvider()
    },
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = { /* Remove title text */ },
        navigationIcon = {
          IconButton(onClick = onCollapse) {
            Icon(
              imageVector = PixelPlayerIcons.KeyboardArrowDown,
              contentDescription = "Collapse",
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        },
        actions = {
          IconButton(onClick = { playerViewModel.onSleepTimerClick() }) {
            Icon(
              imageVector = StarBookIcons.Bedtime,
              contentDescription = "Sleep Timer",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp)
            )
          }
          IconButton(onClick = { playerViewModel.onSpeedClick() }) {
            Icon(
              imageVector = StarBookIcons.Speed,
              contentDescription = "Speed",
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
          Box {
            IconButton(onClick = { showOverflowMenu = true }) {
              Icon(
                imageVector = PixelPlayerIcons.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface,
              )
            }
            DropdownMenu(
              expanded = showOverflowMenu,
              onDismissRequest = { showOverflowMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("Skip Silence") },
                onClick = {
                  playerViewModel.toggleSkipSilence()
                  showOverflowMenu = false
                }
              )
              DropdownMenuItem(
                text = { Text("Volume Boost") },
                onClick = {
                  playerViewModel.onVolumeGainIconClick()
                  showOverflowMenu = false
                }
              )
            }
          }
        },
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(4.dp))

      // Album Art (Swipeable chapters)
      AlbumCarouselSection(
        queue = uiState.currentPlaybackQueue,
        currentMediaItemIndex = stableState.currentMediaItemIndex,
        onChapterSelected = playerViewModel::onChapterSelection,
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .padding(vertical = 16.dp), // Increased padding to prevent edge touches
      )

      Spacer(Modifier.height(12.dp))

      // Info (Non-clickable)
      Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
          text = currentSong.album, // Audiobook title
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.basicMarquee()
        )
        Text(
          text = currentSong.artist,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(Modifier.height(16.dp))

      // Progress
      PlayerProgressSection(
        currentPositionProvider = { currentPosition },
        totalDuration = totalDuration,
        isPlayingProvider = { isPlaying },
        onSeek = { playerViewModel.seekTo(it) },
      )

      Spacer(Modifier.height(15.dp)) // 8dp + 7dp

      // Chapter Pill
      stableState.currentChapterName?.let { chapterName ->
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
          modifier = Modifier
            .padding(bottom = 19.dp) // 12dp + 7dp
            .clickable { onOpenChapters() }
        ) {
          Text(
            text = chapterName,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }

      // Controls
      AnimatedPlaybackControls(
        isPlayingProvider = { isPlaying },
        isBufferingProvider = { stableState.isBuffering },
        onRewind = { playerViewModel.rewind() },
        onPlayPause = { playerViewModel.playPause() },
        onFastForward = { playerViewModel.fastForward() },
        modifier = Modifier.padding(top = 7.dp),
        colorOtherButtons = MaterialTheme.colorScheme.secondaryContainer,
        colorPlayPause = MaterialTheme.colorScheme.primaryContainer,
        tintPlayPauseIcon = MaterialTheme.colorScheme.onPrimaryContainer,
        tintOtherIcons = MaterialTheme.colorScheme.onSecondaryContainer,
      )
    }
  }
}

@Composable
fun PlayerProgressSection(
    currentPositionProvider: () -> Long,
    totalDuration: Long,
    isPlayingProvider: () -> Boolean,
    onSeek: (Long) -> Unit
) {
    val (smoothProgress, _) = rememberSmoothProgress(
        isPlayingProvider = isPlayingProvider,
        currentPositionProvider = currentPositionProvider,
        totalDuration = totalDuration
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        WavySliderExpressive(
            value = { smoothProgress.value },
            onValueChange = { /* handled by commit */ },
            onValueCommit = { onSeek((it * totalDuration).toLong()) },
            isPlaying = isPlayingProvider(),
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            thumbColor = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelColor = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
            Text(
                text = formatDuration(currentPositionProvider()),
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
            Text(
                text = "-" + formatDuration((totalDuration - currentPositionProvider()).coerceAtLeast(0)),
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}
