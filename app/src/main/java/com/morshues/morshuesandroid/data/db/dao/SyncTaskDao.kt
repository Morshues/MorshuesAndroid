package com.morshues.morshuesandroid.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.morshues.morshuesandroid.data.db.entity.SyncStatus
import com.morshues.morshuesandroid.data.db.entity.SyncTask
import com.morshues.morshuesandroid.data.db.entity.SyncType
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncTaskDao {
    // Insert tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: SyncTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<SyncTask>): List<Long>

    // Query tasks
    @Query("SELECT * FROM sync_tasks WHERE status = :status ORDER BY priority DESC, createdAt ASC LIMIT :limit")
    suspend fun getPendingTasks(status: SyncStatus = SyncStatus.PENDING, limit: Int): List<SyncTask>

    @Query("SELECT * FROM sync_tasks WHERE filePath IN (:filePaths) AND status IN ('PENDING', 'IN_PROGRESS')")
    suspend fun findExistingTasks(filePaths: List<String>): List<SyncTask>

    @Query("SELECT COUNT(*) FROM sync_tasks WHERE status = :status")
    fun getTaskCountByStatus(status: SyncStatus): Flow<Int>

    @Query("SELECT * FROM sync_tasks WHERE status = :status ORDER BY completedAt DESC")
    fun getTasksByStatus(status: SyncStatus): Flow<List<SyncTask>>

    // Update tasks
    @Query("UPDATE sync_tasks SET status = :status, startedAt = :startedAt, workRequestId = :workerId WHERE id = :taskId")
    suspend fun markTaskStartedWithWorker(taskId: Long, workerId: String, status: SyncStatus = SyncStatus.IN_PROGRESS, startedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_tasks SET status = :status, completedAt = :completedAt WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Long, status: SyncStatus = SyncStatus.COMPLETED, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_tasks SET status = :status, errorMessage = :errorMessage WHERE id = :taskId")
    suspend fun markTaskFailed(taskId: Long, status: SyncStatus = SyncStatus.FAILED, errorMessage: String)

    // Delete tasks
    @Delete
    suspend fun deleteTask(task: SyncTask)

    @Query("DELETE FROM sync_tasks WHERE status = :status")
    suspend fun deleteTasksByStatus(status: SyncStatus)

    @Query("DELETE FROM sync_tasks WHERE syncType = :syncType")
    suspend fun deleteTasksBySyncType(syncType: SyncType)

    @Query("DELETE FROM sync_tasks WHERE folderPath = :folderPath")
    suspend fun deleteTasksByFolder(folderPath: String)
}
