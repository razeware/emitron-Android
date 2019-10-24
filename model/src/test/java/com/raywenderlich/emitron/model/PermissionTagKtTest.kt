package com.raywenderlich.emitron.model

import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class PermissionTagKtTest {

  @Test
  fun isDownloadPermissionTag() {
    val permissionTag = PermissionTag.Download

    permissionTag.isDownloadPermissionTag() isEqualTo true
  }
}
