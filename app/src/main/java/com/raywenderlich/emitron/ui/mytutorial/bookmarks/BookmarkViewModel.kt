package com.raywenderlich.emitron.ui.mytutorial.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.bookmarks.BookmarkRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import com.raywenderlich.emitron.utils.BoundaryCallbackNotifier
import com.raywenderlich.emitron.utils.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for bookmarks
 */
class BookmarkViewModel @Inject constructor(
  private val bookmarkRepository: BookmarkRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val bookmarkActionDelegate: BookmarkActionDelegate,
  private val boundaryCallbackNotifier: BoundaryCallbackNotifier
) : ViewModel() {


  /**
   * Observer for bookmark delete action
   *
   */
  val bookmarkDeleteActionResult: LiveData<Event<BookmarkActionDelegate.BookmarkActionResult>>
    get() {
      return bookmarkActionDelegate.bookmarkActionResult
    }

  /**
   * Load bookmarks from database
   */
  fun loadBookmarks() {
    val listing =
      bookmarkRepository.getBookmarks(
        boundaryCallbackNotifier = boundaryCallbackNotifier
      )
    contentPagedViewModel.localRepoResult.value = listing
  }

  /**
   * Delete a bookmark
   */
  fun updateContentBookmark(content: Data?) {
    viewModelScope.launch {
      bookmarkActionDelegate.updateContentBookmark(content, boundaryCallbackNotifier)
    }
  }

  /**
   * Get pagination helper
   */
  fun getPaginationViewModel(): ContentPagedViewModel = contentPagedViewModel
}
