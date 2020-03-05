package com.razeware.emitron.ui.common

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Test

class PagedAdapterTest {

  private lateinit var pagedAdapter: PagedAdapter
  private val onChangeItem: (Int) -> Unit = mock()
  private val onChangeLastItem: (Boolean) -> Unit = mock()

  @Before
  fun setUp() {
    pagedAdapter = PagedAdapter()
  }

  @Test
  fun updateUiState() {
    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.INIT_FAILED,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.INIT_FAILED
    verify(onChangeLastItem).invoke(false)
  }

  @Test
  fun updateUiState_B() {
    // Given
    pagedAdapter.uiState = UiStateManager.UiState.ERROR

    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.INIT_EMPTY,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.INIT_EMPTY
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun updateUiState_C() {
    // Given
    pagedAdapter.uiState = UiStateManager.UiState.ERROR

    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.INIT,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.INIT
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun updateUiState_D() {

    // Given
    pagedAdapter.uiState = null

    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.ERROR,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.ERROR
    verify(onChangeLastItem).invoke(false)
  }

  @Test
  fun updateUiState_E() {

    // Given
    pagedAdapter.uiState = UiStateManager.UiState.ERROR

    // When
    pagedAdapter.updateUiState(
      2, UiStateManager.UiState.LOADING,
      onChangeItem, onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.LOADING
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun updateUiState_F() {
    // Given
    pagedAdapter.uiState = null

    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.LOADING,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.LOADING
    verify(onChangeLastItem).invoke(false)
  }

  @Test
  fun updateUiState_G() {

    // Given
    val pagedAdapter = PagedAdapter()
    val onChangeItem: (Int) -> Unit = mock()
    val onChangeLastItem: (Boolean) -> Unit = mock()

    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.ERROR_CONNECTION,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.ERROR_CONNECTION
    verify(onChangeLastItem).invoke(false)

    // When
    pagedAdapter.updateUiState(
      2,
      UiStateManager.UiState.LOADED,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.LOADED
    verify(onChangeLastItem).invoke(true)

    // Given
    pagedAdapter.uiState = UiStateManager.UiState.ERROR

    // When
    pagedAdapter.updateUiState(
      2, UiStateManager.UiState.LOADING,
      onChangeItem, onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.LOADING
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun hasUiStateError() {
    val pagedAdapter = PagedAdapter()
    pagedAdapter.uiState = UiStateManager.UiState.ERROR
    assertThat(pagedAdapter.hasUiStateError()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.ERROR_CONNECTION
    assertThat(pagedAdapter.hasUiStateError()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.EMPTY
    assertThat(pagedAdapter.hasUiStateError()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.INIT_EMPTY
    assertThat(pagedAdapter.hasUiStateError()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.LOADING
    assertThat(pagedAdapter.hasUiStateError()).isFalse()
  }

  @Test
  fun hasExtraRow() {
    val pagedAdapter = PagedAdapter()
    assertThat(pagedAdapter.hasExtraRow()).isFalse()

    pagedAdapter.uiState = UiStateManager.UiState.LOADING
    assertThat(pagedAdapter.hasExtraRow()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.ERROR
    assertThat(pagedAdapter.hasExtraRow()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.LOADED
    assertThat(pagedAdapter.hasExtraRow()).isFalse()

    pagedAdapter.uiState = UiStateManager.UiState.INIT_LOADED
    assertThat(pagedAdapter.hasExtraRow()).isFalse()
  }
}
