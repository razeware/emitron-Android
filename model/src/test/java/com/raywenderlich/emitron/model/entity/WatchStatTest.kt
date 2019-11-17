package com.raywenderlich.emitron.model.entity

import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.utils.isEqualTo
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
