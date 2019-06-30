package com.raywenderlich.emitron.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class FilterFragment : Fragment() {

  private lateinit var filterViewModel: FilterViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    filterViewModel =
      ViewModelProviders.of(this).get(FilterViewModel::class.java)
    return inflater.inflate(R.layout.fragment_filter, container, false)
  }
}
