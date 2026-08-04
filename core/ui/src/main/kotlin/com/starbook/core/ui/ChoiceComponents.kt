package com.starbook.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChoiceCard(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(180.dp)
            .clip(RoundedCornerShape(32.dp)),
        color = surfaceColor,
        border = BorderStroke(2.dp, Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(containerColor, accentColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
            }
            Spacer(Modifier.height(14.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = onSurfaceColor)
        }
    }
}

@Composable
fun SetupBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF08090D) else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    0.0f to Color(0xFFB7C4FF).copy(alpha = if (isDark) 0.16f else 0.12f),
                    0.6f to Color.Transparent,
                    center = androidx.compose.ui.geometry.Offset(0.12f, 0.08f)
                )
            )
            .background(
                Brush.radialGradient(
                    0.0f to Color(0xFFFFB68C).copy(alpha = if (isDark) 0.14f else 0.10f),
                    0.6f to Color.Transparent,
                    center = androidx.compose.ui.geometry.Offset(0.92f, 0.12f)
                )
            )
            .background(
                Brush.radialGradient(
                    0.0f to Color(0xFF8FE3C4).copy(alpha = if (isDark) 0.13f else 0.08f),
                    0.6f to Color.Transparent,
                    center = androidx.compose.ui.geometry.Offset(0.5f, 1.02f)
                )
            )
            .background(backgroundColor),
        content = content
    )
}
