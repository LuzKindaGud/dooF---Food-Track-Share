package com.example.doancoso3.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val userProfile: LiveData<UserEntity?> = authRepository.currentUser
        .flatMapLatest { firebaseUser ->
            android.util.Log.d("ProfileViewModel", "Current user changed: ${firebaseUser?.uid}")
            firebaseUser?.uid?.let { uid ->
                authRepository.observeUserProfile(uid)
            } ?: flowOf(null)
        }
        .distinctUntilChanged()
        .onEach { entity ->
            android.util.Log.d("ProfileViewModel", "Observed profile update: ${entity?.displayName}")
        }
        .asLiveData()

    private val _isLoggedOut = MutableLiveData<Boolean>(false)
    val isLoggedOut: LiveData<Boolean> = _isLoggedOut

    // Preferences (Static for now or can be moved to DataStore/Room)
    private val _pushNotificationsEnabled = MutableLiveData(true)
    val pushNotificationsEnabled: LiveData<Boolean> = _pushNotificationsEnabled

    private val _familySyncEnabled = MutableLiveData(false)
    val familySyncEnabled: LiveData<Boolean> = _familySyncEnabled

    init {
        syncProfile()
    }

    private fun syncProfile() {
        authRepository.getCurrentUser()?.uid?.let { uid ->
            viewModelScope.launch {
                authRepository.syncUserProfile(uid)
            }
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _pushNotificationsEnabled.value = enabled
    }

    fun toggleFamilySync(enabled: Boolean) {
        _familySyncEnabled.value = enabled
    }

    fun logout() {
        authRepository.logout()
        _isLoggedOut.value = true
    }
}
