package com.raywenderlich.emitron.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class PlayerFragment : Fragment() {

  private lateinit var playerViewModel: PlayerViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    playerViewModel =
      ViewModelProviders.of(this).get(PlayerViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_player, container, false)
    return root
  }
}
