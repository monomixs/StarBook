package com.starbook.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveProgressBar(
  progress: Float,
  color: Color,
  modifier: Modifier = Modifier,
  strokeWidth: Float = 6f,
) {
  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val width = size.width
      val height = size.height
      val mid = height / 2
      val amplitude = 4f
      val periods = 4f
      val steps = 64

      fun buildWavePath(): Path {
        val path = Path()
        for (i in 0..steps) {
          val t = i.toFloat() / steps
          val x = t * width
          val y = mid + sin(t * periods * PI.toFloat() * 2) * amplitude
          if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        return path
      }

      val path = buildWavePath()

      // Draw background track
      drawPath(
        path = path,
        color = color.copy(alpha = 0.2f),
        style = Stroke(width = strokeWidth)
      )

      // Draw progress with clipping
      clipRect(right = width * progress) {
        drawPath(
          path = path,
          color = color,
          style = Stroke(width = strokeWidth)
        )
      }
    }
  }
}
