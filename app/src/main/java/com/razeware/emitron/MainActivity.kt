package com.razeware.emitron

import android.annotation.TargetApi
import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.crashlytics.android.Crashlytics
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.razeware.emitron.databinding.ActivityMainBinding
import com.razeware.emitron.di.modules.viewmodel.ViewModelFactory
import com.razeware.emitron.notifications.NotificationChannels
import com.razeware.emitron.ui.player.PipActionDelegate
import com.razeware.emitron.utils.extensions.*
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

    initObservers()
    CastContext.getSharedInstance(this)
  }

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
      R.id.navigation_login,
      R.id.navigation_onboarding,
      R.id.navigation_player -> {
        // Hide bottom nav on login screen
        binding.navDivider.visibility = View.GONE
        binding.navView.visibility = View.GONE
      }
    }
  }

  private fun initObservers() {
    viewModel.isPlaying.observe(this) {
      if (hasPipSupport()) {
        setPictureInPictureParams(updatePipParameters())
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

  /**
   * See [AppCompatActivity.onUserLeaveHint]
   */
  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    if (hasPipSupport() && viewModel.isPlaying()) {
      enterPictureInPictureMode(updatePipParameters())
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  private fun updatePipParameters() = PictureInPictureParams.Builder()
    .setAspectRatio(PipActionDelegate.getPipRatio(this))
    .setActions(PipActionDelegate.getPipActions(this, viewModel.isPlaying()))
    .build()

  /**
   * See [AppCompatActivity.onCreateOptionsMenu]
   */
  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_cast, menu)
    CastButtonFactory.setUpMediaRouteButton(
      this,
      menu,
      R.id.media_route_menu_item
    )
    return true
  }
}
