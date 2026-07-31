package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.starbook.features.playbackScreen.pixelplayer.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumCarouselSection(
    queue: List<Song>,
    currentMediaItemIndex: Int,
    onChapterSelected: (Song, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (queue.isEmpty()) return

    val state = rememberCarouselState(initialItem = currentMediaItemIndex) { queue.size }

    // Sync state with current index when it changes programmatically
    LaunchedEffect(currentMediaItemIndex) {
        if (state.currentItem != currentMediaItemIndex) {
            state.animateScrollToItem(currentMediaItemIndex)
        }
    }

    // Trigger chapter change on user swipe
    LaunchedEffect(state.currentItem) {
        if (state.currentItem != currentMediaItemIndex) {
            onChapterSelected(queue[state.currentItem], state.currentItem)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        RoundedHorizontalMultiBrowseCarousel(
            state = state,
            carouselWidth = maxWidth,
            itemSpacing = 8.dp,
            itemCornerRadius = 24.dp,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            val song = queue[index]
            SmartImage(
                model = song.albumArtUriString,
                modifier = Modifier
                    .fillMaxSize()
                    .maskClip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
