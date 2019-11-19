package com.razeware.emitron.ui.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.filter.FilterRepository
import com.razeware.emitron.model.*
import com.razeware.emitron.utils.*
import com.razeware.emitron.utils.async.ThreadManager
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

    val domains = MutableLiveData<List<Data>>().apply {
      value = listOf(
        Data(
          id = "1",
          type = "domains",
          attributes = Attributes(name = "iOS and Swift")
        ),
        Data(
          id = "2",
          type = "domains",
          attributes = Attributes(name = "Android and Kotlin")
        )
      )
    }
    whenever(filterRepository.getDomains()).doReturn(domains)

    val categories = MutableLiveData<List<Data>>().apply {
      value = listOf(
        Data(
          id = "1",
          type = "categories",
          attributes = Attributes(name = "Algorithms")
        ),
        Data(
          id = "2",
          type = "categories",
          attributes = Attributes(name = "Architecture")
        )
      )
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
      Data(
        type = FilterType.ContentType.toRequestFormat(),
        attributes = Attributes(name = "Video Course", contentType = "collection")
      ),
      Data(
        type = FilterType.ContentType.toRequestFormat(),
        attributes = Attributes(name = "Screencast", contentType = "screencast")
      )
    )

    val result = viewModel.getContentTypeList(
      mapOf(
        ContentType.Collection to "Video Course",
        ContentType.Screencast to "Screencast"
      )
    )

    result isEqualTo expected
  }

  @Test
  fun getDifficultyList() {
    val expected = listOf(
      Data(
        type = FilterType.Difficulty.toRequestFormat(),
        attributes = Attributes(name = "Beginner")
      ),
      Data(
        type = FilterType.Difficulty.toRequestFormat(),
        attributes = Attributes(name = "Advanced")
      )
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
