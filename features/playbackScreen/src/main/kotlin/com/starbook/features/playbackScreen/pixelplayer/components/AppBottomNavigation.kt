package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.navigation.Destination

@Composable
fun AppBottomNavigation(
    selectedTab: Destination.Tab,
    isMiniPlayerActive: Boolean = false,
    modifier: Modifier = Modifier,
    onTabClick: (Destination.Tab) -> Unit
) {
    val topCornerRadius = if (isMiniPlayerActive) 16.dp else 32.dp

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
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    selected = selectedTab == Destination.Tab.HOME,
                    label = "Home",
                    icon = StarBookIcons.Home,
                    onClick = { onTabClick(Destination.Tab.HOME) }
                )
                NavItem(
                    selected = selectedTab == Destination.Tab.SEARCH,
                    label = "Search",
                    icon = StarBookIcons.Search,
                    onClick = { onTabClick(Destination.Tab.SEARCH) }
                )
                NavItem(
                    selected = selectedTab == Destination.Tab.LIBRARY,
                    label = "Library",
                    icon = StarBookIcons.LibraryBooks,
                    onClick = { onTabClick(Destination.Tab.LIBRARY) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val contentColor = if (selected) {
        if (isDark) Color.Black else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        shape = CircleShape,
        modifier = Modifier.height(48.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp))
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Text(text = label, fontSize = 12.sp, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
