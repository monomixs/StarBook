package com.starbook.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.starbook.features.playbackScreen.pixelplayer.UnifiedPlayerSheetV2
import com.starbook.features.playbackScreen.pixelplayer.PixelPlayerViewModel
import com.starbook.features.playbackScreen.pixelplayer.PlayerSheetState
import com.starbook.features.playbackScreen.pixelplayer.components.AppBottomNavigation
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import com.starbook.app.navigation.BottomSheetSceneStrategy
import com.starbook.app.navigation.NavEntryResolver
import com.starbook.app.navigation.StartDestinationProvider
import com.starbook.core.analytics.api.Analytics
import com.starbook.core.common.rootGraphAs
import com.starbook.core.data.ThemeColorScheme
import com.starbook.core.data.ThemeMode
import com.starbook.core.data.store.ThemeColorSchemeStore
import com.starbook.core.data.store.ThemeModeStore
import com.starbook.core.logging.api.Logger
import com.starbook.core.ui.LocalSharedTransitionScope
import com.starbook.core.ui.StarBookTheme
import com.starbook.core.ui.GlobalLoadingState
import com.starbook.core.ui.LoadingOverlay
import com.starbook.features.review.ReviewFeature
import com.starbook.navigation.Destination
import com.starbook.navigation.NavigationCommand
import com.starbook.navigation.Navigator
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@ContributesTo(AppScope::class)
interface MainActivityGraph {
  fun inject(activity: MainActivity)
}

class MainActivity : AppCompatActivity() {

  @Inject
  private lateinit var navigator: Navigator

  @Inject
  lateinit var navEntryResolver: NavEntryResolver

  @Inject
  private lateinit var startDestinationProvider: StartDestinationProvider

  @Inject
  private lateinit var analytics: Analytics

  @Inject
  private lateinit var pixelPlayerViewModel: PixelPlayerViewModel

  @Inject
  private lateinit var mediaScanTrigger: com.starbook.core.scanner.MediaScanTrigger

  @Inject
  private lateinit var globalLoadingState: GlobalLoadingState

  @Inject
  @ThemeModeStore
  private lateinit var themeModeStore: DataStore<ThemeMode>

  @Inject
  @ThemeColorSchemeStore
  private lateinit var themeColorSchemeStore: DataStore<ThemeColorScheme>

  @OptIn(ExperimentalSharedTransitionApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    rootGraphAs<MainActivityGraph>().inject(this)
    super.onCreate(savedInstanceState)

    enableEdgeToEdge(
      navigationBarStyle = SystemBarStyle.auto(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT,
      ),
    )
    if (android.os.Build.VERSION.SDK_INT >= 29) {
      window.isNavigationBarContrastEnforced = false
    }

    setContent {
      @Suppress("UNCHECKED_CAST")
      val backStack = rememberNavBackStack(*startDestinationProvider(intent).toTypedArray()) as MutableList<Destination.Compose>
      LaunchedEffect(backStack.last()) {
        analytics.screenView(backStack.last().trackingName)
      }
      val themeMode = themeModeStore.data.collectAsState(initial = null).value
        ?: return@setContent
      val themeColorScheme = themeColorSchemeStore.data.collectAsState(initial = null).value
        ?: return@setContent
      StarBookTheme(
        themeMode = themeMode,
        themeColorScheme = themeColorScheme,
      ) {
        val bottomSheetStrategy = remember { BottomSheetSceneStrategy<Destination.Compose>() }
        val dialogStrategy = remember { DialogSceneStrategy<Destination.Compose>() }

        val currentDestination = backStack.lastOrNull()
        val sheetState by pixelPlayerViewModel.sheetState.collectAsState()
        val stablePlayerState by pixelPlayerViewModel.stablePlayerState.collectAsState()

        val isScanningRaw by mediaScanTrigger.scannerActive.collectAsState(false)
        val isGlobalLoading by globalLoadingState.isShowing.collectAsState()

        var isScanning by remember { mutableStateOf(false) }
        LaunchedEffect(isScanningRaw) {
            if (isScanningRaw) {
                kotlinx.coroutines.delay(600)
            }
            isScanning = isScanningRaw
        }

        val selectedTab = remember(currentDestination) {
            when (currentDestination) {
                is Destination.Home -> Destination.Tab.HOME
                is Destination.Search -> Destination.Tab.SEARCH
                is Destination.BookOverview -> Destination.Tab.LIBRARY
                else -> Destination.Tab.LIBRARY
            }
        }

        val mainTabs = remember {
            listOf(Destination.Home, Destination.Search, Destination.BookOverview)
        }
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { mainTabs.size }
        )
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(currentDestination) {
            val index = mainTabs.indexOfFirst { it::class == currentDestination?.let { it::class } }
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(
                    page = index,
                    animationSpec = spring(
                        dampingRatio = 0.65f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }

        LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
            if (!pagerState.isScrollInProgress) {
                val targetDest = mainTabs[pagerState.currentPage]
                if (currentDestination?.let { it::class } != targetDest::class) {
                    backStack.clear()
                    backStack.add(Destination.Home)
                    if (targetDest != Destination.Home) {
                        backStack.add(targetDest)
                    }
                }
            }
        }

        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = currentDestination in mainTabs
          ) { page ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                  val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                  val settle = 1f - pageOffset.coerceIn(0f, 1f)

                  val scale = lerp(0.94f, 1f, settle)
                  scaleX = scale
                  scaleY = scale
                  alpha = lerp(0.4f, 1f, settle)
                }
            ) {
              when (page) {
                0 -> com.starbook.features.bookOverview.views.BookOverviewScreen(Destination.Tab.HOME)
                1 -> com.starbook.features.bookOverview.views.BookOverviewScreen(Destination.Tab.SEARCH)
                2 -> com.starbook.features.bookOverview.views.BookOverviewScreen(Destination.Tab.LIBRARY)
              }
            }
          }

