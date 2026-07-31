package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AutoScrollingTextOnDemand(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    gradientEdgeColor: Color = Color.Transparent,
    expansionFractionProvider: () -> Float = { 1f },
    canScroll: Boolean = true
) {
    var hasOverflow by remember(text) { mutableStateOf(false) }

    // Simplification: always use basicMarquee if it overflows and canScroll is true
    Box(modifier = modifier) {
        SubcomposeLayout { constraints ->
            val content = subcompose("measurer") {
                Text(text = text, style = style, maxLines = 1, softWrap = false)
            }.first().measure(constraints.copy(minWidth = 0, maxWidth = Int.MAX_VALUE))

            hasOverflow = content.width > constraints.maxWidth

            val placeable = subcompose("content") {
                if (hasOverflow && canScroll && expansionFractionProvider() > 0.99f) {
                    AutoScrollingText(
                        text = text,
                        style = style,
                        gradientEdgeColor = gradientEdgeColor
                    )
                } else {
                    Text(
                        text = text,
                        style = style,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }.first().measure(constraints)

            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }
}

@Composable
fun AutoScrollingText(
    text: String,
    style: TextStyle,
    gradientEdgeColor: Color,
    gradientWidth: Dp = 12.dp
) {
    val color = style.color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current

    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .fillMaxWidth()
            .basicMarquee(
                iterations = Int.MAX_VALUE,
                initialDelayMillis = 2000
            )
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                val width = size.width
                val height = size.height
                if (width > 0 && height > 0) {
                    val gradientWidthPx = gradientWidth.toPx()
                    // Simple right fade for now
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Black,
                            (width - gradientWidthPx) / width to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
            }
    )
}
