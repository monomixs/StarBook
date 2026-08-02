package com.starbook.features.bookOverview.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.starbook.core.data.BookId
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.bookOverview.overview.BookOverviewCategory
import com.starbook.features.bookOverview.overview.BookOverviewItemViewState
import kotlinx.coroutines.delay
import com.starbook.core.strings.R as StringsR

import kotlin.time.Duration.Companion.milliseconds

private val SpringFloat = spring<Float>(
    dampingRatio = 0.6f,
    stiffness = Spring.StiffnessLow
)

private val SpringIntOffset = spring<IntOffset>(
    dampingRatio = 0.6f,
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = IntOffset(1, 1)
)

@Composable
fun BookSearchScreen(
    viewState: BookSearchViewState,
    query: String,
    onQueryChange: (String) -> Unit,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit,
    onSettingsClick: () -> Unit
) {
    val isSearching = query.isNotBlank()

    Scaffold(
        topBar = {
            SearchHeader(
                query = query,
                onQueryChange = onQueryChange,
                isSearching = isSearching
            )
        }
    ) { padding ->
        BookSearchContent(
            viewState = viewState,
            onQueryChange = onQueryChange,
            onBookClick = onBookClick,
            onBookLongClick = onBookLongClick,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun BookSearchContent(
    viewState: BookSearchViewState,
    onQueryChange: (String) -> Unit,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isSearching = viewState.query.isNotBlank()

    Box(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        AnimatedContent(
            targetState = isSearching,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) +
                        slideInVertically(
                            animationSpec = SpringIntOffset,
                            initialOffsetY = { 40 }
                        )) togetherWith
                        fadeOut(animationSpec = tween(200))
            },
            label = "SearchContentTransition"
        ) { searching ->
            if (searching) {
                SearchResultsList(
                    viewState = viewState,
                    query = viewState.query,
                    onBookClick = onBookClick,
                    onBookLongClick = onBookLongClick
                )
            } else {
                ExploreShelf(
                    viewState = viewState,
                    onBookClick = onBookClick,
                    onBookLongClick = onBookLongClick
                )
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean
) {
    val headlineSize by animateFloatAsState(if (isSearching) 26f else 38f, animationSpec = SpringFloat, label = "headlineSize")
    val subheadAlpha by animateFloatAsState(if (isSearching) 0f else 1f, label = "subheadAlpha")
    val subheadHeight by animateDpAsState(if (isSearching) 0.dp else 24.dp, label = "subheadHeight")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .padding(top = 16.dp, bottom = 4.dp)
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = headlineSize.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.01).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(modifier = Modifier
                .height(subheadHeight)
                .alpha(subheadAlpha)) {
                Text(
                    text = "Find your next listen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                query = query,
                onQueryChange = onQueryChange
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val backgroundColor by animateColorAsState(
        if (query.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = StarBookIcons.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Titles, authors, narrators…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = StarBookIcons.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExploreShelf(
    viewState: BookSearchViewState,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<BookOverviewCategory?>(null) }
    val categories = remember {
        listOf(
            null,
            BookOverviewCategory.FINISHED,
            BookOverviewCategory.CURRENT,
            BookOverviewCategory.NOT_STARTED
        )
    }

    val allBooks = (viewState as? BookSearchViewState.EmptySearch)?.books ?: emptyList()
    val continueBooks = remember(allBooks) { allBooks.filter { it.category == BookOverviewCategory.CURRENT } }
    val filteredBooks = remember(allBooks, selectedCategory) {
        if (selectedCategory == null) allBooks else allBooks.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp)
    ) {
        // Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 0.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val label = when (category) {
                        null -> "All"
                        BookOverviewCategory.FINISHED -> "Finished"
                        BookOverviewCategory.CURRENT -> "Continue"
                        BookOverviewCategory.NOT_STARTED -> "Haven't Started"
                    }
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        if (continueBooks.isNotEmpty() && selectedCategory == null) {
            item {
                Text(
                    text = "Pick up where you left off",
                    style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(continueBooks, key = { _, book -> book.id.value }) { index, book ->
                        ContinueCard(book, index, onBookClick, onBookLongClick)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            Text(
                text = "Explore the shelf",
                style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
            )
        }

        val chunkedBooks = filteredBooks.chunked(2)
        itemsIndexed(chunkedBooks) { rowIndex, row ->
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEachIndexed { colIndex, book ->
                    val absoluteIndex = rowIndex * 2 + colIndex
                    key(book.id.value) {
                        Box(modifier = Modifier.weight(1f)) {
                            DiscoverCard(book, absoluteIndex, onBookClick, onBookLongClick)
                        }
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    viewState: BookSearchViewState,
    query: String,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit
) {
    val books = (viewState as? BookSearchViewState.SearchResults)?.books ?: emptyList()
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val onHighlightColor = MaterialTheme.colorScheme.onPrimaryContainer

    if (books.isEmpty()) {
        EmptyState(query)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 220.dp)
        ) {
            item {
                Text(
                    text = buildAnnotatedString {
                        append("${books.size} results for ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("“$query”")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            itemsIndexed(books, key = { _, book -> book.id.value }) { index, book ->
                ResultRow(book, index, query, highlightColor, onHighlightColor, onBookClick, onBookLongClick)
            }
        }
    }
}

@Composable
private fun StaggeredEntrance(
    index: Int,
    content: @Composable () -> Unit
) {
    // Optimization: Skip animation for very high indices to reduce timer load on low-end devices
    if (index > 20) {
        content()
        return
    }

    val visible = androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!visible.value) {
            delay((index * 16).milliseconds)
            visible.value = true
        }
    }
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(tween(400)) + slideInVertically(SpringIntOffset) { 20 },
        label = "StaggeredEntrance"
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinueCard(
    book: BookOverviewItemViewState,
    index: Int,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit
) {
    StaggeredEntrance(index = index) {
        val appearance = remember(index) { CoverAppearance.get(index) }
        Column(
            modifier = Modifier
                .width(132.dp)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onBookClick(book.id) },
                    onLongClick = { onBookLongClick(book.id) }
                )
        ) {
            AsymmetricBookCover(
                book = book,
                appearance = appearance,
                modifier = Modifier.size(132.dp, 168.dp),
                showDogEar = true
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = book.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(7.dp))
            LinearProgressIndicator(
                progress = { book.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${(book.progress * 100).toInt()}% · ${book.remainingTime} left",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiscoverCard(
    book: BookOverviewItemViewState,
    index: Int,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit
) {
    StaggeredEntrance(index = index) {
        val appearance = remember(index) { CoverAppearance.get(index) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onBookClick(book.id) },
                    onLongClick = { onBookLongClick(book.id) }
                )
        ) {
            AsymmetricBookCover(
                book = book,
                appearance = appearance,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 3.6f),
                showDogEar = book.progress > 0 && book.progress < 1f
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = book.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResultRow(
    book: BookOverviewItemViewState,
    index: Int,
    query: String,
    highlightColor: Color,
    onHighlightColor: Color,
    onBookClick: (BookId) -> Unit,
    onBookLongClick: (BookId) -> Unit
) {
    StaggeredEntrance(index = index) {
        val appearance = remember(index) { CoverAppearance.get(index) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onBookClick(book.id) },
                    onLongClick = { onBookLongClick(book.id) }
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AsymmetricBookCover(
                book = book,
                appearance = appearance,
                modifier = Modifier.size(58.dp),
                showDogEar = book.progress > 0 && book.progress < 1f
            )

            Column(modifier = Modifier.weight(1f)) {
                val highlightedName = remember(book.name, query, highlightColor, onHighlightColor) {
                    highlightText(book.name, query, highlightColor, onHighlightColor)
                }
                Text(
                    text = highlightedName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val highlightedAuthor = remember(book.author, query, highlightColor, onHighlightColor) {
                    highlightText(book.author ?: "Unknown", query, highlightColor, onHighlightColor)
                }
                Text(
                    text = highlightedAuthor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (book.genre != null) {
                        Text(
                            text = book.genre,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Box(modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant))
                    Icon(
                        imageVector = StarBookIcons.History, // Using as a clock icon proxy
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = book.duration ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { onBookClick(book.id) },
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = StarBookIcons.NotStarted,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AsymmetricBookCover(
    book: BookOverviewItemViewState,
    appearance: CoverAppearance,
    modifier: Modifier = Modifier,
    showDogEar: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, animationSpec = SpringFloat, label = "CoverScale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(appearance.shape)
            .background(appearance.gradient)
    ) {
        AsyncImage(
            model = book.cover,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (book.cover == null) {
            Text(
                text = book.name.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (showDogEar) {
            DogEar(modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun DogEar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(28.dp)) {
        val pathDark = Path().apply {
            moveTo(size.width, 0f)
            lineTo(0f, 0f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(pathDark, Color.Black.copy(alpha = 0.22f))

        val pathLight = Path().apply {
            moveTo(size.width, 0f)
            lineTo(size.width * 0.22f, 0f)
            lineTo(size.width, size.height * 0.78f)
            close()
        }
        drawPath(
            pathLight,
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.55f))
            )
        )
    }
}

@Composable
private fun EmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = StarBookIcons.Search,
            contentDescription = null,
            modifier = Modifier.size(46.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = buildAnnotatedString {
                append("No matches for “")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(query)
                }
                append("”")
            },
            style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Try a different title, author or narrator.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, start = 24.dp, end = 24.dp)
        )
    }
}

private fun highlightText(text: String, query: String, highlightColor: Color, onHighlightColor: Color): AnnotatedString {
    if (query.isEmpty()) return AnnotatedString(text)

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()

    return buildAnnotatedString {
        var start = 0
        while (true) {
            val index = lowerText.indexOf(lowerQuery, start)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))
            withStyle(SpanStyle(background = highlightColor, color = onHighlightColor)) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
        }
    }
}

private data class CoverAppearance(
    val shape: Shape,
    val gradient: Brush
) {
    companion object {
        private val Shapes = listOf(
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 8.dp, bottomStart = 28.dp),
            RoundedCornerShape(topStart = 8.dp, topEnd = 28.dp, bottomEnd = 28.dp, bottomStart = 28.dp),
            RoundedCornerShape(topStart = 28.dp, topEnd = 8.dp, bottomEnd = 28.dp, bottomStart = 28.dp)
        )

        private val Gradients = listOf(
            Brush.linearGradient(listOf(Color(0xFF6A3EA1), Color(0xFF9B6FD1))),
            Brush.linearGradient(listOf(Color(0xFFC97A17), Color(0xFFF0B15C))),
            Brush.linearGradient(listOf(Color(0xFF3F6B57), Color(0xFF74A98B))),
            Brush.linearGradient(listOf(Color(0xFF3E5C76), Color(0xFF7C9CB8))),
            Brush.linearGradient(listOf(Color(0xFF7A2E4D), Color(0xFFB15A78)))
        )

        fun get(index: Int) = CoverAppearance(
            shape = Shapes[index % Shapes.size],
            gradient = Gradients[index % Gradients.size]
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookSearchScreenPreview() {
    val mockBooks = listOf(
        BookOverviewItemViewState(
            name = "The Cartographer's Daughter",
            author = "Elena Marsh",
            cover = null,
            progress = 0.62f,
            id = BookId("1"),
            remainingTime = "4h 18m",
            genre = "Fiction",
            duration = "11h 20m",
            category = BookOverviewCategory.CURRENT
        ),
        BookOverviewItemViewState(
            name = "Static and Salt",
            author = "Devon Ochoa",
            cover = null,
            progress = 0.18f,
            id = BookId("2"),
            remainingTime = "7h 25m",
            genre = "Sci-Fi",
            duration = "9h 05m",
            category = BookOverviewCategory.CURRENT
        ),
        BookOverviewItemViewState(
            name = "Nine Doors to Marrow",
            author = "Callum Fitch",
            cover = null,
            progress = 0.0f,
            id = BookId("3"),
            remainingTime = "12h 55m",
            genre = "Mystery",
            duration = "12h 55m",
            category = BookOverviewCategory.NOT_STARTED
        )
    )

    MaterialTheme {
        BookSearchScreen(
            viewState = BookSearchViewState.EmptySearch(
                books = mockBooks,
                suggestedAuthors = emptyList(),
                recentQueries = emptyList(),
                query = ""
            ),
            query = "",
            onQueryChange = {},
            onBookClick = {},
            onBookLongClick = {},
            onSettingsClick = {}
        )
    }
}
