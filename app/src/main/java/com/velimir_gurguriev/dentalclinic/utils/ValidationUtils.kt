package com.velimir_gurguriev.dentalclinic.utils

import android.util.Patterns

object ValidationUtils {

    fun isBlank(value: String): Boolean {
        return value.trim().isEmpty()
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}