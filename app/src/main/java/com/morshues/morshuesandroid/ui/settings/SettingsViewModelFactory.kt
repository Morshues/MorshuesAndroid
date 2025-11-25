package com.morshues.morshuesandroid.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase
import com.morshues.morshuesandroid.settings.SettingsManager

class SettingsViewModelFactory(
    private val settingsManager: SettingsManager,
    private val syncTaskRepository: SyncTaskRepository,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val syncTaskEnqueuer: SyncTaskEnqueuer,
    private val syncFolderUseCase: SyncFolderUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                settingsManager,
                syncTaskRepository,
                syncingFolderRepository,
                syncTaskEnqueuer,
                syncFolderUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
