package com.starbook.features.bookOverview.bottomSheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.features.playbackScreen.pixelplayer.PixelPlayerIcons
import com.starbook.features.playbackScreen.pixelplayer.components.SmartImage
import com.starbook.core.ui.icons.StarBookIcons

@Composable
internal fun AudiobookDetailsBottomSheet(
    book: Book,
    sheetState: EditBookBottomSheetState?,
    onResume: (BookId) -> Unit,
    onEdit: (BookId) -> Unit,
    onItemClick: (BottomSheetItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartImage(
                model = book.content.coverUrl,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.content.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                book.content.author?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Stats
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val progress = (book.position.toFloat() / book.duration.toFloat()).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                    trackColor = MaterialTheme.colorScheme.secondaryContainer
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "${book.chapters.size} Chapters",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val playText = if (book.position == 0L) "Play" else "Resume"
            Button(
                onClick = { onResume(book.id) },
                modifier = Modifier.weight(1f).height(56.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(imageVector = PixelPlayerIcons.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(playText, fontWeight = FontWeight.Bold)
            }
            FilledTonalIconButton(
                onClick = { onEdit(book.id) },
                modifier = Modifier.size(56.dp),
                shape = CircleShape
            ) {
                Icon(imageVector = StarBookIcons.Edit, contentDescription = "Edit")
            }
        }

        sheetState?.let {
            HorizontalDivider()
            BottomSheetContent(it, onItemClick)
        }
    }
}
