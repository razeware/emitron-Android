package com.raywenderlich.emitron

import android.os.Bundle
import android.view.View
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : DaggerAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val navController = findNavController(R.id.nav_host_fragment)
    nav_view.setupWithNavController(navController)

    navController.addOnDestinationChangedListener { _, destination, _ ->
      onNavDestinationChanged(destination)
    }
  }

  private fun onNavDestinationChanged(destination: NavDestination) {
    nav_view.visibility = View.VISIBLE

    when (destination.id) {
      R.id.navigation_settings,
      R.id.navigation_filter,
      R.id.navigation_collection,
      R.id.navigation_login -> {
        nav_view.visibility = View.GONE
      }
      R.id.navigation_player -> {
        nav_view.visibility = View.GONE
      }
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(AppBarConfiguration(navController.graph)) ||
        super.onSupportNavigateUp()
  }

}
