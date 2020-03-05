package com.razeware.emitron.ui.common

import androidx.navigation.ui.AppBarConfiguration
import com.razeware.emitron.R

/**
 * Returns [AppBarConfiguration] for app
 */
fun getDefaultAppBarConfiguration(): AppBarConfiguration {

  /**
   * Add top level destinations to the following set
   * Top level destinations won't have a back button.
   */
  val topLevelDestinations = setOf(
    R.id.navigation_library,
    R.id.navigation_downloads,
    R.id.navigation_my_tutorials,
    R.id.navigation_login
  )
  return AppBarConfiguration(topLevelDestinations)
}
