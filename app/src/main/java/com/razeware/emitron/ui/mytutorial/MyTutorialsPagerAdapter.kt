package com.razeware.emitron.ui.mytutorial

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.razeware.emitron.R
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkFragment
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionFragment

/**
 * Adapter for ViewPager in My tutorials screen
 */
class MyTutorialsPagerAdapter(private val context: Context?, fm: FragmentManager) :
  FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  /**
   * See [FragmentStatePagerAdapter.getCount]
   */
  override fun getCount(): Int = 3

  /**
   * See [FragmentStatePagerAdapter.getItem]
   */
  override fun getItem(position: Int): Fragment {
    return when (MyTutorialPosition.values()[position]) {
      MyTutorialPosition.InProgress -> ProgressionFragment.newInstanceInProgress()
      MyTutorialPosition.Completed -> ProgressionFragment()
      else -> BookmarkFragment()
    }
  }

  override fun getPageTitle(position: Int): CharSequence {
    return when (MyTutorialPosition.values()[position]) {
      MyTutorialPosition.InProgress -> context?.getString(R.string.my_tutorials_tab_in_progress)
        ?: ""
      MyTutorialPosition.Completed -> context?.getString(R.string.my_tutorials_tab_completed)
        ?: ""
      else -> context?.getString(R.string.my_tutorials_tab_bookmarks) ?: ""
    }
  }

  /**
   * Position of views on my tutorial screen (in order of declaration)
   */
  enum class MyTutorialPosition {
    /**
     * In Progress content
     */
    InProgress,
    /**
     * Completed content
     */
    Completed,
    /**
     * Bookmarked content
     */
    Bookmarked
  }
}
