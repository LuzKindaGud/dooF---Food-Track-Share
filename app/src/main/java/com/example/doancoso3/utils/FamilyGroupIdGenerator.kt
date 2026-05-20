package com.example.doancoso3.utils

import kotlin.random.Random

object FamilyGroupIdGenerator {

    private const val ID_LENGTH = 6
    private val CHAR_POOL: List<Char> = ('A'..'Z') + ('0'..'9')

    fun generate(): String {
        return (1..ID_LENGTH)
            .map { CHAR_POOL[Random.nextInt(CHAR_POOL.size)] }
            .joinToString("")
    }
}
