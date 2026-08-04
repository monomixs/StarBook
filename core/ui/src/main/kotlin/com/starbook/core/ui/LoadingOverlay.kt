package com.starbook.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.ui.icons.StarBookIcons
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingOverlay(
    isShowing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isShowing,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(1200)), // Slow fade out
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    enabled = true,
                    onClick = {},
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Getting things ready for you",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.01).sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                RotatingStar()
            }
        }
    }
}

@Composable
private fun RotatingStar() {
    val infiniteTransition = rememberInfiniteTransition(label = "StarRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 2.2f else 1f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessLow
        ),
        label = "StarScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(140.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                rotationZ = rotation
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.6f)) {
            val path = createRoundedStarPath(
                cx = size.width / 2,
                cy = size.height / 2,
                outerRadius = size.width / 2,
                innerRadius = size.width / 4,
                points = 5,
                rounding = 15f // Sharpness reduction
            )

            // Draw filled star with rounded corners via Stroke trick + Fill
            drawPath(
                path = path,
                color = primaryColor
            )
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 20f, // Heavy stroke to round out the points
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun createRoundedStarPath(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    innerRadius: Float,
    points: Int,
    rounding: Float
): Path {
    val path = Path()
    val angleStep = Math.PI / points

    for (i in 0 until 2 * points) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - Math.PI / 2
        val x = (cx + cos(angle) * radius).toFloat()
        val y = (cy + sin(angle) * radius).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}
