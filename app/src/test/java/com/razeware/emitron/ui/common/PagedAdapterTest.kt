package com.razeware.emitron.ui.common

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.razeware.emitron.utils.NetworkState
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
  fun updateNetworkState() {
    // When
    pagedAdapter.updateNetworkState(
      2,
      NetworkState.INIT_FAILED,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.ERROR
    verify(onChangeLastItem).invoke(false)
  }

  @Test
  fun updateNetworkState_B() {
    // Given
    pagedAdapter.networkState = NetworkState.FAILED

    // When
    pagedAdapter.updateNetworkState(
      2,
      NetworkState.INIT_EMPTY,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo UiStateManager.UiState.ERROR_EMPTY
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun updateNetworkState_C() {
    // Given
    pagedAdapter.networkState = NetworkState.FAILED

    // When
    pagedAdapter.updateNetworkState(
      2,
      NetworkState.INIT,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.uiState isEqualTo null
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun updateNetworkState_D() {

    // Given
    pagedAdapter.networkState = null

    // When
    pagedAdapter.updateNetworkState(
      2,
      NetworkState.FAILED,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.networkState isEqualTo NetworkState.FAILED
    verify(onChangeLastItem).invoke(false)
  }

  @Test
  fun updateNetworkState_E() {

    // Given
    pagedAdapter.networkState = NetworkState.FAILED

    // When
    pagedAdapter.updateNetworkState(
      2, NetworkState.RUNNING,
      onChangeItem, onChangeLastItem
    )

    // Then
    pagedAdapter.networkState isEqualTo NetworkState.RUNNING
    verify(onChangeItem).invoke(1)
  }

  @Test
  fun updateNetworkState_F() {
    // Given
    pagedAdapter.networkState = null

    // When
    pagedAdapter.updateNetworkState(
      2,
      NetworkState.RUNNING,
      onChangeItem,
      onChangeLastItem
    )

    // Then
    pagedAdapter.networkState isEqualTo NetworkState.RUNNING
    verify(onChangeLastItem).invoke(false)
  }

  @Test
  fun updateUiState() {

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

    pagedAdapter.uiState = UiStateManager.UiState.ERROR_EMPTY
    assertThat(pagedAdapter.hasUiStateError()).isTrue()

    pagedAdapter.uiState = UiStateManager.UiState.LOADING
    assertThat(pagedAdapter.hasUiStateError()).isFalse()
  }

  @Test
  fun hasExtraRow() {
    val pagedAdapter = PagedAdapter()
    assertThat(pagedAdapter.hasExtraRow()).isFalse()

    pagedAdapter.networkState = NetworkState.RUNNING
    assertThat(pagedAdapter.hasExtraRow()).isTrue()

    pagedAdapter.networkState = null
    pagedAdapter.uiState = UiStateManager.UiState.ERROR
    assertThat(pagedAdapter.hasExtraRow()).isTrue()

    pagedAdapter.networkState = null
    pagedAdapter.uiState = UiStateManager.UiState.LOADED
    assertThat(pagedAdapter.hasExtraRow()).isFalse()

    pagedAdapter.networkState = NetworkState.SUCCESS
    pagedAdapter.uiState = null
    assertThat(pagedAdapter.hasExtraRow()).isFalse()

    pagedAdapter.networkState = NetworkState.INIT_SUCCESS
    pagedAdapter.uiState = null
    assertThat(pagedAdapter.hasExtraRow()).isFalse()
  }
}
