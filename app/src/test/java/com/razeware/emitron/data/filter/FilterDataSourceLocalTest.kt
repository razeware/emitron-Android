package com.razeware.emitron.data.filter

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.razeware.emitron.data.filter.dao.CategoryDao
import com.razeware.emitron.data.filter.dao.DomainDao
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DataType
import com.razeware.emitron.model.entity.Category
import com.razeware.emitron.model.entity.Domain
import com.razeware.emitron.model.toRequestFormat
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterDataSourceLocalTest {

  private val categoryDao: CategoryDao = mock()
  private val domainDao: DomainDao = mock()

  private lateinit var dataSource: FilterDataSourceLocal

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    dataSource = FilterDataSourceLocal(domainDao, categoryDao)
  }

  @Test
  fun saveDomains() {
    testCoroutineRule.runBlockingTest {
      val domains = listOf(
        Data(
          "1",
          type = DataType.Domains.toRequestFormat(),
          attributes = Attributes(name = "iOS and Swift")
        ), Data(
          "2",
          type = DataType.Domains.toRequestFormat(),
          attributes = Attributes(name = "Android and Kotlin")
        )
      )

      dataSource.saveDomains(domains)

      val domainCaptor = argumentCaptor<List<Domain>>()

      verify(domainDao).insertDomains(domainCaptor.capture())

      domainCaptor.allValues.size isEqualTo 1
      with(domainCaptor.allValues[0]) {
        this[0].domainId isEqualTo "1"
        this[0].name isEqualTo "iOS and Swift"
        this[1].domainId isEqualTo "2"
        this[1].name isEqualTo "Android and Kotlin"
      }
      verifyNoMoreInteractions(domainDao)
    }
  }

  @Test
  fun saveCategories() {
    testCoroutineRule.runBlockingTest {
      val categories = listOf(
        Data(
          "3",
          type = DataType.Categories.toRequestFormat(),
          attributes = Attributes(name = "Algorithms")
        ), Data(
          "4",
          type = DataType.Categories.toRequestFormat(),
          attributes = Attributes(name = "Architecture")
        )
      )

      dataSource.saveCategories(categories)

      val categoryCaptor = argumentCaptor<List<Category>>()

      verify(categoryDao).insertCategories(categoryCaptor.capture())

      categoryCaptor.allValues.size isEqualTo 1
      with(categoryCaptor.allValues[0]) {
        this[0].categoryId isEqualTo "3"
        this[0].name isEqualTo "Algorithms"
        this[1].categoryId isEqualTo "4"
        this[1].name isEqualTo "Architecture"
      }
      verifyNoMoreInteractions(categoryDao)
    }
  }

  @Test
  fun getCategories() {
    dataSource.getCategories()
    verify(categoryDao).getCategories()
    verifyNoMoreInteractions(categoryDao)
  }

  @Test
  fun getDomains() {
    dataSource.getDomains()
    verify(domainDao).getDomains()
    verifyNoMoreInteractions(domainDao)
  }
}
