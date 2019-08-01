package com.raywenderlich.emitron.model

/**
 * Content completion status
 *
 * @param httpRequestValue Value to be use in http requests
 */
enum class CompletionStatus(val httpRequestValue: String) {
  InProgress("in_progress"),
  Completed("completed");

  companion object {

    /**
     * Map of all [CompletionStatus]
     */
    internal val map = values().associateBy(CompletionStatus::name)

    /**
     * @param type String value of [CompletionStatus]
     *
     * @return [CompletionStatus]
     */
    fun fromValue(type: String?): CompletionStatus? = type?.let {
      map[it.capitalize()]
    }
  }
}
