package com.raywenderlich.emitron.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class LibraryFragment : Fragment() {

  private lateinit var libraryViewModel: LibraryViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    libraryViewModel =
      ViewModelProviders.of(this).get(LibraryViewModel::class.java)
    return inflater.inflate(R.layout.fragment_library, container, false)
  }
}
