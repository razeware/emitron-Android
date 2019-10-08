package com.raywenderlich.emitron.ui.mytutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentMyTutorialsBinding
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import kotlinx.android.synthetic.main.fragment_my_tutorials.*

/**
 * My tutorials view
 */
class MyTutorialFragment : Fragment() {

  private lateinit var binding: FragmentMyTutorialsBinding

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
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
    val adapter = MyTutorialsPagerAdapter(context, childFragmentManager)
    binding.viewPager.adapter = adapter
    binding.tabLayout.setupWithViewPager(view_pager)
    binding.navigationActionSettings.setOnClickListener(
      Navigation.createNavigateOnClickListener(
        R.id.action_navigation_my_tutorials_to_navigation_settings
      )
    )
  }
}
