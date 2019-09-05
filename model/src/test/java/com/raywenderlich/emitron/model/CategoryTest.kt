package com.raywenderlich.emitron.model

import org.junit.Test

class CategoryTest {

  @Test
  fun listFrom() {
    val result = Category.listFrom(listOf(
        Data(id = "1",
            attributes = Attributes(name = "Architecture")),
        Data(id = "2",
            attributes = Attributes(name = "Algorithms")))
    )
    result[0].categoryId = "1"
    result[0].name = "Architecture"
    result[0].categoryId = "2"
    result[0].name = "Algorithms"
  }
}
