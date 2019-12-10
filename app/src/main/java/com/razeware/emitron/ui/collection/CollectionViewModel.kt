package com.razeware.emitron.ui.collection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.offline.Download
import com.razeware.emitron.data.content.ContentRepository
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.ui.common.UiStateViewModel
import com.razeware.emitron.ui.download.DownloadAction
import com.razeware.emitron.ui.download.DownloadActionDelegate
import com.razeware.emitron.ui.login.PermissionActionDelegate
import com.razeware.emitron.ui.login.PermissionsAction
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionAction
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.razeware.emitron.ui.onboarding.OnboardingAction
import com.razeware.emitron.ui.onboarding.OnboardingActionDelegate
import com.razeware.emitron.ui.player.Playlist
import com.razeware.emitron.utils.Event
import com.razeware.emitron.utils.NetworkState
import com.razeware.emitron.utils.UiStateManager
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for content detail view
 */
class CollectionViewModel @Inject constructor(
  private val repository: ContentRepository,
  private val bookmarkActionDelegate: BookmarkActionDelegate,
  private val progressionActionDelegate: ProgressionActionDelegate,
  private val downloadActionDelegate: DownloadActionDelegate,
  private val onboardingActionDelegate: OnboardingActionDelegate,
  private val permissionActionDelegate: PermissionActionDelegate
) : ViewModel(), UiStateViewModel, OnboardingAction by onboardingActionDelegate,
  DownloadAction by downloadActionDelegate, PermissionsAction by permissionActionDelegate,
  ProgressionAction by progressionActionDelegate {

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
   * LiveData for collection details
   */
  val collection: LiveData<Data>
    get() {
      return _collection
    }

  /**
   * LiveData for episode for collection
   */
  val collectionEpisodes: LiveData<List<EpisodeItem>>
    get() {
      return _collectionEpisodes
    }

  /**
   * LiveData for content type for collection
   *
   * You need to hide the list/headings when the collection type is [ContentType.Screencast]
   */
  val collectionContentType: LiveData<ContentType>
    get() {
      return _collectionContentType
    }

  /**
   * LiveData for bookmark action
   *
   */
  val bookmarkActionResult: LiveData<Event<BookmarkActionDelegate.BookmarkActionResult>> =
    bookmarkActionDelegate.bookmarkActionResult

  private val _downloads = MutableLiveData<List<Download>>()

  /**
   * LiveData for [com.razeware.emitron.model.entity.Download] table
   */
  val downloads: LiveData<List<Download>>
    get() = _downloads

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
  fun loadCollection(content: Data) {
    uiState.value = UiStateManager.UiState.LOADING
    _collection.value = content
    _collectionContentType.value = content.getContentType()

    val contentId = content.id
    if (contentId.isNullOrBlank()) {
      uiState.value = UiStateManager.UiState.ERROR
      return
    }

    viewModelScope.launch {
      if (!loadContentFromDb(contentId)) {
        loadContentFromApi(contentId)
      }

      uiState.value = UiStateManager.UiState.LOADED
    }
  }

  private suspend fun loadContentFromDb(contentId: String): Boolean {
    val onFailure = {
      _loadCollectionResult.value = Event(false)
    }

    val content = try {
      repository.getContentFromDb(contentId)
    } catch (exception: IOException) {
      onFailure()
      null
    } catch (exception: HttpException) {
      onFailure()
      null
    }

    content ?: return false

    return if (content.isCached()) {
      updateContentEpisodes(content)
      _loadCollectionResult.value = Event(true)
      true
    } else {
      false
    }
  }

  private suspend fun loadContentFromApi(contentId: String): Boolean {
    val onFailure = {
      _loadCollectionResult.value = Event(false)
    }

    val content = try {
      repository.getContent(contentId)
    } catch (exception: IOException) {
      onFailure()
      null
    } catch (exception: HttpException) {
      onFailure()
      null
    }

    content ?: return false

    updateContentEpisodes(content)
    _loadCollectionResult.value = Event(true)

    return true
  }

  private fun updateContentEpisodes(content: Content) {
    content.apply {
      _collection.value = content.datum?.updateRelationships(content.included)

      // If content is not screencast, set the episodes
      if (!content.isTypeScreencast()) {

        content.apply {
          val fetchedCollectionEpisodes = getIncludedGroups().flatMap {
            val childContentIds = it.getChildContentIds()
            val data =
              included?.filter { (id) -> id in childContentIds }
                ?.map { data -> data.updateRelationships(included) }
            EpisodeItem.buildFrom(it.copy(relationships = it.relationships?.setContents(data)))
          }

          _collectionEpisodes.value = fetchedCollectionEpisodes
        }
      }
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
      else -> {
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
  fun updateContentProgression(
    hasConnection: Boolean,
    episode: Data?,
    position: Int = 0,
    updatedAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    viewModelScope.launch {
      progressionActionDelegate.updateContentProgression(
        hasConnection,
        episode,
        position,
        updatedAt = updatedAt
      )
    }
  }

  /**
   * Is content playback allowed
   *
   * @param isConnected Is device connected to internet
   */
  fun isContentPlaybackAllowed(
    isConnected: Boolean,
    checkDownloadPermission: Boolean = true
  ): Boolean {
    val collection = _collection.value
    val isProfessionalContent = collection?.isProfessional()
    val isDownloaded = collection?.isDownloaded()

    return when {
      isConnected && isProfessionalContent == true -> {
        if (checkDownloadPermission && isDownloaded == true) {
          permissionActionDelegate.isDownloadAllowed()
        } else {
          permissionActionDelegate.isProfessionalVideoPlaybackAllowed()
        }
      }
      !isConnected -> permissionActionDelegate.isDownloadAllowed()
      else -> {
        if (checkDownloadPermission && isDownloaded == true) {
          permissionActionDelegate.isDownloadAllowed()
        } else {
          true
        }
      }
    }
  }

  /**
   * Get collection id
   *
   * @return id for video course, screencast
   */
  fun getContentId(): String? = _collection.value?.id

  /**
   * Check collection type
   *
   * @return true if collection type is screencast, else false
   */
  private fun isScreencast(): Boolean = _collection.value?.isTypeScreencast() ?: false

  /**
   * Get content ids
   *
   * @return list of content id if collection type is screencast, else list of episode ids
   */
  fun getContentIds(): List<String> {
    val contentId = getContentId()
    contentId ?: return emptyList()

    return if (isScreencast()) {
      listOf(contentId)
    } else {
      val episodes = collectionEpisodes.value
      val episodeIds = episodes?.mapNotNull { it.data?.id }
      episodeIds ?: emptyList()
    }
  }

  /**
   * Update download progress
   *
   * @param downloadProgress Download progress
   */
  fun updateDownload(downloadProgress: DownloadProgress) {
    viewModelScope.launch {
      downloadActionDelegate.updateDownloadProgress(downloadProgress)
    }
  }

  /**
   * Update collection download state
   *
   * @param downloads list of [com.razeware.emitron.model.entity.Download] from db
   *
   * @return Download state for collection
   */
  fun updateCollectionDownloadState(
    downloads: List<com.razeware.emitron.model.entity.Download>,
    downloadIds: List<String>
  ): com.razeware.emitron.model.Download? {
    val collection = _collection.value
    val download = downloadActionDelegate
      .getCollectionDownloadState(collection, downloads, downloadIds)
    _collection.value = collection?.copy(download = download)
    return download
  }

  /**
   * Collection is downloaded?
   *
   * @return true if collection is downloaded, else false
   */
  fun isDownloaded(): Boolean = _collection.value?.isDownloaded() ?: false

  /**
   * Get permissions for the current logged in user
   */
  fun getPermissions() {
    viewModelScope.launch {
      permissionActionDelegate.fetchPermissions()
    }
  }

  /**
   * Remove download
   */
  fun removeDownload() {
    _collection.value = _collection.value?.removeDownload()
  }
}
