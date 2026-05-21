package com.example.doancoso3.utils

/**
 * Validates food item inputs.
 */
object FoodItemValidator {

    /**
     * Validates food item name.
     * Must be 1-100 characters.
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Tên sản phẩm không được để trống")
            name.length > 100 -> ValidationResult.Error("Tên sản phẩm không được quá 100 ký tự")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates food item quantity.
     * Must be 0.01-9999.
     */
    fun validateQuantity(quantity: Double): ValidationResult {
        return when {
            quantity < 0.01 -> ValidationResult.Error("Số lượng phải ít nhất là 0.01")
            quantity > 9999.0 -> ValidationResult.Error("Số lượng không được quá 9999")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates food item expiry date.
     * Must be present and positive.
     */
    fun validateExpiryDate(expiryDate: Long?): ValidationResult {
        return when {
            expiryDate == null || expiryDate <= 0 -> ValidationResult.Error("Ngày hết hạn không hợp lệ")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates food item image size.
     * Must be <= 5MB.
     */
    fun validateImageSize(sizeInBytes: Long): ValidationResult {
        val maxSizeBytes = 5 * 1024 * 1024 // 5MB
        return if (sizeInBytes > maxSizeBytes) {
            ValidationResult.Error("Ảnh không được quá 5MB")
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Composite validation for a food item.
     */
    fun validateFoodItem(
        name: String,
        quantity: Double,
        expiryDate: Long,
        imageSizeInBytes: Long = 0
    ): Map<String, ValidationResult> {
        return mapOf(
            "name" to validateName(name),
            "quantity" to validateQuantity(quantity),
            "expiryDate" to validateExpiryDate(expiryDate),
            "image" to validateImageSize(imageSizeInBytes)
        )
    }

    /**
     * Checks if all validation results are successful.
     */
    fun isValid(results: Map<String, ValidationResult>): Boolean {
        return results.values.all { it is ValidationResult.Success }
    }
}
