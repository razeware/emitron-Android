package com.raywenderlich.emitron

import android.os.Bundle
import android.view.View
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.raywenderlich.emitron.databinding.ActivityMainBinding
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import dagger.android.support.DaggerAppCompatActivity

/**
 * Parent screen from all fragments
 */
class MainActivity : DaggerAppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  /**
   * onCreate()
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = setDataBindingView(R.layout.activity_main)

    val navController = findNavController(R.id.nav_host_fragment)

    binding.navView.setupWithNavController(navController)

    navController.addOnDestinationChangedListener { _, destination, _ ->
      onNavDestinationChanged(destination)
    }
  }

  private fun onNavDestinationChanged(destination: NavDestination) {
    // bottom nav is visible on all screen
    binding.navView.visibility = View.VISIBLE

    when (destination.id) {
      R.id.navigation_settings,
      R.id.navigation_filter,
      R.id.navigation_collection,
      R.id.navigation_login -> {
        // Hide bottom nav on login screen
        binding.navView.visibility = View.GONE
      }
    }
  }

  /**
   * Handle up navigation with nav controller
   */
  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(AppBarConfiguration(navController.graph)) ||
        super.onSupportNavigateUp()
  }

}
