package com.razeware.emitron.model.entity

import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class WatchStatTest {

  @Test
  fun toData() {
    val watchStat = WatchStat(
      "1", 30, "" +
          "2019111621", "2019111621"
    )

    watchStat.toData() isEqualTo Data(
      type = "watch_stats",
      attributes = Attributes(
        contentId = "1",
        seconds = 30,
        watchedOn = "2019111621"
      )
    )
  }
}
