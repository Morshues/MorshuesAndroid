package com.morshues.morshuesandroid.ui.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {
    val user = sessionStore.user

    fun logout() {
        viewModelScope.launch {
            sessionStore.clear()
        }
    }
}