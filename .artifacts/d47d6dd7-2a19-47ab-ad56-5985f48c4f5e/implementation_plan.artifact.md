# Implementation Plan - Dedicated Search Logic

Completely decouple the search screen's logic from the library's view model by creating a dedicated `BookSearchViewModel`. This ensures the search screen consistently displays audiobooks and "Explore" shelf data without conflicting with the library's state.

## User Review Required

> [!IMPORTANT]
> **Architectural Change:** I am moving the search state and logic out of `BookOverviewViewModel` into a new `BookSearchViewModel`. This follows the project's established pattern (like `StatsViewModel`) and will solve the "empty screen" issue by providing a dedicated data pipeline for the search feature.

## Proposed Changes

### Feature Architecture

#### [NEW] [BookSearchViewModel.kt](file:///C:/Users/wedle/StudioProjects/StarBook/features/bookOverview/src/main/kotlin/com/starbook/features/bookOverview/search/BookSearchViewModel.kt)
- Manage independent `query` and `viewState`.
- Provide `state()` Composable function to reactively build `BookSearchViewState`.
- Implement `onSearchQueryChange` and `onBookClick` handlers.
- Load books, recent searches, and author suggestions specifically for the search feature.

#### [MODIFY] [BookOverviewGraph.kt](file:///C:/Users/wedle/StudioProjects/StarBook/features/bookOverview/src/main/kotlin/com/starbook/features/bookOverview/di/BookOverviewGraph.kt)
- Add `val bookSearchViewModel: BookSearchViewModel` to the graph.

### UI Integration

#### [MODIFY] [BookOverview.kt](file:///C:/Users/wedle/StudioProjects/StarBook/features/bookOverview/src/main/kotlin/com/starbook/features/bookOverview/views/BookOverview.kt)
- Update `BookOverviewScreen` for `Destination.Tab.SEARCH` to use `bookGraph.bookSearchViewModel`.
- Ensure query and state are passed correctly to `BookSearchScreen`.

#### [MODIFY] [BookSearchScreen.kt](file:///C:/Users/wedle/StudioProjects/StarBook/features/bookOverview/src/main/kotlin/com/starbook/features/bookOverview/search/BookSearchScreen.kt)
- Ensure the screen uses the provided `viewState` and `query` from the new ViewModel.

### Cleanup

#### [MODIFY] [BookOverviewViewModel.kt](file:///C:/Users/wedle/StudioProjects/StarBook/features/bookOverview/src/main/kotlin/com/starbook/features/bookOverview/overview/BookOverviewViewModel.kt)
- Remove search-related fields and logic that now reside in `BookSearchViewModel`.
- Simplify `state()` and remove tab-based logic for search visibility.

## Verification Plan

### Automated Tests
- Build the project to verify successful DI code generation.

### Manual Verification
- Deploy to device.
- Navigate to the **Search** tab.
- Verify "Pick up where you left off" and "Explore the shelf" show books.
- Verify search input triggers results and highlighting.
- Verify category chips (Finished, Continue, etc.) work correctly.
- Verify switching to the **Library** tab and back maintains the search state if intended, or resets cleanly.
