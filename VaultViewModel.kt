package com.securevault.security

import de.mkammerer.argon2.Argon2Factory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Argon2Helper @Inject constructor() {

    companion object {
        private const val MEMORY_COST = 65536
        private const val ITERATIONS = 3
        private const val PARALLELISM = 1
    }

    private val argon2 = Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2id)

    fun hashPassword(password: CharArray): String {
        return try {
            argon2.hash(ITERATIONS, MEMORY_COST, PARALLELISM, password)
        } finally {
            argon2.wipeArray(password)
        }
    }

    fun verifyPassword(hash: String, password: CharArray): Boolean {
        return try {
            argon2.verify(hash, password)
        } finally {
            argon2.wipeArray(password)
        }
    }
}
