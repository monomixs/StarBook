package com.starbook.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import com.starbook.core.common.serialization.UriSerializer
import com.starbook.core.data.BookId

sealed interface Destination {

  enum class Tab {
    HOME, SEARCH, LIBRARY
  }

  @Serializable
  data class Playback(val bookId: BookId) : Compose {
    override val trackingName: String get() = "Playback"
  }

  @Serializable
  data class Bookmarks(val bookId: BookId) : Compose {
    override val trackingName: String get() = "Bookmarks"
  }

  @Serializable
  data class CoverFromInternet(val bookId: BookId) : Compose {
    override val trackingName: String get() = "CoverFromInternet"
  }

  data class Website(val url: String) : Destination

  @Serializable
  data class EditCover(
    val bookId: BookId,
    val cover:
    @Serializable(with = UriSerializer::class)
    Uri,
  ) : Compose {
    override val trackingName: String get() = "EditCover"
  }

  data class Activity(val intent: Intent) : Destination

  @Serializable
  sealed interface Compose :
    Destination,
    NavKey {
    val trackingName: String
  }

  @Serializable
  data object Settings : Compose {
    override val trackingName: String get() = "Settings"
  }

  @Serializable
  data object SupportStarBook : Compose {
    override val trackingName: String get() = "SupportStarBook"
  }

  @Serializable
  data object DeveloperSettings : Compose {
    override val trackingName: String get() = "DeveloperSettings"
  }

  @Serializable
  data object BookOverview : Compose {
    override val trackingName: String get() = "BookOverview"
  }

  @Serializable
  data object Home : Compose {
    override val trackingName: String get() = "Home"
  }

  @Serializable
  data object Search : Compose {
    override val trackingName: String get() = "Search"
  }

  @Serializable
  data object FolderPicker : Compose {
    override val trackingName: String get() = "FolderPicker"
  }

  @Serializable
  data class SelectFolderType(
    val uri:
    @Serializable(with = UriSerializer::class)
    Uri,
    val origin: Origin,
  ) : Compose {
    override val trackingName: String = "SelectFolderType"
  }

  @Serializable
  data object OnboardingWelcome : Compose {
    override val trackingName: String get() = "OnboardingWelcome"
  }

  @Serializable
  data object OnboardingCompletion : Compose {
    override val trackingName: String get() = "OnboardingCompletion"
  }

  @Serializable
  data object OnboardingExplanation : Compose {
    override val trackingName: String get() = "OnboardingExplanation"
  }

  @Serializable
  data object Credits : Compose {
    override val trackingName: String get() = "Credits"
  }

  @Serializable
  data class MetadataEditor(val bookId: BookId) : Compose {
    override val trackingName: String get() = "MetadataEditor"
  }

  data object BatteryOptimization : Destination

  @Serializable
  data class AddContent(val origin: Origin) : Compose {
    override val trackingName: String = "AddContent"
  }
}

