package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.navigation.Destination


@Composable
fun AppBottomNavigation(
    selectedTab: Destination.Tab,
    modifier: Modifier = Modifier,
    isMiniPlayerActive: Boolean = false,
    onTabClick: (Destination.Tab) -> Unit,
) {
    val topCornerRadius = if (isMiniPlayerActive) 16.dp else 32.dp
    val density = LocalDensity.current

    var containerWidth by remember { mutableStateOf(0.dp) }
    val itemPositions = remember { mutableStateListOf<Dp>() }
    if (itemPositions.isEmpty()) {
        repeat(3) { itemPositions.add(0.dp) }
    }

    val selectedIndex = when (selectedTab) {
        Destination.Tab.HOME -> 0
        Destination.Tab.SEARCH -> 1
        Destination.Tab.LIBRARY -> 2
    }

    val pillWidth = 108.dp
    val animatedPillOffset by animateDpAsState(
        targetValue = if (itemPositions.isNotEmpty()) itemPositions[selectedIndex] - (pillWidth / 2) else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = SpringStiffness
        ),
        label = "PillOffset"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 16.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(80.dp)
            .onGloballyPositioned {
                containerWidth = with(density) { it.size.width.toDp() }
            },
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
            modifier = Modifier.fillMaxSize()
        ) {
            // Pill Indicator
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedPillOffset.roundToPx(), 0) }
                    .align(Alignment.CenterStart)
                    .width(pillWidth)
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple(Destination.Tab.HOME, "Home", StarBookIcons.Home),
                    Triple(Destination.Tab.SEARCH, "Search", StarBookIcons.Search),
                    Triple(Destination.Tab.LIBRARY, "Library", StarBookIcons.LibraryBooks)
                )

                tabs.forEachIndexed { index, (tab, label, icon) ->
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
                            selected = selectedTab == tab,
                            label = label,
                            icon = icon,
                        ) { onTabClick(tab) }
                    }
                }
            }
        }
    }
}

private const val SpringStiffness = 600f

@Composable
private fun NavItem(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            if (isDark) Color.Black else MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "ContentColor"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(300),
        label = "LabelAlpha"
    )

    val labelBlur by animateDpAsState(
        targetValue = if (selected) 0.dp else 8.dp,
        animationSpec = tween(350),
        label = "LabelBlur"
    )


    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = contentColor,
        shape = CircleShape,
        modifier = Modifier
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = SpringStiffness
                    )
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )

            if (selected) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = labelAlpha
                        }
                        .blur(labelBlur)
                )
            }
        }
    }
}
