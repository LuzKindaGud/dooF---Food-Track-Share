package com.example.doancoso3.utils

object FamilyGroupValidator {

    const val MAX_MEMBERS = 10

    fun canAddMember(currentMemberCount: Int): Boolean {
        return currentMemberCount < MAX_MEMBERS
    }

    fun canJoinGroup(currentFamilyId: String?): Boolean {
        return currentFamilyId.isNullOrBlank()
    }

    fun isFamilyIdFormatValid(familyId: String): Boolean {
        return familyId.length == 6 && familyId.all { it.isLetterOrDigit() }
    }
}
