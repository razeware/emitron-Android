package com.raywenderlich.emitron.ui.mytutorial

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkFragment
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionFragment

class MyTutorialsPagerAdapter(private val context: Context?, fm: FragmentManager) :
  FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  override fun getCount(): Int = 3

  override fun getItem(position: Int): Fragment {
    return when (position) {
      0 -> ProgressionFragment.newInstanceInProgress()
      1 -> ProgressionFragment()
      else -> BookmarkFragment()
    }
  }

  override fun getPageTitle(position: Int): CharSequence {
    return when (position) {
      0 -> context?.getString(R.string.my_tutorials_tab_in_progress) ?: ""
      1 -> context?.getString(R.string.my_tutorials_tab_completed) ?: ""
      else -> context?.getString(R.string.my_tutorials_tab_bookmarks) ?: ""
    }
  }
}
