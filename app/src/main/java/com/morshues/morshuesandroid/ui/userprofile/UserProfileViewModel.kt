package com.morshues.morshuesandroid.ui.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val sessionStore: SessionStore,
) : ViewModel() {
    val user = sessionStore.user

    fun logout() {
        viewModelScope.launch {
            sessionStore.clear()
        }
    }
}