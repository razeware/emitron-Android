package com.raywenderlich.emitron.utils.extensions

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*

/**
 * Run block inside Observer
 */
fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T?) -> Unit) =
  observe(owner, Observer { block(it) })

/**
 * Create viewmodel for activity
 */
inline fun <reified T : ViewModel> AppCompatActivity.createViewModel(viewModelFactory: ViewModelProvider.Factory): T =
  androidx.lifecycle.ViewModelProviders.of(this, viewModelFactory)[T::class.java]


fun <T : ViewDataBinding> Activity.setDataBindingView(layoutId: Int): T =
  DataBindingUtil.setContentView(this, layoutId)
