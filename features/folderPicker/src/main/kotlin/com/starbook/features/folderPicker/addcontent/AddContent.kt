package com.starbook.features.folderPicker.addcontent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.starbook.core.common.rootGraphAs
import com.starbook.core.ui.StarBookTheme
import com.starbook.navigation.Destination
import com.starbook.navigation.NavEntryProvider
import com.starbook.navigation.Origin

@ContributesTo(AppScope::class)
interface AddContentGraph {
  val viewModelFactory: AddContentViewModel.Factory
}

@ContributesTo(AppScope::class)
interface AddContentProvider {

  @Provides
  @IntoSet
  fun addContentNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.AddContent> { key ->
    NavEntry(key) {
      AddContent(origin = key.origin)
    }
  }
}

@Composable
fun AddContent(origin: Origin) {
  val viewModel = retain(origin.name) {
    rootGraphAs<AddContentGraph>().viewModelFactory.create(origin)
  }
  SelectFolder(
    onBack = {
      viewModel.back()
    },
    origin = origin,
    onAdd = { folderType, uri ->
      viewModel.add(uri, folderType)
    },
  )
}

@Composable
@Preview
private fun AddContentPreview() {
  StarBookTheme {
    AddContent(Origin.Default)
  }
}

