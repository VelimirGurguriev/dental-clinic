package com.velimir_gurguriev.dentalclinic.validators.register

import com.velimir_gurguriev.dentalclinic.models.forms.RegisterForm
import com.velimir_gurguriev.dentalclinic.utils.ValidationUtils
import com.velimir_gurguriev.dentalclinic.validators.ValidationResult

class RegisterFormValidator {

    fun validate(form: RegisterForm): ValidationResult {
        if (
            ValidationUtils.isBlank(form.name) ||
            ValidationUtils.isBlank(form.email) ||
            ValidationUtils.isBlank(form.password)
        ) {
            return ValidationResult(
                isValid = false,
                message = "Моля, попълнете всички полета."
            )
        }

        if (!ValidationUtils.isValidEmail(form.email)) {
            return ValidationResult(
                isValid = false,
                message = "Моля, въведете валиден имейл."
            )
        }

        if (!ValidationUtils.isValidPassword(form.password)) {
            return ValidationResult(
                isValid = false,
                message = "Паролата трябва да е поне 6 символа."
            )
        }

        if (form.accountType.isEmpty()) {
            return ValidationResult(
                isValid = false,
                message = "Моля, изберете тип акаунт."
            )
        }

        return ValidationResult(isValid = true)
    }
}
