package com.raywenderlich.emitron.data.filter

import androidx.lifecycle.LiveData
import com.raywenderlich.emitron.data.filter.dao.CategoryDao
import com.raywenderlich.emitron.data.filter.dao.DomainDao
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.entity.Category
import com.raywenderlich.emitron.model.entity.Domain
import javax.inject.Inject

/**
 * Local data source to fetch filter categories/domains
 */
class FilterDataSourceLocal @Inject constructor(
  private val domainDao: DomainDao,
  private val categoryDao: CategoryDao
) {

  /**
   * Save domains to database
   */
  suspend fun saveDomains(domains: List<Data>) {
    val domainList = Domain.listFrom(domains)
    domainDao.insertDomains(domainList)
  }

  /**
   * Save categories to database
   */
  suspend fun saveCategories(categories: List<Data>) {
    val categoryList = Category.listFrom(categories)
    categoryDao.insertCategories(categoryList)
  }

  /**
   * Get observer for category table
   */
  fun getCategories(): LiveData<List<Category>> = categoryDao.getCategories()

  /**
   * Get observer for domain table
   */
  fun getDomains(): LiveData<List<Domain>> = domainDao.getDomains()

}
