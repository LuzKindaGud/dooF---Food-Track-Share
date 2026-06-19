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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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

    private val _updateState = MutableLiveData<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState: LiveData<ProfileUpdateState> = _updateState

    private val _resetPasswordState = MutableLiveData<ProfileUpdateState>(ProfileUpdateState.Idle)
    val resetPasswordState: LiveData<ProfileUpdateState> = _resetPasswordState

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

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            val result = authRepository.updateUserProfile(displayName)
            if (result.isSuccess) {
                _updateState.value = ProfileUpdateState.Success
            } else {
                _updateState.value = ProfileUpdateState.Error(result.exceptionOrNull()?.message ?: "Update failed")
            }
        }
    }

    fun resetPassword() {
        val email = userProfile.value?.email ?: return
        viewModelScope.launch {
            _resetPasswordState.value = ProfileUpdateState.Loading
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                _resetPasswordState.value = ProfileUpdateState.Success
            } else {
                _resetPasswordState.value = ProfileUpdateState.Error(result.exceptionOrNull()?.message ?: "Reset failed")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = ProfileUpdateState.Idle
        _resetPasswordState.value = ProfileUpdateState.Idle
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

sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    object Success : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}
