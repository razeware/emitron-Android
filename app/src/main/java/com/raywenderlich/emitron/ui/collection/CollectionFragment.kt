package com.raywenderlich.emitron.ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class CollectionFragment : Fragment() {

  private lateinit var contentViewModel: CollectionViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    contentViewModel =
      ViewModelProviders.of(this).get(CollectionViewModel::class.java)
    return inflater.inflate(R.layout.fragment_collection, container, false)
  }
}
