package com.starbook.features.bookOverview.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.ui.WaveProgressBar
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.bookOverview.overview.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  stats: StatsViewModel.StatsViewState,
  onBookClick: (String) -> Unit,
  onBookLongClick: (String) -> Unit,
  onSettingsClick: () -> Unit,
) {
  Scaffold(
    containerColor = Color.Transparent,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Good evening",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = "Your listening",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
           IconButton(onClick = onSettingsClick) {
             Icon(
               imageVector = StarBookIcons.Settings,
               contentDescription = "Settings"
             )
           }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 105.dp),
      verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
      // Hero Card
      item(key = "hero") {
        HeroCard(stats)
      }

      // Continue Listening (2xN Grid Flow)
      if (stats.inProgressBooks.isNotEmpty()) {
        item(key = "continue_title") {
          SectionTitle("Continue Listening")

          val bookChunks = remember(stats.inProgressBooks) {
            stats.inProgressBooks.chunked(2)
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
          ) {
            items(bookChunks) { chunk ->
              Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                chunk.forEach { book ->
                  HomeListBookRow(
                    book = book,
                    onBookClick = onBookClick,
                    onBookLongClick = onBookLongClick,
                    modifier = Modifier.width(300.dp)
                  )
                }
                if (chunk.size == 1) {
                  Spacer(Modifier.height(80.dp).width(300.dp))
                }
              }
            }
          }
        }
      }

      // Hours per audiobook
      if (stats.topByHours.isNotEmpty()) {
        item(key = "hours_title") {
          SectionTitle("Hours Per Audiobook")
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val maxHours = remember(stats.topByHours) {
              stats.topByHours.maxOfOrNull { it.listenedHours + (it.listenedMinutes / 60f) } ?: 1f
            }
            stats.topByHours.forEach { book ->
              HoursRow(book, maxHours)
            }
          }
        }
      }

      // Most listened authors
      if (stats.topAuthors.isNotEmpty()) {
        item(key = "authors_title") {
          SectionTitle("Most Listened Authors")
          FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = Int.MAX_VALUE
          ) {
            stats.topAuthors.forEach { author ->
              AuthorChip(author)
            }
          }
        }
      }

      item { Spacer(Modifier.height(20.dp)) }
    }
  }
}

@Composable
private fun HeroCard(stats: StatsViewModel.StatsViewState) {
  Surface(
    color = MaterialTheme.colorScheme.primary,
    contentColor = Color.White,
    shape = RoundedCornerShape(28.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Text(
        text = "Total time listened",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.85f)
      )
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = stats.totalHours.toString(),
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White
        )
        Text(
          text = "hrs",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 5.dp, bottom = 8.dp),
          color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
          text = stats.totalMinutes.toString(),
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White
        )
        Text(
          text = "mins",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 5.dp, bottom = 8.dp),
          color = Color.White.copy(alpha = 0.8f)
        )
      }
      Text(
        text = "Across ${stats.bookCount} audiobooks · ${stats.finishedCount} finished",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.85f)
      )
      Spacer(Modifier.height(16.dp))
      WaveProgressBar(progress = 1f, color = Color.White, modifier = Modifier.fillMaxWidth().height(14.dp), strokeWidth = 5f)
    }
  }
}

@Composable
private fun SectionTitle(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(bottom = 14.dp),
    color = MaterialTheme.colorScheme.onSurface
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeListBookRow(
  book: StatsViewModel.BookStat,
  onBookClick: (String) -> Unit,
  onBookLongClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = RoundedCornerShape(20.dp),
    shadowElevation = 4.dp,
    modifier = modifier
        .clip(RoundedCornerShape(20.dp))
        .combinedClickable(
            onClick = { onBookClick(book.id) },
            onLongClick = { onBookLongClick(book.id) }
        )
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
         com.starbook.features.playbackScreen.pixelplayer.components.SmartImage(
           modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
           model = book.coverUrl,
           contentScale = androidx.compose.ui.layout.ContentScale.Crop,
         )
         if (book.coverUrl == null) {
           Text(
             text = book.title.take(1).uppercase(),
             fontSize = 20.sp,
             fontWeight = FontWeight.ExtraBold,
             color = MaterialTheme.colorScheme.onPrimaryContainer
           )
         }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = book.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = book.author,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        WaveProgressBar(
          progress = book.progress,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.fillMaxWidth().height(8.dp)
        )
      }
    }
  }
}

@Composable
private fun HoursRow(book: StatsViewModel.BookStat, maxHours: Float) {
  val isDark = isSystemInDarkTheme()
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Text(
        text = book.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface
      )
      val timeText = buildString {
        if (book.listenedHours > 0) append("${book.listenedHours}h ")
        append("${book.listenedMinutes}m")
      }
      Text(
        text = timeText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
      )
    }
    Spacer(Modifier.height(8.dp))
    val currentTotalHours = book.listenedHours + (book.listenedMinutes / 60f)
    val widthFactor = (currentTotalHours / maxHours).coerceAtLeast(0.06f).coerceAtMost(1f)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(widthFactor)
          .fillMaxHeight()
          .background(
            if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
            CircleShape
          )
      )
    }
  }
}

@Composable
private fun AuthorChip(author: StatsViewModel.AuthorStat) {
  val isDark = isSystemInDarkTheme()
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(100.dp),
    verticalArrangement = Arrangement.spacedBy(7.dp)
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .background(
          if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer,
          RoundedCornerShape(24.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = StarBookIcons.Person,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
      )
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = author.name,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
      )
      val timeText = buildString {
        if (author.listenedHours > 0) append("${author.listenedHours}h ")
        append("${author.listenedMinutes}m")
      }
      Text(
        text = timeText,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
