package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.starbook.core.playback.misc.Decibel
import com.starbook.features.playbackScreen.BookPlayDialogViewState
import com.starbook.core.strings.R as StringsR
import java.text.DecimalFormat

@Composable
internal fun SpeedDialog(
    dialogState: BookPlayDialogViewState.SpeedDialog,
    onSpeedChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speedFormatter = remember { DecimalFormat("0.00 x") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(stringResource(id = StringsR.string.playback_speed_title)) },
        text = {
            Column {
                Text(stringResource(id = StringsR.string.playback_speed_title) + ": " + speedFormatter.format(dialogState.speed))
                val valueRange = 0.5F..dialogState.maxSpeed
                val rangeSize = valueRange.endInclusive - valueRange.start
                val stepSize = 0.05
                val steps = (rangeSize / stepSize).toInt() - 1
                Slider(
                    steps = steps,
                    valueRange = valueRange,
                    value = dialogState.speed,
                    onValueChange = onSpeedChanged
                )
            }
        }
    )
}

@Composable
internal fun VolumeGainDialog(
    dialogState: BookPlayDialogViewState.VolumeGainDialog,
    onGainChanged: (Decibel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column {
                Text(stringResource(id = StringsR.string.playback_option_volume_boost) + ": " + dialogState.valueFormatted)
                Slider(
                    valueRange = 0F..dialogState.maxGain.value,
                    value = dialogState.gain.value,
                    onValueChange = { onGainChanged(Decibel(it)) }
                )
            }
        }
    )
}

@Composable
internal fun SelectChapterDialog(
    dialogState: BookPlayDialogViewState.SelectChapterDialog,
    onChapterClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        content = {
            val selectedIndex = dialogState.items.indexOfFirst { it.active }
            val initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0)
            LazyColumn(
                state = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleItemIndex),
                content = {
                    items(dialogState.items) { chapter ->
                        val isCurrentChapter = chapter.active
                        val description = stringResource(StringsR.string.playback_chapter_current_content_description)
                        val backgroundColor = if (chapter.active) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                        val contentColor = if (chapter.active) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        val secondaryColor = if (chapter.active) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = backgroundColor,
                                headlineColor = contentColor,
                                leadingIconColor = secondaryColor,
                                trailingIconColor = secondaryColor,
                                supportingColor = secondaryColor
                            ),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(shape = RoundedCornerShape(16.dp))
                                .semantics {
                                    selected = chapter.active
                                    if (isCurrentChapter) contentDescription = description
                                }
                                .clickable { onChapterClick(chapter.number) },
                            headlineContent = {
                                Text(
                                    text = chapter.name,
                                    fontWeight = if (chapter.active) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingContent = {
                                Text(
                                    text = chapter.number.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            trailingContent = {
                                Text(
                                    text = chapter.time,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            )
        }
    )
}
