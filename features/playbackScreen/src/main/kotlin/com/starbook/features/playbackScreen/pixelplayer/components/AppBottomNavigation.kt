package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.navigation.Destination
import kotlin.math.absoluteValue

@Composable
fun AppBottomNavigation(
    selectedTab: Destination.Tab,
    pillPositionFraction: Float,
    modifier: Modifier = Modifier,
    isMiniPlayerActive: Boolean = false,
    onTabClick: (Destination.Tab) -> Unit,
) {
    val topCornerRadius = if (isMiniPlayerActive) 16.dp else 32.dp
    val density = LocalDensity.current

    val itemPositions = remember { mutableStateListOf<Dp>() }
    if (itemPositions.isEmpty()) {
        repeat(3) { itemPositions.add(0.dp) }
    }

    val tabs = remember {
        listOf(Destination.Tab.HOME, Destination.Tab.SEARCH, Destination.Tab.LIBRARY)
    }

    val pillWidth = 108.dp

    // Interaction states
    var isDragging by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var manualTargetIndex by remember { mutableIntStateOf(-1) }

    val currentTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    // Reset manual target only when the actual selection matches the target
    LaunchedEffect(selectedTab) {
        val actualIndex = tabs.indexOf(selectedTab)
        if (actualIndex == manualTargetIndex) {
            manualTargetIndex = -1
        }
    }

    // Base position from Pager
    val basePillOffset = remember(pillPositionFraction, itemPositions.toList()) {
        if (itemPositions.isEmpty()) 0.dp else {
            val floorIndex = pillPositionFraction.toInt().coerceIn(0, 2)
            val ceilIndex = (floorIndex + 1).coerceIn(0, 2)
            val fraction = pillPositionFraction - floorIndex

            val startPos = itemPositions[floorIndex]
            val endPos = itemPositions[ceilIndex]

            (startPos + (endPos - startPos) * fraction) - (pillWidth / 2)
        }
    }

    // Target position for when we are letting go of a drag or clicking
    val intendedTabOffset = remember(manualTargetIndex, currentTabIndex, itemPositions.toList()) {
        val target = if (manualTargetIndex != -1) manualTargetIndex else currentTabIndex
        if (itemPositions.size > target) {
            itemPositions[target] - (pillWidth / 2)
        } else 0.dp
    }

    val totalPillOffset = when {
        isDragging -> basePillOffset + with(density) { dragOffsetPx.toDp() }
        manualTargetIndex != -1 -> intendedTabOffset
        else -> basePillOffset
    }

    val animatedPillOffset by animateDpAsState(
        targetValue = totalPillOffset,
        animationSpec = if (isDragging) {
            snap()
        } else {
            spring(
                dampingRatio = 0.65f,
                stiffness = SpringStiffness
            )
        },
        label = "PillOffset"
    )

    // Calculate current visual fraction based on animated offset to drive label visibility
    val currentVisualFraction = remember(animatedPillOffset, itemPositions.toList()) {
        if (itemPositions.size < 2) 0f
        else {
            val start = itemPositions.first() - (pillWidth / 2)
            val end = itemPositions.last() - (pillWidth / 2)
            val totalDist = (end - start).value
            if (totalDist == 0f) 0f
            else {
                ((animatedPillOffset - start).value / totalDist) * (tabs.size - 1)
            }
        }
    }

    // Scaling Animation
    val isMoving = remember(pillPositionFraction) {
        val fractionPart = pillPositionFraction % 1.0f
        fractionPart > 0.02f && fractionPart < 0.98f
    }

    val pillScale by animateFloatAsState(
        targetValue = if (isPressed || isDragging || isMoving) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "PillScale"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 16.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(
            topStart = topCornerRadius,
            topEnd = topCornerRadius,
            bottomStart = 32.dp,
            bottomEnd = 32.dp
        ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(itemPositions.toList(), basePillOffset) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                        val currentPillX = animatedPillOffset.toPx()
                        val pillWidthPx = pillWidth.toPx()

                        // Hit test: check if touch is inside the pill area
                        if (down.position.x >= currentPillX && down.position.x <= currentPillX + pillWidthPx) {
                            isPressed = true
                            dragOffsetPx = 0f
                            down.consume() // Intercept from buttons

                            var dragActive = false
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val changes = event.changes

                                if (changes.any { !it.pressed }) {
                                    // Release
                                    isPressed = false
                                    if (dragActive) {
                                        isDragging = false
                                        if (itemPositions.isNotEmpty()) {
                                            val finalOffsetDp = animatedPillOffset + (pillWidth / 2)
                                            val closestIndex = itemPositions.indices.minBy { index ->
                                                (itemPositions[index] - finalOffsetDp).value.absoluteValue
                                            }
                                            manualTargetIndex = closestIndex
                                            onTabClick(tabs[closestIndex])
                                        }
                                    }
                                    break
                                }

                                val moveAmount = changes.first().position.x - changes.first().previousPosition.x
                                if (moveAmount.absoluteValue > 0.1f) {
                                    dragActive = true
                                    isDragging = true

                                    val minX = (itemPositions.first() - (pillWidth / 2)).toPx()
                                    val maxX = (itemPositions.last() - (pillWidth / 2)).toPx()
                                    val basePx = basePillOffset.toPx()

                                    val newTotalPx = (basePx + dragOffsetPx + moveAmount).coerceIn(minX, maxX)
                                    dragOffsetPx = newTotalPx - basePx

                                    changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabLabels = listOf(
                    Triple(Destination.Tab.HOME, "Home", StarBookIcons.Home),
                    Triple(Destination.Tab.SEARCH, "Search", StarBookIcons.Search),
                    Triple(Destination.Tab.LIBRARY, "Library", StarBookIcons.LibraryBooks)
                )

                tabLabels.forEachIndexed { index, (tab, label, icon) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned {
                                val parent = it.parentLayoutCoordinates!!
                                val center = it.size.width / 2f
                                val offset = parent.localPositionOf(it, Offset(center, 0f))
                                itemPositions[index] = with(density) { offset.x.toDp() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        NavItem(
                            selected = (currentVisualFraction - index.toFloat()).absoluteValue < 0.25f,
                            label = label,
                            icon = icon,
                        ) {
                            manualTargetIndex = index
                            onTabClick(tab)
                        }
                    }
                }
            }

            // Pill Indicator - Above items
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedPillOffset.roundToPx(), 0) }
                    .align(Alignment.CenterStart)
                    .width(pillWidth)
                    .height(48.dp)
                    .graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
            )
        }
    }
}

private const val SpringStiffness = 600f

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavItem(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .height(48.dp)
            .then(
                if (selected) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        onClick = if (!selected) onClick else ({}),
        color = Color.Transparent,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = SpringStiffness),
                    expandFrom = Alignment.Start
                ) + fadeIn(tween(300)) + scaleIn(initialScale = 0.8f),
                exit = shrinkHorizontally(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = SpringStiffness),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(tween(300)) + scaleOut(targetScale = 0.8f) + slideOutHorizontally { -20 }
            ) {
                val transition = updateTransition(targetState = selected, label = "LabelTransition")
                val blurRadius by transition.animateDp(
                    transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
                    label = "Blur"
                ) { state ->
                    if (state) 0.dp else 16.dp // Heavy blur on hide
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .blur(blurRadius)
                            .graphicsLayer {
                                scaleX = if (selected) 1f else 0.8f
                                scaleY = if (selected) 1f else 0.8f
                            }
                    )
                }
            }
        }
    }
}
