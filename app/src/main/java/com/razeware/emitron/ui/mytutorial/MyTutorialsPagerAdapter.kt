package com.razeware.emitron.ui.mytutorial

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkFragment
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionFragment

/**
 * Adapter for ViewPager in My tutorials screen
 */
class MyTutorialsPagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {

  /**
   * See [FragmentStatePagerAdapter.getCount]
   */
  override fun getItemCount(): Int = 3

  /**
   * See [FragmentStatePagerAdapter.getItem]
   */
  override fun createFragment(position: Int): Fragment {
    return when (MyTutorialPosition.values()[position]) {
      MyTutorialPosition.InProgress -> ProgressionFragment.newInstanceInProgress()
      MyTutorialPosition.Completed -> ProgressionFragment()
      else -> BookmarkFragment()
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
