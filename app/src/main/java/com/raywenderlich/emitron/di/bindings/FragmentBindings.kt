package com.raywenderlich.emitron.di.bindings

import com.raywenderlich.emitron.ui.collection.CollectionFragment
import com.raywenderlich.emitron.ui.download.DownloadFragment
import com.raywenderlich.emitron.ui.filter.FilterFragment
import com.raywenderlich.emitron.ui.library.LibraryFragment
import com.raywenderlich.emitron.ui.login.LoginFragment
import com.raywenderlich.emitron.ui.mytutorial.MyTutorialHostFragment
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkFragment
import com.raywenderlich.emitron.ui.mytutorial.progression.ProgressionFragment
import com.raywenderlich.emitron.ui.onboarding.OnboardingFragment
import com.raywenderlich.emitron.ui.player.PlayerFragment
import com.raywenderlich.emitron.ui.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindings {

  @ContributesAndroidInjector
  abstract fun contributeLibraryFragment(): LibraryFragment

  @ContributesAndroidInjector
  abstract fun contributeDownloadFragment(): DownloadFragment

  @ContributesAndroidInjector
  abstract fun contributeBookmarkFragment(): BookmarkFragment

  @ContributesAndroidInjector
  abstract fun contributeFilterFragment(): FilterFragment

  @ContributesAndroidInjector
  abstract fun contributeLoginFragment(): LoginFragment

  @ContributesAndroidInjector
  abstract fun contributeSettingsFragment(): SettingsFragment

  @ContributesAndroidInjector
  abstract fun contributeCollectionFragment(): CollectionFragment

  @ContributesAndroidInjector
  abstract fun contributePlayerFragment(): PlayerFragment

  @ContributesAndroidInjector
  abstract fun contributeOnboardingFragment(): OnboardingFragment

  @ContributesAndroidInjector
  abstract fun contributeMyTutorialHostFragment(): MyTutorialHostFragment

  @ContributesAndroidInjector
  abstract fun contributeProgressionFragment(): ProgressionFragment
}
