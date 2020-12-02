package com.razeware.emitron

import android.annotation.TargetApi
import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowInsets
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.razeware.emitron.databinding.ActivityMainBinding
import com.razeware.emitron.notifications.NotificationChannels
import com.razeware.emitron.ui.download.workers.PendingDownloadWorker
import com.razeware.emitron.ui.player.PipActionDelegate
import com.razeware.emitron.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint


/**
 * Parent screen from all fragments
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private val viewModel: MainViewModel by viewModels()

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
    FirebaseCrashlytics
      .getInstance()
      .setCrashlyticsCollectionEnabled(viewModel.isCrashReportingAllowed())

    requestGestureUi()
    createNotificationChannels()

    initObservers()
    CastContext.getSharedInstance(this)
    initPendingDownloadsWorker()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      setupWindowInsets()
    }
  }

  /**
   * Because new devices often use display cutouts (A.K.A. Notches), to save up screen real estate
   * for front cameras, it's important to add extra padding to the top part of the screen, to avoid
   * notch overlapping with the UI.
   *
   * To do this, we read the Notch size, and reduce the number by the default status bar height,
   * because this is the margin size that's added to all the screens by default. This provides us
   * with a stable UI, that doesn't overlap any content with the notch.
   * */
  @TargetApi(Build.VERSION_CODES.P)
  private fun setupWindowInsets() {
    binding.container.doOnLayout {
      val inset = binding.container.rootWindowInsets

      val cutoutSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        inset?.getInsets(WindowInsets.Type.statusBars())?.top
      } else {
        inset?.displayCutout?.safeInsetTop
      }

      if (cutoutSize != null) {
        val defaultTopMargin = resources.getDimensionPixelSize(R.dimen.guideline_top_status_bar)

        binding.container.updatePadding(top = cutoutSize - defaultTopMargin)
      }
    }
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

  private fun initPendingDownloadsWorker() {
    PendingDownloadWorker.enqueue(
      WorkManager.getInstance(this),
      viewModel.downloadsWifiOnly()
    )
  }
}
