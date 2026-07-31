package com.starbook.core.data.store

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import com.starbook.core.data.BookId
import com.starbook.core.data.GridMode
import com.starbook.core.data.ThemeColorScheme
import com.starbook.core.data.ThemeMode
import com.starbook.core.data.sleeptimer.SleepTimerPreference
import com.starbook.core.featureflag.FeatureFlagOverride
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ContributesTo(AppScope::class)
public interface StoreModule {

  @Provides
  @SingleIn(AppScope::class)
  private fun sharedPreferences(context: Application): SharedPreferences {
    return context.getSharedPreferences(
      "${context.packageName}_preferences",
      Context.MODE_PRIVATE,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @ThemeModeStore
  private fun themeMode(
    factory: StarBookDataStoreFactory,
    application: Application,
    sharedPreferences: SharedPreferences,
  ): DataStore<ThemeMode> {
    return factory.create(
      serializer = ThemeMode.serializer(),
      fileName = "themeMode",
      defaultValue = ThemeMode.FollowSystem,
      migrations = listOf(
        LegacyDarkThemeMigration(application, sharedPreferences),
      ),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @ThemeColorSchemeStore
  private fun themeColorScheme(factory: StarBookDataStoreFactory): DataStore<ThemeColorScheme> {
    return factory.create(
      serializer = ThemeColorScheme.serializer(),
      fileName = "themeColorScheme",
      defaultValue = ThemeColorScheme.StarBookBlue,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @AutoRewindAmountStore
  private fun autoRewindAmount(
    factory: StarBookDataStoreFactory,
    sharedPreferences: SharedPreferences,
  ): DataStore<Int> {
    return factory.int(
      fileName = "autoRewind",
      defaultValue = 2,
      migrations = listOf(intPrefsDataMigration(sharedPreferences, "AUTO_REWIND")),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @FadeOutStore
  private fun fadeOut(factory: StarBookDataStoreFactory): DataStore<Duration> {
    return factory.create(
      fileName = "fadeOut",
      defaultValue = 10.seconds,
      serializer = Duration.serializer(),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @SeekTimeStore
  private fun seekTime(
    factory: StarBookDataStoreFactory,
    sharedPreferences: SharedPreferences,
  ): DataStore<Int> {
    return factory.int(
      fileName = "seekTime",
      defaultValue = 20,
      migrations = listOf(intPrefsDataMigration(sharedPreferences, "SEEK_TIME")),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @SleepTimerPreferenceStore
  private fun sleepTimerPreference(factory: StarBookDataStoreFactory): DataStore<SleepTimerPreference> {
    return factory.create(
      serializer = SleepTimerPreference.Companion.serializer(),
      fileName = "sleepTime3",
      defaultValue = SleepTimerPreference.Default,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @GridModeStore
  private fun gridMode(
    factory: StarBookDataStoreFactory,
    sharedPreferences: SharedPreferences,
  ): DataStore<GridMode> {
    return factory.create(
      GridMode.serializer(),
      GridMode.FOLLOW_DEVICE,
      "gridMode",
      migrations = listOf(
        PrefsDataMigration(
          sharedPreferences,
          key = "gridView",
          getFromSharedPreferences = {
            when (sharedPreferences.getString("gridView", null)) {
              "LIST" -> GridMode.LIST
              "GRID" -> GridMode.GRID
              else -> GridMode.FOLLOW_DEVICE
            }
          },
        ),
      ),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @OnboardingCompletedStore
  private fun onboardingCompleted(factory: StarBookDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("onboardingCompleted", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @CurrentBookStore
  private fun currentBook(factory: StarBookDataStoreFactory): DataStore<BookId?> {
    return factory.create(
      serializer = BookId.serializer().nullable,
      fileName = "currentBook",
      defaultValue = null,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @AmountOfBatteryOptimizationRequestedStore
  private fun amountOfBatteryOptimizationsRequestedStore(factory: StarBookDataStoreFactory): DataStore<Int> {
    return factory.int("amountOfBatteryOptimizationsRequestedStore", 0)
  }

  @Provides
  @SingleIn(AppScope::class)
  @ReviewDialogShownStore
  private fun reviewDialogShown(factory: StarBookDataStoreFactory): DataStore<Boolean> {
    return factory.create(Boolean.serializer(), false, "reviewDialogShown")
  }

  @Provides
  @SingleIn(AppScope::class)
  @FolderPickerMovedDialogShownStore
  private fun folderPickerMovedDialogShown(factory: StarBookDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("folderPickerMovedDialogShow2n", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @AnalyticsConsentStore
  private fun analyticsConsent(factory: StarBookDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("analyticsConsent", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @DeveloperMenuUnlockedStore
  private fun developerMenuUnlocked(factory: StarBookDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("developerMenuUnlocked", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @FeatureFlagOverridesStore
  private fun featureFlagOverrides(factory: StarBookDataStoreFactory): DataStore<Map<String, FeatureFlagOverride>> {
    return factory.create(
      serializer = MapSerializer(String.serializer(), FeatureFlagOverride.serializer()),
      defaultValue = emptyMap(),
      fileName = "featureFlagOverrides",
    )
  }
}

private class LegacyDarkThemeMigration(
  application: Application,
  private val sharedPreferences: SharedPreferences,
) : DataMigration<ThemeMode> {

  private val oldDataStoreFile = File(application.applicationContext.filesDir, "datastore/darkTheme")

  override suspend fun cleanUp() {
    oldDataStoreFile.delete()
    sharedPreferences.edit {
      remove("darkTheme")
    }
  }

  override suspend fun migrate(currentData: ThemeMode): ThemeMode {
    val legacyValue = when {
      oldDataStoreFile.exists() -> oldDataStoreFile.readText().trim().toBooleanStrictOrNull()
      sharedPreferences.contains("darkTheme") -> sharedPreferences.getBoolean("darkTheme", false)
      else -> null
    }
    return when (legacyValue) {
      true -> ThemeMode.Dark
      false -> ThemeMode.Light
      null -> currentData
    }
  }

  override suspend fun shouldMigrate(currentData: ThemeMode): Boolean {
    return oldDataStoreFile.exists() || sharedPreferences.contains("darkTheme")
  }
}


