package com.example.childcare

import org.junit.Assert.*
import org.junit.Test

class InputValidationTest {

    private fun validarEntradas(email: String, password: String): Boolean {
        val patronEmail = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        if (!email.matches(patronEmail)) return false
        if (password.length < 6) return false

        // Usamos el valor Unicode del signo $ para evitar el error
        val blacklist = Regex(
            "[\\{\\}\\[\\]\\u0024]|\"\\s*or\\s*\"|\\u0024ne",
            RegexOption.IGNORE_CASE
        )

        if (blacklist.containsMatchIn(email) || blacklist.containsMatchIn(password)) {
            return false
        }

        return true
    }

    @Test
    fun entradasMaliciosas_sonRechazadas() {
        val maliciosas = listOf(
            "\" OR \"\"=\"",
            "{ \"\$ne\": null }", // aquí no hay problema
            "' OR 1=1 --",
            "\"; drop table users; --"
        )

        maliciosas.forEach {
            assertFalse("Debería rechazar: $it", validarEntradas(it, it))
        }
    }

    @Test
    fun entradasValidas_sonAceptadas() {
        assertTrue(validarEntradas("usuario@example.com", "clave123"))
    }
}