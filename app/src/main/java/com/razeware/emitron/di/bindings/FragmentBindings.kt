package com.razeware.emitron.di.bindings

import com.razeware.emitron.ui.collection.CollectionFragment
import com.razeware.emitron.ui.download.DownloadFragment
import com.razeware.emitron.ui.filter.FilterFragment
import com.razeware.emitron.ui.library.LibraryFragment
import com.razeware.emitron.ui.login.LoginFragment
import com.razeware.emitron.ui.mytutorial.MyTutorialFragment
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkFragment
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionFragment
import com.razeware.emitron.ui.onboarding.OnboardingFragment
import com.razeware.emitron.ui.player.PlayerFragment
import com.razeware.emitron.ui.settings.SettingsBottomSheetDialogFragment
import com.razeware.emitron.ui.settings.SettingsFragment
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
  abstract fun contributeMyTutorialHostFragment(): MyTutorialFragment

  @ContributesAndroidInjector
  abstract fun contributeProgressionFragment(): ProgressionFragment

  @ContributesAndroidInjector
  abstract fun contributeSettingsBottomSheetDialogFragment(): SettingsBottomSheetDialogFragment
}
