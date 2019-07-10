package com.raywenderlich.emitron.di.impl

import javax.inject.Inject

class SessionManagerImpl @Inject constructor() : SessionManager {
  override fun getUserEmailAddress(): String {
    return ""
  }

  override fun getUserAccountId(): String {
    return ""
  }

  override fun getUserName(): String {
    return ""
  }
}
