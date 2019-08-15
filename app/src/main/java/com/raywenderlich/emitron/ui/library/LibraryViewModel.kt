package com.raywenderlich.emitron.ui.library

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
  private val repository: ContentRepository,
  val contentPagedViewModel: ContentPagedViewModel
) : ViewModel() {

  fun loadCollections(filters: List<Data> = emptyList()) {
    val listing = repository.getContents(filters = filters)
    contentPagedViewModel.repoResult.postValue(listing)
  }
}
