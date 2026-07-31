package com.starbook.features.bookOverview.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import com.starbook.features.bookOverview.bottomSheet.BottomSheetViewModel
import com.starbook.features.bookOverview.deleteBook.DeleteBookViewModel
import com.starbook.features.bookOverview.editTitle.EditBookTitleViewModel
import com.starbook.features.bookOverview.fileCover.FileCoverViewModel
import com.starbook.features.bookOverview.overview.BookOverviewViewModel
import com.starbook.features.bookOverview.overview.StatsViewModel

import com.starbook.features.bookOverview.metadata.MetadataEditorViewModel
import com.starbook.features.bookOverview.search.BookSearchViewModel

abstract class BookOverviewScope private constructor()

@GraphExtension(scope = BookOverviewScope::class)
interface BookOverviewGraph {
  val bookOverviewViewModel: BookOverviewViewModel
  val statsViewModel: StatsViewModel
  val bookSearchViewModel: BookSearchViewModel
  val editBookTitleViewModel: EditBookTitleViewModel
  val bottomSheetViewModel: BottomSheetViewModel
  val deleteBookViewModel: DeleteBookViewModel
  val fileCoverViewModel: FileCoverViewModel
  val metadataEditorViewModel: MetadataEditorViewModel

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  interface Factory {
    fun create(): BookOverviewGraph

    @ContributesTo(AppScope::class)
    interface Provider {
      val bookOverviewGraphProviderFactory: Factory
    }
  }
}

