package com.starbook.features.bookOverview.internetCover

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import com.starbook.core.data.BookId
import com.starbook.features.bookOverview.di.BookOverviewScope
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator

@SingleIn(BookOverviewScope::class)
@Inject
class InternetCoverViewModel(private val navigator: Navigator) {

  fun onInternetCover(bookId: BookId) {
    navigator.goTo(Destination.CoverFromInternet(bookId))
  }
}

