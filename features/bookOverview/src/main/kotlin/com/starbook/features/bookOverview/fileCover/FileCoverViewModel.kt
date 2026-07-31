package com.starbook.features.bookOverview.fileCover

import android.net.Uri
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import com.starbook.core.data.BookId
import com.starbook.features.bookOverview.di.BookOverviewScope
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator

@SingleIn(BookOverviewScope::class)
@Inject
class FileCoverViewModel(private val navigator: Navigator) {

  fun onImagePicked(uri: Uri, bookId: BookId) {
    navigator.goTo(Destination.EditCover(bookId, uri))
  }
}

