package com.raywenderlich.emitron.di.modules.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject
constructor(private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) :
  ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    var creator: Provider<out ViewModel>? = creators[modelClass]
    if (creator == null) {
      for ((key, value) in creators) {
        if (modelClass.isAssignableFrom(key)) {
          creator = value
          break
        }
      }
    }
    requireNotNull(creator) { "unknown model class $modelClass" }
    try {
      return creator.get() as T
    } catch (ignored: RuntimeException) {
      throw IllegalArgumentException("unknown model class $modelClass")
    }

  }
}
