package com.morshues.morshuesandroid.ui.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.morshuesandroid.data.SessionStore

class UserProfileViewModelFactory(
    private val sessionStore: SessionStore,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(sessionStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}