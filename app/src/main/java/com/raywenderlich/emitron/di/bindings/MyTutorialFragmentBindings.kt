package com.raywenderlich.emitron.di.bindings

import com.raywenderlich.emitron.ui.mytutorial.MyTutorialBaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MyTutorialFragmentBindings {

  @ContributesAndroidInjector
  abstract fun contributeMyTutorialBaseFragment(): MyTutorialBaseFragment
}
