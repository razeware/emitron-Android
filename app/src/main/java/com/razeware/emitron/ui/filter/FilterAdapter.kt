package com.razeware.emitron.ui.filter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.model.Data

internal class Filter(
  val category: FilterCategory? = null,
  val option: Pair<FilterCategory, Data>? = null
)

/**
 * Applicable filter categories
 */
enum class FilterCategory(
  /** Filter category res id */
  val resId: Int
) {
  /**
   * Filter platform
   */
  Platform(R.string.title_filter_platform),
  /**
   * Filter content type
   */
  ContentType(R.string.title_filter_content_type),
  /**
   * Filter difficulty
   */
  Difficulty(R.string.title_filter_difficulty),
  /**
   * Filter category
   */
  Category(R.string.title_filter_categories)
}

/**
 * Adapter for filter options
 */
class FilterAdapter(
  private val categories: MutableMap<FilterCategory, List<Data>>,
  private val onCategorySelected: (FilterCategory?) -> Unit
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val selectedOptions: MutableList<Data> = mutableListOf()
  private val selectedHeaders = SparseArrayCompat<Boolean>()
  private var filterOptions: List<Filter> = emptyList()

  init {
    updateFilterOptions()
  }

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_filter -> to show filter option
   * R.layout.item_filter_header -> to show filter headers i.e. platform, difficulty etc.
   */
  override fun getItemViewType(position: Int): Int {
    if (isHeaderPosition(position))
      return R.layout.item_filter_header

    return R.layout.item_filter
  }

  private fun isHeaderPosition(position: Int): Boolean {
    return null != filterOptions[position].category
  }

  /**
   * [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_filter -> FilterItemViewHolder.create(parent, viewType)
      else -> {
        FilterHeaderItemViewHolder.create(parent, viewType)
      }
    }
  }

  /**
   * [RecyclerView.Adapter.getItemCount]
   */
  override fun getItemCount(): Int = filterOptions.size

  /**
   * [RecyclerView.Adapter.onBindViewHolder]
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is FilterItemViewHolder -> {
        bindFilterItem(holder, position)
      }
      is FilterHeaderItemViewHolder -> {
        bindFilterHeader(holder, position)
      }
      else -> {
        // No such case
      }
    }
  }

  private fun bindFilterItem(holder: FilterItemViewHolder, position: Int) {
    val option = filterOptions[position].option ?: return
    val data = option.second
    val isPositionSelected = selectedOptions.contains(data)
    data.getName()?.let { title ->
      holder.bindTo(title, isPositionSelected) { clickPosition, isChecked ->
        val clickFilter = filterOptions.asSequence().elementAt(clickPosition).option
        clickFilter?.let {
          if (!isChecked) {
            clickFilter.let {
              selectedOptions.add(data)
            }
          } else {
            selectedOptions.remove(data)
          }
        }
        notifyItemChanged(clickPosition)
      }
    }
  }

  private fun bindFilterHeader(holder: FilterHeaderItemViewHolder, position: Int) {
    val filterCategoryResId = filterOptions[position].category?.resId ?: 0
    val filterCategory = if (filterCategoryResId != 0) {
      holder.itemView.context.getString(filterCategoryResId)
    } else {
      ""
    }
    val isPositionExpanded = selectedHeaders[position] ?: false
    holder.bindTo(filterCategory, isPositionExpanded) { clickPosition ->
      val isClickedHeaderExpanded = selectedHeaders[clickPosition] ?: false
      if (isClickedHeaderExpanded) {
        val selectedFilterCategory =
          filterOptions.asSequence().elementAt(clickPosition).category
        selectedFilterCategory?.let {
          val childCount = categories[selectedFilterCategory]?.size ?: 0
          categories[selectedFilterCategory] = emptyList()
          updateFilterOptions()
          notifyItemRangeRemoved(clickPosition + 1, childCount)
          notifyItemChanged(clickPosition)
        }
      } else {
        val category = filterOptions[clickPosition].category
        onCategorySelected(category)
      }
      selectedHeaders.put(clickPosition, !isPositionExpanded)
    }
  }

  /**
   * [RecyclerView.Adapter.onViewRecycled]
   *
   * Clear up on recycle
   */
  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    super.onViewRecycled(holder)
    (holder as? FilterItemViewHolder)?.unBind()
    (holder as? FilterHeaderItemViewHolder)?.unBind()
  }

  private fun updateFilterOptions() {
    filterOptions = categories.flatMap {
      listOf(
        Filter(it.key),
        *(it.value.map { data -> Filter(option = it.key to data) }.toTypedArray())
      )
    }
  }

  /**
   * Set filters
   *
   * @param options List of filter options
   * @param filterHeader id of header
   */
  fun setFilterOptions(options: List<Data>?, filterHeader: FilterCategory?) {
    if (null == filterHeader) return
    if (null == options || options.isEmpty()) return
    categories[filterHeader] = options
    updateFilterOptions()
    notifyDataSetChanged()
  }

  /**
   * Set selected filters
   */
  fun setSelectedOptions(selectedFilters: List<Data>) {
    selectedOptions.clear()
    selectedOptions.addAll(selectedFilters)
  }

  /**
   * Get selected filters
   */
  fun getSelectedOptions(): List<Data> = selectedOptions.toList()

}
