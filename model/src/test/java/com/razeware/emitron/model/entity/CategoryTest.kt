package com.razeware.emitron.model.entity

import com.google.common.truth.Truth
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class CategoryTest {

  @Test
  fun toData() {
    val domain = Category(categoryId = "1", name = "Architecture")

    Truth.assertThat(domain.toData()).isEqualTo(
      Data(
        id = "1",
        type = "categories",
        attributes = Attributes(name = "Architecture")
      )
    )
  }

  @Test
  fun listFrom() {
    val result = Category.listFrom(
      listOf(
        Data(
          id = "1",
          type = "categories",
          attributes = Attributes(name = "Architecture")
        ),
        Data(
          id = "2",
          type = "categories",
          attributes = Attributes(name = "Algorithms")
        )
      )
    )
    result[0].categoryId isEqualTo "1"
    result[0].name isEqualTo "Architecture"
    result[1].categoryId isEqualTo "2"
    result[1].name isEqualTo "Algorithms"
  }
}
