package com.example.doancoso3.utils

/**
 * Validates authentication inputs (email and password).
 */
object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * Validates email format.
     * Must contain one '@', non-empty local part, domain with at least one dot.
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email không được để trống")
            !EMAIL_REGEX.matches(email) -> ValidationResult.Error("Email không hợp lệ")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates password.
     * Must be 6-128 characters.
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Mật khẩu không được để trống")
            password.length < 6 -> ValidationResult.Error("Mật khẩu phải có ít nhất 6 ký tự")
            password.length > 128 -> ValidationResult.Error("Mật khẩu không được quá 128 ký tự")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates confirm password matches password.
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error("Xác nhận mật khẩu không được để trống")
            confirmPassword != password -> ValidationResult.Error("Mật khẩu xác nhận không khớp")
            else -> ValidationResult.Success
        }
    }
}
