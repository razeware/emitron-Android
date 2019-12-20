package com.razeware.emitron.data.content.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.razeware.emitron.model.entity.Download
import com.razeware.emitron.model.entity.DownloadWithContent

/**
 * Dao for progressions
 */
@Dao
interface DownloadDao {

  /**
   * Insert new download request
   */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(download: Download)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(download: List<Download>)

  /**
   * Delete queued download
   */
  @Query(
    """DELETE from downloads
      WHERE downloads.download_id in(:downloadIds)"""
  )
  suspend fun delete(downloadIds: List<String>)

  /**
   * Delete all downloads
   */
  @Query("DELETE from downloads")
  suspend fun deleteAll()

  /**
   * Update content download progress
   *
   * @param downloadId Download Id
   * @param progress Download progress
   */
  @Query(
    """UPDATE downloads 
             SET progress = :progress,
             state = :state
             WHERE download_id = :downloadId
          """
  )
  suspend fun updateProgress(downloadId: String, progress: Int, state: Int)

  /**
   * Update content download state
   *
   * @param downloadIds List of download Ids
   * @param state New download state
   */
  @Query(
    """UPDATE downloads 
             SET state = :state
             WHERE download_id in(:downloadIds)
          """
  )
  suspend fun updateState(downloadIds: List<String>, state: Int)

  /**
   * Update content download url
   *
   * @param downloadId Download Id
   * @param url Download Url
   */
  @Query(
    """UPDATE downloads 
             SET url = :url,
             state = :state
             WHERE download_id = :downloadId
          """
  )
  suspend fun updateUrl(downloadId: String, url: String, state: Int)

  /**
   * Get queued downloads
   */
  @Query(
    """SELECT * FROM downloads
             INNER JOIN contents
             ON contents.content_id = downloads.download_id
             WHERE contents.content_type in(:contentTypes)
             AND downloads.state in(:states)
             ORDER BY downloads.created_at
             LIMIT :limit
          """
  )
  @Transaction
  suspend fun getQueuedDownloads(
    limit: Int,
    states: Array<Int>,
    contentTypes: Array<String>
  ): List<DownloadWithContent>

  /**
   * Get in progress downloads
   */
  @Query(
    """SELECT * FROM downloads
             INNER JOIN contents
             ON contents.content_id = downloads.download_id
             WHERE contents.content_type in(:contentTypes)
             AND downloads.state = :state
             ORDER BY downloads.created_at
          """
  )
  @Transaction
  suspend fun getInProgressDownloads(
    state: Int,
    contentTypes: Array<String>
  ): List<DownloadWithContent>

  /**
   * Get queued downloads
   */
  @Query(
    """SELECT * FROM downloads
             INNER JOIN contents
             ON contents.content_id = downloads.download_id
             WHERE downloads.download_id = :id
          """
  )
  @Transaction
  suspend fun getDownload(
    id: String
  ): DownloadWithContent?

  /**
   * Get queued downloads
   */
  @Query(
    """SELECT * FROM downloads
             INNER JOIN contents
             ON contents.content_id = downloads.download_id
             WHERE contents.content_type in(:contentTypes)
             ORDER BY created_at DESC
          """
  )
  @Transaction
  fun getQueuedDownloads(
    contentTypes: Array<String>
  ): DataSource.Factory<Int, DownloadWithContent>

  /**
   * Get all downloads by id
   *
   * @param ids List of downloads
   */
  @Query(
    """SELECT * FROM downloads
             WHERE download_id in(:ids)
          """
  )
  fun getDownloadsById(ids: List<String>): LiveData<List<Download>>
}
