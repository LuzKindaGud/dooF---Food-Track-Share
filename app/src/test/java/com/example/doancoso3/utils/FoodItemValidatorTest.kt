package com.example.doancoso3.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll

class FoodItemValidatorTest : FunSpec({

    test("validateName should accept names between 1 and 100 characters") {
        forAll(Arb.string(1..100)) { name ->
            FoodItemValidator.validateName(name) is ValidationResult.Success
        }
    }

    test("validateName should reject empty names") {
        FoodItemValidator.validateName("") shouldBe ValidationResult.Error("Tên sản phẩm không được để trống")
    }

    test("validateName should reject names longer than 100 characters") {
        forAll(Arb.string(101..200)) { name ->
            val result = FoodItemValidator.validateName(name)
            result is ValidationResult.Error && (result as ValidationResult.Error).message == "Tên sản phẩm không được quá 100 ký tự"
        }
    }

    test("validateQuantity should accept quantities between 0.01 and 9999") {
        forAll(Arb.double(0.01..9999.0)) { quantity ->
            FoodItemValidator.validateQuantity(quantity) is ValidationResult.Success
        }
    }

    test("validateQuantity should reject quantities less than 0.01") {
        forAll(Arb.double(0.0..0.009)) { quantity ->
            FoodItemValidator.validateQuantity(quantity) is ValidationResult.Error
        }
    }

    test("validateExpiryDate should accept positive timestamps") {
        forAll(Arb.long(1..Long.MAX_VALUE)) { timestamp ->
            FoodItemValidator.validateExpiryDate(timestamp) is ValidationResult.Success
        }
    }

    test("validateExpiryDate should reject null or non-positive timestamps") {
        FoodItemValidator.validateExpiryDate(null) shouldBe ValidationResult.Error("Ngày hết hạn không hợp lệ")
        FoodItemValidator.validateExpiryDate(0) shouldBe ValidationResult.Error("Ngày hết hạn không hợp lệ")
        FoodItemValidator.validateExpiryDate(-1) shouldBe ValidationResult.Error("Ngày hết hạn không hợp lệ")
    }
})
