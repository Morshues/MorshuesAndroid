package com.morshues.morshuesandroid.data.repository

import com.morshues.morshuesandroid.data.db.dao.SyncTaskDao
import com.morshues.morshuesandroid.data.db.entity.SyncStatus
import com.morshues.morshuesandroid.data.db.entity.SyncTask
import com.morshues.morshuesandroid.data.db.entity.SyncType
import kotlinx.coroutines.flow.Flow

class SyncTaskRepository(private val syncTaskDao: SyncTaskDao) {

    suspend fun addTasks(tasks: List<SyncTask>): List<Long> {
        if (tasks.isEmpty()) return emptyList()

        val filePaths = tasks.map { it.filePath }
        val existingTasks = syncTaskDao.findExistingTasks(filePaths)
        val existingFilePaths = existingTasks.map { it.filePath }.toSet()
        val uniqueTasks = tasks.filter { task ->
            task.filePath !in existingFilePaths
        }

        return if (uniqueTasks.isNotEmpty()) {
            syncTaskDao.insertTasks(uniqueTasks)
        } else {
            emptyList()
        }
    }

    suspend fun getPendingTasks(limit: Int): List<SyncTask> {
        return syncTaskDao.getPendingTasks(SyncStatus.PENDING, limit)
    }

    fun getActiveTaskCount(): Flow<Int> {
        return syncTaskDao.getTaskCountByStatus(SyncStatus.IN_PROGRESS)
    }

    fun getPendingTaskCount(): Flow<Int> {
        return syncTaskDao.getTaskCountByStatus(SyncStatus.PENDING)
    }

    suspend fun markTaskStartedWithWorker(taskId: Long, workerId: String) {
        syncTaskDao.markTaskStartedWithWorker(taskId, workerId)
    }

    suspend fun markTaskCompleted(taskId: Long) {
        syncTaskDao.markTaskCompleted(taskId)
    }

    suspend fun markTaskFailed(taskId: Long, errorMessage: String) {
        syncTaskDao.markTaskFailed(taskId, errorMessage = errorMessage)
    }

    suspend fun deleteTasksBySyncType(syncType: SyncType) {
        syncTaskDao.deleteTasksBySyncType(syncType)
    }

    suspend fun deleteTasksByFolder(folderPath: String) {
        syncTaskDao.deleteTasksByFolder(folderPath)
    }

    suspend fun clearCompletedTasks() {
        syncTaskDao.deleteTasksByStatus(SyncStatus.COMPLETED)
    }

    suspend fun clearFailedTasks() {
        syncTaskDao.deleteTasksByStatus(SyncStatus.FAILED)
    }
}