package com.raywenderlich.emitron.ui.collection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.bookmarks.BookmarkRepository
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.UiStateViewModel
import com.raywenderlich.emitron.utils.Event
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.UiStateManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Collection (Content detail) detail viewmodel
 *
 * ViewModel for content detail view
 */
class CollectionViewModel @Inject constructor(
  private val repository: ContentRepository,
  private val bookmarkRepository: BookmarkRepository,
  private val progressionRepository: ProgressionRepository
) : ViewModel(), UiStateViewModel {

  private val _networkState = MutableLiveData<NetworkState>()

  override val uiState: MutableLiveData<UiStateManager.UiState> = MutableLiveData()

  override val networkState: LiveData<NetworkState>
    get() {
      return _networkState
    }

  private val _collection = MutableLiveData<Data>()

  private val _collectionEpisodes = MutableLiveData<List<EpisodeItem>>()

  private val _collectionContentType = MutableLiveData<ContentType>()

  private val _bookmarkActionResult = MutableLiveData<Event<BookmarkActionResult>>()

  private val _loadCollectionResult = MutableLiveData<Event<Boolean>>()

  private val _completionActionResult =
    MutableLiveData<Pair<Event<EpisodeProgressionActionResult>, Int>>()

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

  /**
   * Progression action API result
   */
  enum class EpisodeProgressionActionResult {
    /**
     * Episode progression complete request succeeded
     */
    EpisodeMarkedCompleted,
    /**
     * Episode progression complete request succeeded
     */
    EpisodeMarkedInProgress,
    /**
     * Episode progression in progress request succeeded
     */
    EpisodeFailedToMarkComplete,
    /**
     * Episode progression in progress request succeeded
     */
    EpisodeFailedToMarkInProgress,
  }

  /**
   * Observer for collection details
   */
  val collection: LiveData<Data>
    get() {
      return _collection
    }

  /**
   * Observer for episode for collection
   */
  val collectionEpisodes: LiveData<List<EpisodeItem>>
    get() {
      return _collectionEpisodes
    }

  /**
   * Observer for content type for collection
   *
   * You need to hide the list/headings when the collection type is [ContentType.Screencast]
   */
  val collectionContentType: LiveData<ContentType>
    get() {
      return _collectionContentType
    }

  /**
   * Observer for bookmark action
   *
   */
  val bookmarkActionResult: LiveData<Event<BookmarkActionResult>>
    get() {
      return _bookmarkActionResult
    }

  /**
   * Observer for progression action
   *
   */
  val completionActionResult: LiveData<Pair<Event<EpisodeProgressionActionResult>, Int>>
    get() {
      return _completionActionResult
    }

  /**
   * Observer for progression action
   *
   */
  val loadCollectionResult: LiveData<Event<Boolean>>
    get() {
      return _loadCollectionResult
    }

  /**
   * Get collection episodes
   */
  fun loadCollection(collection: Data) {
    uiState.value = UiStateManager.UiState.LOADING
    _collection.value = collection
    _collectionContentType.value = collection.getContentType()

    val collectionId = collection.id
    if (collectionId.isNullOrBlank()) {
      uiState.value = UiStateManager.UiState.ERROR
      return
    }

    val onFailure = {
      _loadCollectionResult.value = Event(false)
    }

    viewModelScope.launch {
      val fetchedCollection = try {
        repository.getContent(collectionId)
      } catch (exception: IOException) {
        onFailure()
        null
      } catch (exception: HttpException) {
        onFailure()
        null
      }

      _collection.value = fetchedCollection?.datum

      // If content is not screencast, set the episodes
      if (fetchedCollection?.isTypeScreencast() == false) {

        fetchedCollection.apply {
          val fetchedCollectionEpisodes = getGroups().flatMap {
            val groupedDataIds = it.getGroupedDataIds()
            val data =
              included?.filter { (id) -> id in groupedDataIds }
                ?.map { data -> data.setIncluded(included) }
            EpisodeItem.buildFrom(it.copy(relationships = it.relationships?.setContents(data)))
          }

          _collectionEpisodes.postValue(fetchedCollectionEpisodes)
        }
      }
      uiState.value = UiStateManager.UiState.LOADED
    }
  }

  /**
   * Create playlist to be forwarded to video player
   */
  fun getPlaylist(): Contents? {
    val collection = _collection.value

    return when (collection?.getContentType()) {
      ContentType.Collection -> {
        val playlist = collectionEpisodes.value?.let {
          it.mapNotNull { (_, data) -> data }
        }
        Contents(datum = playlist ?: emptyList())
      }
      ContentType.Screencast -> {
        Contents(datum = listOf(collection))
      }
      null -> {
        throw IllegalStateException("Invalid type for collection or screencast")
      }
    }
  }

  /**
   * Bookmark/Un-bookmark the collection
   */
  fun toggleBookmark() {
    val collection = _collection.value ?: return

    val onFailure = {
      val event = if (collection.isBookmarked()) {
        Event(BookmarkActionResult.BookmarkFailedToDelete)
      } else {
        Event(BookmarkActionResult.BookmarkFailedToCreate)
      }
      _bookmarkActionResult.value = event
    }

    viewModelScope.launch {
      try {
        if (collection.isBookmarked()) {
          deleteCollectionBookmark(collection.getBookmarkId())
        } else {
          bookmarkCollection(collection.id)
        }
      } catch (exception: IOException) {
        onFailure()
      } catch (exception: HttpException) {
        onFailure()
      }
    }
  }

  /**
   * Mark episode completed/in-progress
   */
  fun toggleEpisodeCompleted(episode: Data?, position: Int) {
    if (null == episode) {
      return
    }

    val onFailure = {
      val event = if (episode.isFinished()) {
        Event(EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress)
      } else {
        Event(EpisodeProgressionActionResult.EpisodeFailedToMarkComplete)
      }
      _completionActionResult.value = event to position
    }

    viewModelScope.launch {
      try {
        if (episode.isFinished()) {
          markEpisodeInProgress(episode.id, position)
        } else {
          markEpisodeCompleted(episode.id, position)
        }
      } catch (exception: IOException) {
        onFailure()
      } catch (exception: HttpException) {
        onFailure()
      }
    }
  }

  private suspend fun bookmarkCollection(collectionId: String?) {
    collectionId?.let {
      val (bookmark, result) = bookmarkRepository.createBookmark(collectionId)
      _bookmarkActionResult.value = if (result) {
        val collection = _collection.value
        _collection.value = collection?.addBookmark(bookmark)
        Event(BookmarkActionResult.BookmarkCreated)
      } else {
        Event(BookmarkActionResult.BookmarkFailedToCreate)
      }
    }
  }

  private suspend fun deleteCollectionBookmark(bookmarkId: String?) {
    bookmarkId?.let {
      val result = bookmarkRepository.deleteBookmark(bookmarkId)
      _bookmarkActionResult.value = if (result) {
        val collection = _collection.value
        _collection.value = collection?.removeBookmark()
        Event(BookmarkActionResult.BookmarkDeleted)
      } else {
        Event(BookmarkActionResult.BookmarkFailedToDelete)
      }
    }
  }

  private suspend fun markEpisodeCompleted(episodeId: String?, position: Int) {
    episodeId?.let {
      val (_, result) = progressionRepository.updateProgression(episodeId)
      _completionActionResult.value = if (result) {
        Event(EpisodeProgressionActionResult.EpisodeMarkedCompleted) to position
      } else {
        Event(EpisodeProgressionActionResult.EpisodeFailedToMarkComplete) to position
      }
    }
  }

  private suspend fun markEpisodeInProgress(episodeId: String?, position: Int) {
    episodeId?.let {
      val (_, result) = progressionRepository.updateProgression(episodeId)
      _completionActionResult.value = if (result) {
        Event(EpisodeProgressionActionResult.EpisodeMarkedInProgress) to position
      } else {
        Event(EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress) to position
      }
    }
  }
}
