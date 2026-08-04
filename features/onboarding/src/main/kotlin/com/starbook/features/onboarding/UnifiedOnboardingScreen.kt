package com.starbook.features.onboarding

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.core.common.rootGraphAs
import com.starbook.core.ui.ChoiceCard
import com.starbook.core.ui.SetupBackground
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.onboarding.welcome.OnboardingWelcomeProvider
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun UnifiedOnboardingScreen(initialStep: Int = 1, modifier: Modifier = Modifier) {
    val viewModel = remember {
        rootGraphAs<OnboardingWelcomeProvider>()
            .onboardingWelcomeViewModel
    }

    var currentStep by remember(initialStep) { mutableIntStateOf(initialStep) }
    val totalSteps = 4
    val isDark = isSystemInDarkTheme()

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.onFilePicked(uri)
                currentStep = 4
            }
        }
    )

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.onFolderPicked(uri)
            }
        }
    )

    BackHandler(enabled = currentStep > 1) {
        currentStep--
    }

    val surfaceColor = if (isDark) Color(0xFF232634) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val onSurfaceColor = if (isDark) Color(0xFFECEBF4) else MaterialTheme.colorScheme.onSurface
    val onSurfaceVarColor = if (isDark) Color(0xFFAFB0C4) else MaterialTheme.colorScheme.onSurfaceVariant

    SetupBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            WavebandProgress(
                step = currentStep,
                total = totalSteps,
                isDark = isDark,
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .padding(top = 48.dp, bottom = 8.dp)
            )

            TopBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                onSurfaceVarColor = onSurfaceVarColor,
                onBack = { currentStep-- }
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { it / 2 } + fadeIn()).togetherWith(fadeOut())
                        } else {
                            (slideInVertically { -it / 2 } + fadeIn()).togetherWith(fadeOut())
                        }
                    },
                    label = "stepContent"
                ) { step ->
                    when (step) {
                        1 -> WelcomeStep(
                            onSurfaceColor = onSurfaceColor,
                            onSurfaceVarColor = onSurfaceVarColor,
                            onNext = { currentStep++ }
                        )
                        2 -> ExplanationStep(
                            onSurfaceColor = onSurfaceColor,
                            onSurfaceVarColor = onSurfaceVarColor,
                            onNext = { currentStep++ }
                        )
                        3 -> ChoiceStep(
                            onSurfaceColor = onSurfaceColor,
                            onSurfaceVarColor = onSurfaceVarColor,
                            surfaceColor = surfaceColor,
                            onFileClick = { fileLauncher.launch(arrayOf("audio/*")) },
                            onFolderClick = { folderLauncher.launch(null) }
                        )
                        4 -> CompletionStep(
                            onSurfaceColor = onSurfaceColor,
                            onSurfaceVarColor = onSurfaceVarColor,
                            onStart = { viewModel.startListening() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WavebandProgress(step: Int, total: Int, isDark: Boolean, modifier: Modifier = Modifier) {
    val barCount = 30
    val accentColor = when (step) {
        1 -> Color(0xFFB7C4FF)
        2 -> Color(0xFFFFB68C)
        3 -> Color(0xFF8FE3C4)
        else -> Color(0xFFB7C4FF)
    }

    Row(
        modifier = modifier.height(30.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { i ->
            val h = 6.dp + (abs(sin(i * 0.85 + 0.4)) * 24).dp
            val isFilled = i < (step.toFloat() / total * barCount)
            val baseColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
            val color by animateColorAsState(if (isFilled) accentColor else baseColor, label = "wave")

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun TopBar(
    currentStep: Int,
    totalSteps: Int,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onSurfaceVarColor: Color,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 1) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(surfaceColor)
            ) {
                Icon(
                    imageVector = StarBookIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = onSurfaceColor
                )
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }

        Text(
            text = "STEP $currentStep OF $totalSteps",
            style = MaterialTheme.typography.labelSmall,
            color = onSurfaceVarColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun WelcomeStep(onSurfaceColor: Color, onSurfaceVarColor: Color, onNext: () -> Unit) {
    StepLayout(
        icon = StarBookIcons.Star,
        title = "Welcome to StarBook!",
        subtitle = "Your personal pocket audiobook player. Let's get you set up.",
        onNext = onNext,
        accentColor = Color(0xFFB7C4FF),
        onSurfaceColor = onSurfaceColor,
        onSurfaceVarColor = onSurfaceVarColor
    )
}

@Composable
private fun ExplanationStep(onSurfaceColor: Color, onSurfaceVarColor: Color, onNext: () -> Unit) {
    StepLayout(
        icon = StarBookIcons.LibraryBooks,
        title = "Your Library, Your Way",
        subtitle = "With StarBook, you can add books as individual files or organize them in folders. You choose!",
        onNext = onNext,
        accentColor = Color(0xFFFFB68C),
        onSurfaceColor = onSurfaceColor,
        onSurfaceVarColor = onSurfaceVarColor
    )
}

@Composable
private fun ChoiceStep(
    onSurfaceColor: Color,
    onSurfaceVarColor: Color,
    surfaceColor: Color,
    onFileClick: () -> Unit,
    onFolderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        Text(
            text = "Add Your First Audiobook",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceColor
        )
        Text(
            text = "Would you like to add a single file as a book or a folder?",
            style = MaterialTheme.typography.bodyLarge,
            color = onSurfaceVarColor,
            modifier = Modifier.padding(top = 14.dp)
        )

        Spacer(Modifier.weight(0.2f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChoiceCard(
                label = "Folder",
                icon = StarBookIcons.Folder,
                accentColor = Color(0xFF8FE3C4),
                containerColor = Color(0xFF1E4C3C),
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                onClick = onFolderClick,
                modifier = Modifier.weight(1f)
            )
            ChoiceCard(
                label = "File",
                icon = StarBookIcons.AudioFile,
                accentColor = Color(0xFF8FE3C4),
                containerColor = Color(0xFF1E4C3C),
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                onClick = onFileClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun CompletionStep(onSurfaceColor: Color, onSurfaceVarColor: Color, onStart: () -> Unit) {
    StepLayout(
        icon = StarBookIcons.Check,
        title = "You're All Set!",
        subtitle = "Your first audiobook is ready to play. Enjoy your journey with StarBook!",
        onNext = onStart,
        nextLabel = "Start Listening",
        nextIcon = StarBookIcons.NotStarted,
        accentColor = Color(0xFFB7C4FF),
        onSurfaceColor = onSurfaceColor,
        onSurfaceVarColor = onSurfaceVarColor
    )
}

@Composable
private fun StepLayout(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onNext: () -> Unit,
    accentColor: Color,
    onSurfaceColor: Color,
    onSurfaceVarColor: Color,
    nextLabel: String = "Next",
    nextIcon: ImageVector = StarBookIcons.ArrowForward
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            BlobIcon(icon = icon, accentColor = accentColor)
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceColor
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = onSurfaceVarColor,
            modifier = Modifier.padding(top = 14.dp)
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.End)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color(0xFF132464)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = nextLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.width(8.dp))
            Icon(imageVector = nextIcon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun BlobIcon(icon: ImageVector, accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "blob")
    val borderRadius by transition.animateValue(
        initialValue = 46,
        targetValue = 61,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    Box(
        modifier = Modifier
            .size(176.dp)
            .clip(RoundedCornerShape(borderRadius.dp))
            .background(
                Brush.linearGradient(
                    listOf(accentColor.copy(alpha = 0.4f), accentColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(66.dp),
            tint = Color.White
        )
    }
}
