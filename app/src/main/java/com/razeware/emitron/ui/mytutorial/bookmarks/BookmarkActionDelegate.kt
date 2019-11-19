package com.razeware.emitron.ui.mytutorial.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.razeware.emitron.data.bookmarks.BookmarkRepository
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.BoundaryCallbackNotifier
import com.razeware.emitron.utils.Event
import com.razeware.emitron.utils.decrement
import com.razeware.emitron.utils.increment
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Delegate class to add/remove a bookmark for content
 */
class BookmarkActionDelegate @Inject constructor(
  private val bookmarkRepository: BookmarkRepository
) {

  /**
   * Bookmark action API result
   */
  enum class BookmarkActionResult {
    /**
     * Create Bookmark request succeeded
     */
    BookmarkCreated,
    /**
     * Create Bookmark request failed
     */
    BookmarkFailedToCreate,
    /**
     * Delete Bookmark request succeeded
     */
    BookmarkDeleted,
    /**
     * Delete Bookmark request failed
     */
    BookmarkFailedToDelete
  }

  private val _bookmarkActionResult =
    MutableLiveData<Event<BookmarkActionResult>>()
  /**
   * Observer for bookmark action
   *
   */
  val bookmarkActionResult: LiveData<Event<BookmarkActionResult>>
    get() {
      return _bookmarkActionResult
    }

  /**
   * Bookmark/Un-bookmark the collection
   */
  suspend fun updateContentBookmark(
    data: Data?,
    boundaryCallbackNotifier: BoundaryCallbackNotifier? = null
  ): Data? {
    val collection = data ?: return data
    val isBookmarked = collection.isBookmarked()

    val onFailure = {
      val event = if (isBookmarked) {
        Event(BookmarkActionResult.BookmarkFailedToDelete)
      } else {
        Event(BookmarkActionResult.BookmarkFailedToCreate)
      }
      _bookmarkActionResult.value = event
    }

    val contentId = collection.id
    val bookmarkId = collection.getBookmarkId()

    if (contentId.isNullOrBlank()) {
      onFailure()
      return collection
    }

    if (isBookmarked && bookmarkId.isNullOrBlank()) {
      onFailure()
      return collection
    }

    boundaryCallbackNotifier.increment()
    return if (isBookmarked) {
      val result = removeContentBookmark(contentId, bookmarkId!!)
      boundaryCallbackNotifier.decrement()
      if (result) {
        collection.removeBookmark()
      } else {
        collection
      }
    } else {
      val result = addContentBookmark(contentId)
      boundaryCallbackNotifier.decrement()
      if (null != result) {
        collection.addBookmark(result)
      } else {
        collection
      }
    }
  }

  private suspend fun addContentBookmark(
    contentId: String
  ): Content? {

    val (bookmark, result) = try {
      bookmarkRepository.createBookmark(contentId)
    } catch (exception: IOException) {
      null to false
    } catch (exception: HttpException) {
      null to false
    }

    _bookmarkActionResult.value = if (result) {
      bookmarkRepository.updateBookmarkInDb(contentId, bookmark?.getChildId())
      Event(BookmarkActionResult.BookmarkCreated)
    } else {
      bookmarkRepository.updateBookmarkInDb(contentId, null)
      Event(BookmarkActionResult.BookmarkFailedToCreate)
    }
    return bookmark
  }

  private suspend fun removeContentBookmark(
    contentId: String,
    bookmarkId: String
  ): Boolean {

    val result = try {
      bookmarkRepository.deleteBookmark(contentId, bookmarkId)
    } catch (exception: IOException) {
      false
    } catch (exception: HttpException) {
      false
    }

    _bookmarkActionResult.value = if (result) {
      bookmarkRepository.updateBookmarkInDb(contentId, null)
      Event(BookmarkActionResult.BookmarkDeleted)
    } else {
      bookmarkRepository.updateBookmarkInDb(contentId, bookmarkId)
      Event(BookmarkActionResult.BookmarkFailedToDelete)
    }
    return result
  }
}
