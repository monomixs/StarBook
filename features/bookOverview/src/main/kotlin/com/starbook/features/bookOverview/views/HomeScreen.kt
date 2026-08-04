package com.starbook.features.bookOverview.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.bookOverview.overview.StatsViewModel
import com.starbook.features.playbackScreen.pixelplayer.components.SmartImage
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  stats: StatsViewModel.StatsViewState,
  onBookClick: (String) -> Unit,
  onBookLongClick: (String) -> Unit,
  onSettingsClick: () -> Unit,
  onGoToLibraryClick: () -> Unit,
) {
  var showMoreSheet by remember { mutableStateOf(false) }
  var showStreakDialog by remember { mutableStateOf(false) }

  if (stats.bookCount == 0 || (stats.todayHours == 0 && stats.todayMinutes == 0 && stats.inProgressBooks.isEmpty())) {
      EmptyHomeState(onGoToLibraryClick)
      return
  }

  Scaffold(
    containerColor = Color.Transparent,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = getGreeting() + " 👋",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Your Listening",
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
               contentDescription = "Settings",
               tint = MaterialTheme.colorScheme.onSurface
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
      contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 10.dp, bottom = 140.dp),
    ) {
      // Hero Card (Continue Listening)
      item(key = "hero") {
        val continueBook = stats.inProgressBooks.firstOrNull() ?: stats.topByHours.firstOrNull()
        if (continueBook != null) {
            HeroCard(
                book = continueBook,
                onClick = { onBookClick(continueBook.id) }
            )
            Spacer(Modifier.height(22.dp))
        }
      }

      // Stats Grid
      item(key = "stats") {
        StatsGrid(
            stats = stats,
            onStreakClick = { showStreakDialog = true }
        )
        Spacer(Modifier.height(30.dp))
      }

      // Top Books
      if (stats.topByHours.isNotEmpty()) {
        item(key = "top_books_title") {
          SectionTitle("Your Top Books")
        }

        items(stats.topByHours.take(3), key = { it.id }) { book ->
            val index = stats.topByHours.indexOf(book)
            val badgeText = when(index) {
                0 -> "★ Favorite"
                1 -> "Top 2"
                2 -> "Top 3"
                else -> null
            }
            BookStatRow(
                book = book,
                badgeText = badgeText,
                onClick = { onBookClick(book.id) },
                onLongClick = { onBookLongClick(book.id) }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (stats.recentBooks.size > 3) {
            item(key = "show_more") {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { showMoreSheet = true },
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(2.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)))),
                        modifier = Modifier.height(44.dp).padding(horizontal = 40.dp)
                    ) {
                        Text(
                            "Show more",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
      }

      // Top Authors
      if (stats.topAuthors.isNotEmpty()) {
        item(key = "authors_title") {
          SectionTitle("Top Authors")
          Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(18.dp)
          ) {
              stats.topAuthors.forEach { author ->
                  AuthorCard(author)
              }
          }
        }
      }

      item { Spacer(Modifier.height(40.dp)) }
    }
  }

  if (showMoreSheet) {
    MoreAudiobooksSheet(
        books = stats.recentBooks,
        onBookClick = {
            onBookClick(it)
            showMoreSheet = false
        },
        onDismiss = { showMoreSheet = false }
    )
  }

  if (showStreakDialog) {
      AlertDialog(
          onDismissRequest = { showStreakDialog = false },
          title = { Text("Streak", fontWeight = FontWeight.ExtraBold) },
          text = { Text("Listen to any audiobook for at least 5 minutes a day to increase your streak.") },
          confirmButton = {
              TextButton(onClick = { showStreakDialog = false }) {
                  Text("Got it", fontWeight = FontWeight.Bold)
              }
          },
          shape = RoundedCornerShape(28.dp),
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
      )
  }
}

@Composable
private fun EmptyHomeState(onGoToLibraryClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No Stats Available",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Start Listening to See Your Stats",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onGoToLibraryClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 50.dp, vertical = 12.dp),
            ) {
                Text("Go to library", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun HeroCard(book: StatsViewModel.BookStat, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))))
                .padding(22.dp)
        ) {
            Column {
                Text(
                    "Continue Listening",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    book.title.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "${book.currentChapterName ?: "Chapter Unknown"} • ${(book.progress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        "▶ Resume",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: StatsViewModel.StatsViewState, onStreakClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Today
        StatCard(
            label = "Today",
            value = "${stats.todayHours}h ${stats.todayMinutes}m",
            modifier = Modifier.fillMaxWidth(),
            isHighlight = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "🔥 Streak",
                value = "${stats.streakDays} Days",
                modifier = Modifier.weight(1f),
                onClick = onStreakClick
            )
            StatCard(
                label = "Books",
                value = stats.bookCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isHighlight) Modifier.background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)))
                    ) else Modifier
                )
                .padding(18.dp)
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 18.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookStatRow(
    book: StatsViewModel.BookStat,
    badgeText: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${book.listenedHours}h ${book.listenedMinutes}m", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (badgeText.startsWith("★")) Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)))
                                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondaryContainer)),
                                RoundedCornerShape(50.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            badgeText,
                            color = if (badgeText.startsWith("★")) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(book.progress)
                        .fillMaxHeight()
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))), RoundedCornerShape(20.dp))
                )
            }
        }
    }
}

@Composable
private fun AuthorCard(author: StatsViewModel.AuthorStat) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = StarBookIcons.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                author.name,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                "${author.listenedHours}h ${author.listenedMinutes}m",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreAudiobooksSheet(
    books: List<StatsViewModel.BookStat>,
    onBookClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("More audiobooks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                ) {
                    Icon(StarBookIcons.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    Surface(
                        onClick = { onBookClick(book.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    Text(book.title, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "${book.listenedHours}h ${book.listenedMinutes}m",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(book.progress)
                                                .fillMaxHeight()
                                                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))), RoundedCornerShape(20.dp))
                                        )
                                    }
                                }
                            }

                            val index = books.indexOf(book)
                            if (index < 3) {
                                val badgeText = when(index) {
                                    0 -> "Favorite"
                                    1 -> "Top 2"
                                    2 -> "Top 3"
                                    else -> ""
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (index == 0) Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)))
                                            else Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondaryContainer)),
                                            RoundedCornerShape(999.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        badgeText,
                                        color = if (index == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
