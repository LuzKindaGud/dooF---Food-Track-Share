package com.example.doancoso3.utils

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choose
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.of
import io.kotest.property.forAll

/**
 * Property-based tests for AuthValidator.
 *
 * Feature: food-share-track, Property 1: Auth Input Validation
 *
 * Validates: Requirements 1.3, 1.4
 */
@OptIn(ExperimentalKotest::class)
class AuthValidatorPropertyTest : FunSpec({

    // --- Custom Arb generators ---

    val localPartChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('+', '_', '.', '-')
    val domainChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('.', '-')
    val alphaChars = ('a'..'z') + ('A'..'Z')

    // Generates valid emails: localPart@domain.tld
    val validEmailArb = arbitrary {
        val localLen = Arb.int(1..20).bind()
        val domainLen = Arb.int(1..15).bind()
        val tldLen = Arb.int(2..6).bind()

        val local = (1..localLen).map { Arb.of(localPartChars).bind() }.joinToString("")
        val domain = (1..domainLen).map { Arb.of(('a'..'z') + ('A'..'Z') + ('0'..'9')).bind() }.joinToString("")
        val tld = (1..tldLen).map { Arb.of(alphaChars).bind() }.joinToString("")

        "$local@$domain.$tld"
    }

    // Generates invalid emails (various invalid patterns)
    val invalidEmailArb = arbitrary {
        val choice = Arb.int(0..4).bind()
        when (choice) {
            0 -> "" // empty string
            1 -> {
                // missing @ sign
                val local = (1..5).map { Arb.of(localPartChars).bind() }.joinToString("")
                val domain = (1..5).map { Arb.of(('a'..'z').toList()).bind() }.joinToString("")
                "$local$domain"
            }
            2 -> {
                // empty local part (starts with @)
                val domain = (1..5).map { Arb.of(('a'..'z').toList()).bind() }.joinToString("")
                val tld = (2..4).map { Arb.of(alphaChars).bind() }.joinToString("")
                "@$domain.$tld"
            }
            3 -> {
                // no dot in domain (no TLD separator)
                val local = (1..5).map { Arb.of(localPartChars).bind() }.joinToString("")
                val domain = (1..5).map { Arb.of(('a'..'z').toList()).bind() }.joinToString("")
                "$local@$domain"
            }
            else -> {
                // TLD too short (1 char only)
                val local = (1..5).map { Arb.of(localPartChars).bind() }.joinToString("")
                val domain = (1..5).map { Arb.of(('a'..'z').toList()).bind() }.joinToString("")
                val shortTld = Arb.of(alphaChars).bind()
                "$local@$domain.$shortTld"
            }
        }
    }

    val passwordChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ('!'..'/')

    // Valid password: length 6-128, non-blank
    val validPasswordArb = arbitrary {
        val len = Arb.int(6..128).bind()
        (1..len).map { Arb.of(passwordChars).bind() }.joinToString("")
    }

    // Invalid password: too short (1-5 chars) or too long (129-200 chars) or blank
    val invalidPasswordArb = arbitrary {
        val choice = Arb.int(0..2).bind()
        when (choice) {
            0 -> {
                // too short: 1-5 chars
                val len = Arb.int(1..5).bind()
                (1..len).map { Arb.of(('a'..'z').toList()).bind() }.joinToString("")
            }
            1 -> {
                // too long: 129-200 chars
                val len = Arb.int(129..200).bind()
                (1..len).map { Arb.of(('a'..'z').toList()).bind() }.joinToString("")
            }
            else -> {
                // blank string (spaces only, length 1-10)
                val len = Arb.int(1..10).bind()
                " ".repeat(len)
            }
        }
    }

    // --- Property Tests ---

    test("valid emails should be accepted by AuthValidator") {
        // Feature: food-share-track, Property 1: Auth Input Validation
        // **Validates: Requirements 1.3**
        forAll(PropTestConfig(iterations = 100), validEmailArb) { email ->
            AuthValidator.validateEmail(email) is ValidationResult.Success
        }
    }

    test("invalid emails should be rejected by AuthValidator") {
        // Feature: food-share-track, Property 1: Auth Input Validation
        // **Validates: Requirements 1.3**
        forAll(PropTestConfig(iterations = 100), invalidEmailArb) { email ->
            AuthValidator.validateEmail(email) is ValidationResult.Error
        }
    }

    test("valid passwords (6-128 chars, non-blank) should be accepted by AuthValidator") {
        // Feature: food-share-track, Property 1: Auth Input Validation
        // **Validates: Requirements 1.4**
        forAll(PropTestConfig(iterations = 100), validPasswordArb) { password ->
            AuthValidator.validatePassword(password) is ValidationResult.Success
        }
    }

    test("invalid passwords (too short, too long, or blank) should be rejected by AuthValidator") {
        // Feature: food-share-track, Property 1: Auth Input Validation
        // **Validates: Requirements 1.4**
        forAll(PropTestConfig(iterations = 100), invalidPasswordArb) { password ->
            AuthValidator.validatePassword(password) is ValidationResult.Error
        }
    }
})
