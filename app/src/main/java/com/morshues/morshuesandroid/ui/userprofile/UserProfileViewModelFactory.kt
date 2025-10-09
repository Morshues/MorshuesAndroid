package com.morshues.morshuesandroid.ui.userprofile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.morshuesandroid.data.SessionStore

class UserProfileViewModelFactory(
    private val application: Application,
    private val sessionStore: SessionStore,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(application, sessionStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}