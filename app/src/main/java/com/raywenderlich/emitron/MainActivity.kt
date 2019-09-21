package com.raywenderlich.emitron

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.databinding.ActivityMainBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.utils.extensions.*
import dagger.android.support.DaggerAppCompatActivity
import io.fabric.sdk.android.Fabric
import javax.inject.Inject

/**
 * Parent screen from all fragments
 */
class MainActivity : DaggerAppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: MainViewModel by viewModels { viewModelFactory }
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

    AppCompatDelegate.setDefaultNightMode(viewModel.getNightModeSettings())

    // Collect crash reports only if user has allowed
    if (viewModel.isCrashReportingAllowed()) {
      Fabric.with(this, Crashlytics())
    }

    requestGestureUi()
    createNotificationChannels()
  private fun createNotificationChannels() {
    if (hasNotificationChannelSupport()) {
      NotificationChannels.newInstance(application).createNotificationChannels()
    }
  }

  private fun onNavDestinationChanged(destination: NavDestination) {
    // bottom nav is visible on all screen
    binding.navView.visibility = View.VISIBLE
    binding.navDivider.visibility = View.VISIBLE

    when (destination.id) {
      R.id.navigation_settings,
      R.id.navigation_settings_bottom_sheet,
      R.id.navigation_filter,
      R.id.navigation_collection,
      R.id.navigation_login -> {
        // Hide bottom nav on login screen
        binding.navDivider.visibility = View.GONE
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
