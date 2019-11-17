package com.razeware.emitron.model

import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class FilterTypeTest {

  @Test
  fun isDomain() {
    val filterType = FilterType.Domains

    filterType.isDomain() isEqualTo true
  }

  @Test
  fun isCategory() {
    val filterType = FilterType.Categories

    filterType.isCategory() isEqualTo true
  }

  @Test
  fun isContentType() {
    val filterType = FilterType.ContentType

    filterType.isContentType() isEqualTo true
  }


  @Test
  fun isDifficulty() {
    val filterType = FilterType.Difficulty

    filterType.isDifficulty() isEqualTo true
  }


  @Test
  fun isSearch() {
    val filterType = FilterType.Search

    filterType.isSearch() isEqualTo true
  }


  @Test
  fun isSort() {
    val filterType = FilterType.Sort

    filterType.isSort() isEqualTo true
  }


  @Test
  fun toRequestFormat() {
    val filterType = FilterType.ContentType

    filterType.toRequestFormat() isEqualTo "contentType"
  }
}
