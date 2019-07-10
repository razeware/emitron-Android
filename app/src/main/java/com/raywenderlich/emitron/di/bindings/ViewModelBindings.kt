package com.raywenderlich.emitron.di.bindings

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.MainViewModel
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelKey
import com.raywenderlich.emitron.ui.library.LibraryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelBindings {

  @Binds
  @IntoMap
  @ViewModelKey(LibraryViewModel::class)
  abstract fun bindLibraryViewModel(libraryViewModel: LibraryViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindMainViewModel(libraryViewModel: MainViewModel): ViewModel
}
