package com.raywenderlich.emitron.di.impl

interface SessionManager {
  fun getUserEmailAddress(): String
  fun getUserAccountId(): String
  fun getUserName(): String // As in name ex. Nathan
}
