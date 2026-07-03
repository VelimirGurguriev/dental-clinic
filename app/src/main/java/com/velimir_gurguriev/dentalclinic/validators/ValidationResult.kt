package com.velimir_gurguriev.dentalclinic.validators

data class ValidationResult(
    val isValid: Boolean,
    val message: String = ""
)
