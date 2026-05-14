package com.example.doancoso3.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.repository.AuthRepository
import com.example.doancoso3.utils.AuthValidator
import com.example.doancoso3.utils.ValidationResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- Login State ---
    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    // --- Register State ---
    private val _registerState = MutableLiveData<AuthState>()
    val registerState: LiveData<AuthState> = _registerState

    // --- Reset Password State ---
    private val _resetPasswordState = MutableLiveData<ResetPasswordState>()
    val resetPasswordState: LiveData<ResetPasswordState> = _resetPasswordState

    // --- Validation Errors ---
    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError

    private val _displayNameError = MutableLiveData<String?>()
    val displayNameError: LiveData<String?> = _displayNameError

    fun getCurrentUser(): FirebaseUser? = authRepository.getCurrentUser()

    fun login(email: String, password: String) {
        // Clear previous errors
        _emailError.value = null
        _passwordError.value = null

        // Validate
        val emailValidation = AuthValidator.validateEmail(email)
        val passwordValidation = AuthValidator.validatePassword(password)

        var hasError = false

        if (emailValidation is ValidationResult.Error) {
            _emailError.value = emailValidation.message
            hasError = true
        }
        if (passwordValidation is ValidationResult.Error) {
            _passwordError.value = passwordValidation.message
            hasError = true
        }

        if (hasError) return

        _loginState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { _loginState.value = AuthState.Success(it) },
                onFailure = { _loginState.value = AuthState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun register(email: String, password: String, confirmPassword: String, displayName: String) {
        // Clear previous errors
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
        _displayNameError.value = null

        // Validate
        val emailValidation = AuthValidator.validateEmail(email)
        val passwordValidation = AuthValidator.validatePassword(password)
        val confirmValidation = AuthValidator.validateConfirmPassword(password, confirmPassword)

        var hasError = false

        if (displayName.isBlank()) {
            _displayNameError.value = "Tên hiển thị không được để trống"
            hasError = true
        }
        if (emailValidation is ValidationResult.Error) {
            _emailError.value = emailValidation.message
            hasError = true
        }
        if (passwordValidation is ValidationResult.Error) {
            _passwordError.value = passwordValidation.message
            hasError = true
        }
        if (confirmValidation is ValidationResult.Error) {
            _confirmPasswordError.value = confirmValidation.message
            hasError = true
        }

        if (hasError) return

        _registerState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.register(email, password, displayName)
            result.fold(
                onSuccess = { _registerState.value = AuthState.Success(it) },
                onFailure = { _registerState.value = AuthState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun resetPassword(email: String) {
        _emailError.value = null

        val emailValidation = AuthValidator.validateEmail(email)
        if (emailValidation is ValidationResult.Error) {
            _emailError.value = emailValidation.message
            return
        }

        _resetPasswordState.value = ResetPasswordState.Loading

        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            result.fold(
                onSuccess = { _resetPasswordState.value = ResetPasswordState.Success },
                onFailure = { _resetPasswordState.value = ResetPasswordState.Error(mapFirebaseError(it)) }
            )
        }
    }

    private fun mapFirebaseError(throwable: Throwable): String {
        return when {
            throwable.message?.contains("email address is already in use") == true ->
                "Email đã được sử dụng"
            throwable.message?.contains("password is invalid") == true ||
            throwable.message?.contains("no user record") == true ->
                "Email hoặc mật khẩu không đúng"
            throwable.message?.contains("network") == true ->
                "Lỗi kết nối mạng. Vui lòng thử lại"
            else -> throwable.message ?: "Đã xảy ra lỗi. Vui lòng thử lại"
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ResetPasswordState {
    data object Loading : ResetPasswordState()
    data object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}
