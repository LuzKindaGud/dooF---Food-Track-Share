package com.example.doancoso3.utils

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll

/**
 * Property-based tests for Family Group rules.
 *
 * Feature: food-share-track, Property 2 & 3
 *
 * Validates: Requirements 2.1, 2.7, 2.8
 */
@OptIn(ExperimentalKotest::class)
class FamilyGroupPropertyTest : FunSpec({

    val generatedIdArb = arbitrary { FamilyGroupIdGenerator.generate() }

    test("family ID generation should produce 6-char alphanumeric IDs") {
        // Feature: food-share-track, Property 2: Family ID Generation Format
        forAll(PropTestConfig(iterations = 120), generatedIdArb) { id ->
            id.length == 6 && id.all { it.isLetterOrDigit() }
        }
    }

    test("group membership constraints should reject the 11th member") {
        // Feature: food-share-track, Property 3: Group Membership Constraints
        forAll(PropTestConfig(iterations = 120), Arb.int(0..15)) { count ->
            FamilyGroupValidator.canAddMember(count) == (count < FamilyGroupValidator.MAX_MEMBERS)
        }
    }

    test("users already in a group cannot join another group") {
        // Feature: food-share-track, Property 3: Group Membership Constraints
        forAll(PropTestConfig(iterations = 120), Arb.string(0..8)) { familyId ->
            val currentFamilyId = familyId.ifBlank { null }
            FamilyGroupValidator.canJoinGroup(currentFamilyId) == currentFamilyId.isNullOrBlank()
        }
    }
})
