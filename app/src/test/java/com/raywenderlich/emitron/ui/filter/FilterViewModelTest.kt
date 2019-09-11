package com.raywenderlich.emitron.ui.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.filter.FilterRepository
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.utils.*
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class FilterViewModelTest {

  private lateinit var viewModel: FilterViewModel

  private val filterRepository: FilterRepository = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  private val threadManager: ThreadManager = mock()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.networkExecutor).doReturn(CurrentThreadExecutor())

    val domains = MutableLiveData<List<Domain>>().apply {
      value = listOf(
        Domain().apply {
          domainId = "1"
          name = "iOS and Swift"
        },
        Domain().apply {
          domainId = "2"
          name = "Android and Kotlin"
        },
        Domain().apply {
          domainId = "3"
          name = "Unreal"
          level = "archived"
        })
    }
    whenever(filterRepository.getDomains()).doReturn(domains)

    val categories = MutableLiveData<List<Category>>().apply {
      value = listOf(
        Category().apply {
          categoryId = "1"
          name = "Algorithms"
        },
        Category().apply {
          categoryId = "2"
          name = "Architecture"
        })
    }
    whenever(filterRepository.getCategories()).doReturn(categories)
    viewModel = FilterViewModel(filterRepository)
  }

  @Test
  fun testInit() {
    verify(filterRepository).getCategories()
    verify(filterRepository).getDomains()
    verifyNoMoreInteractions(filterRepository)
  }

  @Test
  fun getDomains() {
    testCoroutineRule.runBlockingTest {
      viewModel.getDomains()
      verify(filterRepository).getCategories()
      verify(filterRepository).getDomains()
      verify(filterRepository).fetchDomains()
      verifyNoMoreInteractions(filterRepository)
    }
  }

  @Test
  fun getDomains_withError() {
    testCoroutineRule.runBlockingTest {
      whenever(filterRepository.fetchDomains()).doThrow(IOException())

      viewModel.getDomains()
      val result = viewModel.loadFilterOptionsResult.observeForTestingResultNullable()
      result?.getContentIfNotHandled() isEqualTo FilterViewModel.LoadFilterOptionResult.FailedToFetchDomains
    }
  }

  @Test
  fun getCategories() {
    testCoroutineRule.runBlockingTest {
      viewModel.getCategories()
      verify(filterRepository).getCategories()
      verify(filterRepository).getDomains()
      verify(filterRepository).fetchCategories()
      verifyNoMoreInteractions(filterRepository)
    }
  }

  @Test
  fun getCategories_withError() {
    testCoroutineRule.runBlockingTest {
      whenever(filterRepository.fetchCategories()).doThrow(IOException())

      viewModel.getCategories()

      val result = viewModel.loadFilterOptionsResult.observeForTestingResultNullable()
      result?.getContentIfNotHandled() isEqualTo
          FilterViewModel.LoadFilterOptionResult.FailedToFetchCategories
    }
  }

  @Test
  fun getContentTypeList() {
    val expected = listOf(
      Data(attributes = Attributes(name = "Video Course")),
      Data(attributes = Attributes(name = "Screencast"))
    )

    val result = viewModel.getDifficultyList(arrayOf("Video Course", "Screencast"))

    result isEqualTo expected
  }

  @Test
  fun getDifficultyList() {
    val expected = listOf(
      Data(attributes = Attributes(name = "Beginner")),
      Data(attributes = Attributes(name = "Advanced"))
    )

    val result = viewModel.getDifficultyList(arrayOf("Beginner", "Advanced"))

    result isEqualTo expected
  }

  @Test
  fun hasDomains() {

    val result = viewModel.domains.observeForTestingResultNullable()
    viewModel.hasDomains() isEqualTo true

    result isEqualTo
        listOf(
          Data(
            id = "1",
            type = DataType.Domains.toRequestFormat(),
            attributes = Attributes(name = "iOS and Swift")
          ),
          Data(
            id = "2",
            type = DataType.Domains.toRequestFormat(),
            attributes = Attributes(name = "Android and Kotlin")
          )
        )

  }

  @Test
  fun hasCategories() {

    val result = viewModel.categories.observeForTestingResult()
    
    viewModel.hasCategories() isEqualTo true

    result isEqualTo listOf(
      Data(
        id = "1",
        type = DataType.Categories.toRequestFormat(),
        attributes = Attributes(name = "Algorithms")
      ),
      Data(
        id = "2",
        type = DataType.Categories.toRequestFormat(),
        attributes = Attributes(name = "Architecture")
      )
    )
  }
}
