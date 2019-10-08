package com.raywenderlich.emitron.ui.collection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.UiStateViewModel
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.raywenderlich.emitron.ui.onboarding.OnboardingView
import com.raywenderlich.emitron.ui.player.Playlist
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
  private val bookmarkActionDelegate: BookmarkActionDelegate,
  private val progressionActionDelegate: ProgressionActionDelegate,
  private val settingsRepository: SettingsRepository
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

  private val _loadCollectionResult = MutableLiveData<Event<Boolean>>()

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
  val bookmarkActionResult: LiveData<Event<BookmarkActionDelegate.BookmarkActionResult>> =
    bookmarkActionDelegate.bookmarkActionResult

  /**
   * Observer for progression action
   *
   */
  val completionActionResult:
      LiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>> =
    progressionActionDelegate.completionActionResult

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

      fetchedCollection?.apply {
        _collection.value = fetchedCollection.datum?.updateRelationships(fetchedCollection.included)

        // If content is not screencast, set the episodes
        if (!fetchedCollection.isTypeScreencast()) {

          fetchedCollection.apply {
            val fetchedCollectionEpisodes = getGroups().flatMap {
              val groupedDataIds = it.getGroupedDataIds()
              val data =
                included?.filter { (id) -> id in groupedDataIds }
                  ?.map { data -> data.updateRelationships(included) }
              EpisodeItem.buildFrom(it.copy(relationships = it.relationships?.setContents(data)))
            }

            _collectionEpisodes.value = fetchedCollectionEpisodes
          }
        }
      }
      uiState.value = UiStateManager.UiState.LOADED
    }
  }

  /**
   * Create playlist to be forwarded to video player
   */
  fun getPlaylist(): Playlist {
    val collection = _collection.value

    return when (collection?.getContentType()) {
      ContentType.Collection -> {
        val playlist = collectionEpisodes.value?.let {
          it.mapNotNull { (_, data) -> data }
        }
        Playlist(collection = collection, episodes = playlist ?: emptyList())
      }
      ContentType.Screencast -> {
        Playlist(collection = collection, episodes = listOf(collection))
      }
      null -> {
        throw IllegalStateException("Invalid type for collection or screencast")
      }
    }
  }

  /**
   * Add or remove collection from bookmarks
   */
  fun updateContentBookmark() {
    viewModelScope.launch {
      _collection.value = bookmarkActionDelegate.updateContentBookmark(collection.value)
    }
  }

  /**
   * Mark a collection episode completed or in-progress
   *
   * @param episode to be marked completed
   * @param position position of episode in a list
   */
  fun updateContentProgression(episode: Data?, position: Int = 0) {
    viewModelScope.launch {
      progressionActionDelegate.updateContentProgression(
        episode,
        position
      )
    }
  }

  /**
   * @return true if content is free to watch, else false
   */
  fun isFreeContent(): Boolean = _collection.value?.isFreeContent() ?: false

  /**
   * @return id for video course, screencast
   */
  fun isOnboardedForType(view: OnboardingView): Boolean =
    settingsRepository.getOnboardedViews().contains(view)

  /**
   * @return true if onboarding can be shown, else false
   */
  fun isOnboardingAllowed(): Boolean = settingsRepository.isOnboardingAllowed()
}