          val isTabActive = currentDestination in mainTabs
          if (!isTabActive) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
                .background(MaterialTheme.colorScheme.background)
            ) {
              SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                  NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(bottomSheetStrategy, dialogStrategy),
                    sharedTransitionScope = this,
                    transitionSpec = {
                      val forward = true
                      StarBookEnterTransition(forward) togetherWith StarBookExitTransition(forward)
                    },
                    popTransitionSpec = {
                      StarBookEnterTransition(false) togetherWith StarBookExitTransition(false)
                    },
                    predictivePopTransitionSpec = {
                      StarBookEnterTransition(false) togetherWith StarBookExitTransition(false)
                    },
                    onBack = {
                      if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                      }
                    },
                    entryProvider = { key ->
                      navEntryResolver.create(key)
                    },
                  )
                }
              }
            }
          }

          BoxWithConstraints(
            modifier = Modifier
              .fillMaxSize()
              .zIndex(3f)
          ) {
            val screenHeight = maxHeight
            val miniPlayerHeight = 64.dp
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            val showBottomNav = currentDestination is Destination.Home ||
                               currentDestination is Destination.Search ||
                               currentDestination is Destination.BookOverview

            val bottomNavHeight = 80.dp + 16.dp + navBarPadding

            val persistentUiOffset by animateDpAsState(
                targetValue = if (showBottomNav) 0.dp else bottomNavHeight + miniPlayerHeight + 20.dp,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                label = "PersistentUiOffset"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, persistentUiOffset.roundToPx()) }
            ) {
                val pillFraction by remember {
                    derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction }
                }
                AppBottomNavigation(
                    selectedTab = selectedTab,
                    pillPositionFraction = pillFraction,
                    isMiniPlayerActive = stablePlayerState.currentSong != null,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onTabClick = { tab ->
                        val targetIndex = when (tab) {
                            Destination.Tab.HOME -> 0
                            Destination.Tab.SEARCH -> 1
                            Destination.Tab.LIBRARY -> 2
                        }
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = targetIndex,
                                animationSpec = spring(
                                    dampingRatio = 0.65f,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                )

                UnifiedPlayerSheetV2(
                    playerViewModel = pixelPlayerViewModel,
                    sheetCollapsedTargetY = with(LocalDensity.current) { (screenHeight - miniPlayerHeight - bottomNavHeight - 10.dp).toPx() },
                    containerHeight = screenHeight,
                    onOpenChapters = {
                      pixelPlayerViewModel.openChapterDialog()
                    },
                )
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .background(
                Brush.verticalGradient(
                  listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                )
              )
              .align(Alignment.BottomCenter)
              .zIndex(0.5f)
          )

          LoadingOverlay(
              isShowing = isScanning || isGlobalLoading,
              modifier = Modifier.zIndex(100f)
          )
        }

        LaunchedEffect(navigator) {
          navigator.navigationCommands.collect { command ->
            when (command) {
              is NavigationCommand.GoTo -> {
                when (val destination = command.destination) {
                  is Destination.Compose -> {
                    backStack += destination
                  }
                  is Destination.Activity -> {
                    startActivity(destination.intent)
                  }
                  Destination.BatteryOptimization -> {
                    toBatteryOptimizations()
                  }
                  is Destination.Website -> {
                    try {
                      startActivity(Intent(Intent.ACTION_VIEW, destination.url.toUri()))
                    } catch (exception: ActivityNotFoundException) {
                      Logger.w(exception)
                    }
                  }
                }
              }
              NavigationCommand.GoBack -> {
                if (backStack.size > 1) {
                  backStack.removeLastOrNull()
                }
              }
              is NavigationCommand.SetRoot -> {
                backStack.clear()
                backStack.add(command.root)
              }
            }
          }
        }

        ReviewFeature()

        LaunchedEffect(currentDestination) {
          if (currentDestination is Destination.Playback && sheetState == PlayerSheetState.COLLAPSED) {
            pixelPlayerViewModel.expandPlayerSheet()
            pixelPlayerViewModel.playBook(currentDestination.bookId)
          }
        }

        LaunchedEffect(sheetState) {
          if (sheetState == PlayerSheetState.COLLAPSED && currentDestination is Destination.Playback) {
            navigator.goBack()
          }
        }
      }
    }
  }

  private fun toBatteryOptimizations() {
    val intent = Intent()
      .apply {
        @Suppress("BatteryLife")
        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        data = "package:$packageName".toUri()
      }
    try {
      startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      Logger.w(e, "Can't request ignoring battery optimizations")
    }
  }

  companion object {

    const val NI_GO_TO_BOOK = "niGotoBook"

    fun goToBookIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
      putExtra(NI_GO_TO_BOOK, true)
      flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    }
  }
}
