package com.starbook.features.playbackScreen.pixelplayer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MiniPlayerHeight = 64.dp
const val ANIMATION_DURATION_MS = 255

enum class PlayerSheetState {
  COLLAPSED,
  EXPANDED,
}

object NavBarStyle {
  const val DEFAULT = "DEFAULT"
  const val FULL_WIDTH = "FULL_WIDTH"
}

object PixelPlayerIcons {
  val FastForward: ImageVector = ImageVector.Builder(
    name = "FastForward",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 26f,
    viewportHeight = 26f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(13.312f, 5.125f)
      arcTo(1.33f, 1.33f, 0f, false, true, 13.906f, 5.25f)
      lineTo(22.22f, 11.844f)
      curveTo(22.62f, 12.22f, 22.939f, 12.515f, 22.939f, 13f)
      reflectiveCurveTo(22.668f, 13.775f, 22.22f, 14.156f)
      lineTo(13.908f, 20.75f)
      curveTo(13.505f, 20.953f, 13.011f, 20.92f, 12.627f, 20.687f)
      curveTo(12.241f, 20.455f, 12.001f, 20.037f, 12.001f, 19.594f)
      verticalLineToRelative(-3.688f)
      lineToRelative(-6.094f, 4.844f)
      curveTo(5.504f, 21.953f, 5.01f, 21.92f, 4.626f, 21.687f)
      curveTo(4.24f, 21.455f, 4.0f, 21.037f, 4.0f, 20.594f)
      verticalLineTo(6.406f)
      curveTo(4.0f, 5.963f, 4.239f, 5.546f, 4.625f, 5.313f)
      arcTo(1.33f, 1.33f, 0f, false, true, 5.313f, 5.125f)
      arcTo(1.33f, 1.33f, 0f, false, true, 5.906f, 5.25f)
      lineTo(12f, 10.094f)
      verticalLineTo(6.406f)
      curveTo(12.0f, 5.963f, 12.239f, 5.546f, 12.625f, 5.313f)
      arcTo(1.33f, 1.33f, 0f, false, true, 13.313f, 5.125f)
      close()
    }
  }.build()

  val FastRewind: ImageVector = ImageVector.Builder(
    name = "FastRewind",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 26f,
    viewportHeight = 26f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(12.688f, 5.125f)
      arcTo(1.33f, 1.33f, 0f, false, false, 12.094f, 5.25f)
      lineTo(3.78f, 11.844f)
      curveTo(3.38f, 12.22f, 3.061f, 12.515f, 3.061f, 13f)
      reflectiveCurveTo(3.332f, 13.775f, 3.78f, 14.156f)
      lineTo(12.092f, 20.75f)
      curveTo(12.495f, 20.953f, 12.989f, 20.92f, 13.373f, 20.687f)
      curveTo(13.759f, 20.455f, 13.999f, 20.037f, 13.999f, 19.594f)
      verticalLineToRelative(-3.688f)
      lineToRelative(6.094f, 4.844f)
      curveTo(20.496f, 21.953f, 20.99f, 21.92f, 21.374f, 21.687f)
      curveTo(21.76f, 21.455f, 22.0f, 21.037f, 22.0f, 20.594f)
      verticalLineTo(6.406f)
      curveTo(22.0f, 5.963f, 21.761f, 5.546f, 21.375f, 5.313f)
      arcTo(1.33f, 1.33f, 0f, false, false, 20.687f, 5.125f)
      arcTo(1.33f, 1.33f, 0f, false, false, 20.094f, 5.25f)
      lineTo(14f, 10.094f)
      verticalLineTo(6.406f)
      curveTo(14.0f, 5.963f, 13.761f, 5.546f, 13.375f, 5.313f)
      arcTo(1.33f, 1.33f, 0f, false, false, 12.687f, 5.125f)
      close()
    }
  }.build()

  val PlayArrow: ImageVector = ImageVector.Builder(
    name = "PlayArrow",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 898f,
    viewportHeight = 1026f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(62.397f, 8f)
      lineTo(881.397f, 475f)
      quadToRelative(16f, 9f, 16f, 39.5f)
      reflectiveQuadToRelative(-16f, 37.5f)
      lineToRelative(-819f, 467f)
      quadToRelative(-12f, 8f, -30f, 5.5f)
      reflectiveQuadToRelative(-32f, -17.5f)
      verticalLineTo(22f)
      quadToRelative(31f, -34f, 62f, -14f)
      close()
    }
  }.build()

  val Pause: ImageVector = ImageVector.Builder(
    name = "Pause",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 1025f,
    viewportHeight = 1024f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(896.428f, 1024f)
      horizontalLineToRelative(-128f)
      quadToRelative(-53f, 0f, -90.5f, -37.5f)
      reflectiveQuadToRelative(-37.5f, -90.5f)
      verticalLineTo(128f)
      quadToRelative(0f, -53f, 37.5f, -90.5f)
      reflectiveQuadToRelative(90.5f, -37.5f)
      horizontalLineToRelative(128f)
      quadToRelative(53f, 0f, 90.5f, 37.5f)
      reflectiveQuadToRelative(37.5f, 90.5f)
      verticalLineToRelative(768f)
      quadToRelative(0f, 53f, -37.5f, 90.5f)
      reflectiveQuadToRelative(-90.5f, 37.5f)
      close()
      moveToRelative(-640f, 0f)
      horizontalLineToRelative(-128f)
      quadToRelative(-53f, 0f, -90.5f, -37.5f)
      reflectiveQuadTo(0.428f, 896f)
      verticalLineTo(128f)
      quadToRelative(0f, -53f, 37.5f, -90.5f)
      reflectiveQuadToRelative(90.5f, -37.5f)
      horizontalLineToRelative(128f)
      quadToRelative(53f, 0f, 90.5f, 37.5f)
      reflectiveQuadToRelative(37.5f, 90.5f)
      verticalLineToRelative(768f)
      quadToRelative(0f, 53f, -37.5f, 90.5f)
      reflectiveQuadToRelative(-90.5f, 37.5f)
      close()
    }
  }.build()

  val KeyboardArrowDown: ImageVector = ImageVector.Builder(
    name = "KeyboardArrowDown",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(7.41f, 8.59f)
      lineTo(12f, 13.17f)
      lineToRelative(4.59f, -4.58f)
      lineTo(18f, 10f)
      lineToRelative(-6f, 6f)
      lineToRelative(-6f, -6f)
      lineToRelative(1.41f, -1.41f)
      close()
    }
  }.build()

  val QueueMusic: ImageVector = ImageVector.Builder(
    name = "QueueMusic",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(7f, 14f)
      horizontalLineToRelative(2f)
      quadToRelative(0.425f, 0f, 0.713f, -0.288f)
      reflectiveQuadTo(10f, 13f)
      reflectiveQuadToRelative(-0.288f, -0.712f)
      reflectiveQuadTo(9f, 12f)
      horizontalLineTo(7f)
      quadToRelative(-0.425f, 0f, -0.712f, 0.288f)
      reflectiveQuadTo(6f, 13f)
      reflectiveQuadToRelative(0.288f, 0.713f)
      reflectiveQuadTo(7f, 14f)
      moveToRelative(12f, -2f)
      quadToRelative(-1.25f, 0f, -2.125f, -0.875f)
      reflectiveQuadTo(16f, 9f)
      reflectiveQuadToRelative(0.875f, -2.125f)
      reflectiveQuadTo(19f, 6f)
      quadToRelative(0.275f, 0f, 0.525f, 0.05f)
      reflectiveQuadToRelative(0.475f, 0.125f)
      verticalLineTo(2f)
      quadToRelative(0f, -0.425f, 0.288f, -0.712f)
      reflectiveQuadTo(21f, 1f)
      horizontalLineToRelative(2f)
      quadToRelative(0.425f, 0f, 0.713f, 0.288f)
      reflectiveQuadTo(24f, 2f)
      reflectiveQuadToRelative(-0.288f, 0.713f)
      reflectiveQuadTo(23f, 3f)
      horizontalLineToRelative(-1f)
      verticalLineToRelative(6f)
      quadToRelative(0f, 1.25f, -0.875f, 2.125f)
      reflectiveQuadTo(19f, 12f)
      moveTo(7f, 11f)
      horizontalLineToRelative(5f)
      quadToRelative(0.425f, 0f, 0.713f, -0.288f)
      reflectiveQuadTo(13f, 10f)
      reflectiveQuadToRelative(-0.288f, -0.712f)
      reflectiveQuadTo(12f, 9f)
      horizontalLineTo(7f)
      quadToRelative(-0.425f, 0f, -0.712f, 0.288f)
      reflectiveQuadTo(6f, 10f)
      reflectiveQuadToRelative(0.288f, 0.713f)
      reflectiveQuadTo(7f, 11f)
      moveToRelative(0f, -3f)
      horizontalLineToRelative(5f)
      quadToRelative(0.425f, 0f, 0.713f, -0.288f)
      reflectiveQuadTo(13f, 7f)
      reflectiveQuadToRelative(-0.288f, -0.712f)
      reflectiveQuadTo(12f, 6f)
      horizontalLineTo(7f)
      quadToRelative(-0.425f, 0f, -0.712f, 0.288f)
      reflectiveQuadTo(6f, 7f)
      reflectiveQuadToRelative(0.288f, 0.713f)
      reflectiveQuadTo(7f, 8f)
      moveTo(6f, 18f)
      lineToRelative(-2.3f, 2.3f)
      quadToRelative(-0.475f, 0.475f, -1.088f, 0.213f)
      reflectiveQuadTo(2f, 19.575f)
      verticalLineTo(4f)
      quadToRelative(0f, -0.825f, 0.588f, -1.412f)
      reflectiveQuadTo(4f, 2f)
      horizontalLineToRelative(11f)
      quadToRelative(0.775f, 0f, 1.363f, 0.475f)
      reflectiveQuadTo(16.95f, 3.7f)
      quadToRelative(0f, 0.35f, -0.162f, 0.625f)
      reflectiveQuadToRelative(-0.438f, 0.45f)
      quadToRelative(-1.1f, 0.675f, -1.725f, 1.8f)
      reflectiveQuadTo(14f, 9f)
      quadToRelative(0f, 1.35f, 0.663f, 2.5f)
      reflectiveQuadTo(16.5f, 13.325f)
      quadToRelative(0.55f, 0.325f, 0.875f, 0.863f)
      reflectiveQuadToRelative(0.325f, 1.187f)
      quadToRelative(0f, 1.125f, -0.788f, 1.875f)
      reflectiveQuadTo(15f, 18f)
      close()
    }
  }.build()

  val MoreVert: ImageVector = ImageVector.Builder(
    name = "MoreVert",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(12f, 8f)
      curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
      reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
      reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
      reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
      close()
      moveToRelative(0f, 2f)
      curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
      reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
      reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
      reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
      close()
      moveToRelative(0f, 6f)
      curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
      reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
      reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
      reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
      close()
    }
  }.build()

  val SkipNext: ImageVector = ImageVector.Builder(
    name = "SkipNext",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(6f, 18f)
      lineToRelative(8.5f, -6f)
      lineTo(6f, 6f)
      verticalLineToRelative(12f)
      close()
      moveTo(16f, 6f)
      verticalLineToRelative(12f)
      horizontalLineToRelative(2f)
      verticalLineTo(6f)
      horizontalLineToRelative(-2f)
      close()
    }
  }.build()

  val SkipPrevious: ImageVector = ImageVector.Builder(
    name = "SkipPrevious",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(6f, 6f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(12f)
      horizontalLineTo(6f)
      verticalLineTo(6f)
      close()
      moveToRelative(3.5f, 6f)
      lineTo(18f, 18f)
      verticalLineTo(6f)
      lineToRelative(-8.5f, 6f)
      close()
    }
  }.build()
}
