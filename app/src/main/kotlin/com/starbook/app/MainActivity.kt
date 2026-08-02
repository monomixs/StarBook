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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.starbook.features.playbackScreen.pixelplayer.UnifiedPlayerSheetV2
import com.starbook.features.playbackScreen.pixelplayer.PixelPlayerViewModel
import com.starbook.features.playbackScreen.pixelplayer.PlayerSheetState
import com.starbook.features.playbackScreen.pixelplayer.components.AppBottomNavigation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
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
import com.starbook.features.review.ReviewFeature
import com.starbook.navigation.Destination
import com.starbook.navigation.NavigationCommand
import com.starbook.navigation.Navigator

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
        val isOverlay = currentDestination != null &&
          currentDestination !is Destination.Home &&
          currentDestination !is Destination.Search &&
          currentDestination !is Destination.BookOverview &&
          currentDestination !is Destination.Playback

        val sheetState by pixelPlayerViewModel.sheetState.collectAsState()
        val stablePlayerState by pixelPlayerViewModel.stablePlayerState.collectAsState()

        val selectedTab = remember(currentDestination) {
            when (currentDestination) {
                is Destination.Home -> Destination.Tab.HOME
                is Destination.Search -> Destination.Tab.SEARCH
                is Destination.BookOverview -> Destination.Tab.LIBRARY
                else -> Destination.Tab.LIBRARY
            }
        }

        fun Destination.Compose.tabIndex(): Int {
          return when (this) {
            is Destination.Home -> 0
            is Destination.Search -> 1
            is Destination.BookOverview -> 2
            else -> -1
          }
        }

        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          // Layer 0 or 2: Navigation Content
          Box(
            modifier = Modifier
              .fillMaxSize()
              .zIndex(if (isOverlay) 2f else 0f)
          ) {
            SharedTransitionLayout {
              CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavDisplay(
                  backStack = backStack,
                  sceneStrategies = listOf(bottomSheetStrategy, dialogStrategy),
                  sharedTransitionScope = this,
                  transitionSpec = {
                    val initial = initialState.destination()
                    val target = targetState.destination()
                    val initialTabIndex = initial?.tabIndex() ?: -1
                    val targetTabIndex = target?.tabIndex() ?: -1

                    val forward = when {
                      initialTabIndex != -1 && targetTabIndex != -1 -> targetTabIndex > initialTabIndex
                      else -> true
                    }

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

          // Layer 1: Persistent UI (Navbar + Player)
          BoxWithConstraints(
            modifier = Modifier
              .fillMaxSize()
              .zIndex(1f)
          ) {
            val screenHeight = maxHeight
            val miniPlayerHeight = 64.dp
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            val showBottomNav = currentDestination is Destination.Home ||
                               currentDestination is Destination.Search ||
                               currentDestination is Destination.BookOverview

            val bottomNavHeight = if (showBottomNav) 80.dp + 16.dp + navBarPadding else 0.dp

            // Always render Bottom Navigation if we're on a main screen
            // but the user wants it to be "still there", so we always render it
            // but we might want to hide it if we're deeply nested?
            // "still there but not visible" suggests always in composition.
            if (showBottomNav) {
                AppBottomNavigation(
                    selectedTab = selectedTab,
                    isMiniPlayerActive = stablePlayerState.currentSong != null,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onTabClick = { tab ->
                        if (selectedTab != tab) {
                            when (tab) {
                                Destination.Tab.HOME -> {
                                    backStack.clear()
                                    backStack.add(Destination.Home)
                                }
                                Destination.Tab.SEARCH -> {
                                    backStack.clear()
                                    backStack.add(Destination.Home)
                                    backStack.add(Destination.Search)
                                }
                                Destination.Tab.LIBRARY -> {
                                    backStack.clear()
                                    backStack.add(Destination.Home)
                                    backStack.add(Destination.BookOverview)
                                }
                            }
                        }
                    }
                )
            }

            val showPlayerSheet = currentDestination !is Destination.Settings &&
              currentDestination !is Destination.DeveloperSettings &&
              currentDestination !is Destination.FolderPicker &&
              currentDestination !is Destination.SelectFolderType &&
              currentDestination !is Destination.AddContent &&
              currentDestination !is Destination.MetadataEditor

            if (showPlayerSheet) {
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

          // Bottom gradient behind everything
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
