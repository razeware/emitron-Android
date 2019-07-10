package com.raywenderlich.emitron.di.bindings

import com.raywenderlich.emitron.ui.download.DownloadFragment
import com.raywenderlich.emitron.ui.library.LibraryFragment
import com.raywenderlich.emitron.ui.mytutorial.MyTutorialFragmentHost
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindings {

  @ContributesAndroidInjector
  abstract fun contributeLibraryFragment(): LibraryFragment

  @ContributesAndroidInjector
  abstract fun contributeDownloadFragment(): DownloadFragment

  @ContributesAndroidInjector(modules = [MyTutorialFragmentBindings::class])
  abstract fun contributeMyTutorialFragment(): MyTutorialFragmentHost
}
