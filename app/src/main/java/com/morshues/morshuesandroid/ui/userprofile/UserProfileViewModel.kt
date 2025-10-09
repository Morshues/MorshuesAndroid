package com.morshues.morshuesandroid.ui.userprofile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import kotlinx.coroutines.launch

class UserProfileViewModel(
    application: Application,
    private val sessionStore: SessionStore,
) : AndroidViewModel(application) {
    val user = sessionStore.user

    fun logout() {
        viewModelScope.launch {
            sessionStore.clear()
        }
    }
}