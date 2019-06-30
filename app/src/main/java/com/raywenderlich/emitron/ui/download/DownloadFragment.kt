package com.raywenderlich.emitron.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class DownloadFragment : Fragment() {

  private lateinit var downloadsViewModel: DownloadViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    downloadsViewModel =
      ViewModelProviders.of(this).get(DownloadViewModel::class.java)
    return inflater.inflate(R.layout.fragment_downloads, container, false)
  }
}
