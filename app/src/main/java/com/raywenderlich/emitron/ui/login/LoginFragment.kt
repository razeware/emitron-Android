package com.raywenderlich.emitron.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class LoginFragment : Fragment() {

  private lateinit var loginViewModel: LoginViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    loginViewModel =
      ViewModelProviders.of(this).get(LoginViewModel::class.java)
    return inflater.inflate(R.layout.fragment_login, container, false)
  }
}
