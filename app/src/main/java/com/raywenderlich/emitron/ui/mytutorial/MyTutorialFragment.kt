package com.raywenderlich.emitron.ui.mytutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class MyTutorialFragment : Fragment() {

  private lateinit var myTutorialViewModel: MyTutorialViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    myTutorialViewModel =
      ViewModelProviders.of(this).get(MyTutorialViewModel::class.java)
    return inflater.inflate(R.layout.fragment_my_tutorials, container, false)
  }
}
