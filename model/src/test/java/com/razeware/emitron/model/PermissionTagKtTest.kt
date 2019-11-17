package com.razeware.emitron.model

import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class PermissionTagKtTest {

  @Test
  fun isDownloadPermissionTag() {
    val permissionTag = PermissionTag.Download

    permissionTag.isDownloadPermissionTag() isEqualTo true
  }
}
