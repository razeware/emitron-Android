package com.razeware.emitron.ui.mytutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.tabs.TabLayoutMediator
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentMyTutorialsBinding
import com.razeware.emitron.utils.extensions.setDataBindingView
import dagger.hilt.android.AndroidEntryPoint

/**
 * My tutorials view
 */
@AndroidEntryPoint
class MyTutorialFragment : Fragment() {

  private lateinit var binding: FragmentMyTutorialsBinding

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = setDataBindingView(R.layout.fragment_my_tutorials, container)
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
  }

  private fun initView() {
    val adapter = MyTutorialsPagerAdapter(this)
    binding.viewPager.adapter = adapter
    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      tab.text = getTabTextForPosition(position)
    }.attach()
    binding.viewPager.isUserInputEnabled = false
    binding.navigationActionSettings.setOnClickListener(
      Navigation.createNavigateOnClickListener(
        R.id.action_navigation_my_tutorials_to_navigation_settings
      )
    )
  }

  private fun getTabTextForPosition(position: Int): String {
    return when (MyTutorialsPagerAdapter.MyTutorialPosition.values()[position]) {
      MyTutorialsPagerAdapter.MyTutorialPosition.InProgress ->
        requireContext().getString(R.string.my_tutorials_tab_in_progress)
      MyTutorialsPagerAdapter.MyTutorialPosition.Completed ->
        requireContext().getString(R.string.my_tutorials_tab_completed)
      else -> requireContext().getString(R.string.my_tutorials_tab_bookmarks)
    }
  }
}
