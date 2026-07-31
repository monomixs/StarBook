package com.starbook.features.bookOverview.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.starbook.core.data.BookId
import com.starbook.core.ui.WaveProgressBar
import com.starbook.features.bookOverview.overview.BookOverviewCategory
import com.starbook.features.bookOverview.overview.BookOverviewItemViewState

@Composable
internal fun ListBooks(
  books: Map<BookOverviewCategory, Map<BookId, BookOverviewItemViewState>>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  showPermissionBugCard: Boolean,
  onPermissionBugCardClick: () -> Unit,
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 220.dp),
  ) {
    if (showPermissionBugCard) {
      item(key = "permission_bug") {
        PermissionBugCard(onPermissionBugCardClick)
      }
    }
    books.forEach { (category, books) ->
      if (books.isEmpty()) return@forEach
      item(key = category.name) {
        Text(
          text = stringResource(id = category.nameRes).toUpperCase(LocaleList.current),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
      }
      items(
        items = books.toList(),
        key = { (bookId, _) -> bookId.value },
        contentType = { "item" },
      ) { (_, book) ->
        ListBookRow(
          book = book,
          onBookClick = onBookClick,
          onBookLongClick = onBookLongClick,
        )
      }
    }
  }
}

@Composable
internal fun ListBookRow(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
) {
  BookCard(
    bookId = book.id,
    onBookClick = onBookClick,
    onBookLongClick = onBookLongClick,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(70.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
      ) {
         AsyncImage(
           modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
           model = book.cover,
           contentScale = ContentScale.Crop,
           contentDescription = null,
         )
         if (book.cover == null) {
           Text(
             text = book.name.take(1).toUpperCase(LocaleList.current),
             fontSize = 24.sp,
             fontWeight = FontWeight.ExtraBold,
             color = MaterialTheme.colorScheme.onPrimaryContainer
           )
         }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = book.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (book.author != null) {
          Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Spacer(Modifier.height(8.dp))
        WaveProgressBar(
          progress = book.progress,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.fillMaxWidth().height(12.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
          text = "${(book.progress * 100).toInt()}% · ${book.remainingTime} left",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
