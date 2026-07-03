package com.velimir_gurguriev.dentalclinic.validators.login

import com.velimir_gurguriev.dentalclinic.models.forms.LoginForm
import com.velimir_gurguriev.dentalclinic.utils.ValidationUtils
import com.velimir_gurguriev.dentalclinic.validators.ValidationResult

class LoginFormValidator {

    fun validate(form: LoginForm): ValidationResult {
        if (ValidationUtils.isBlank(form.email) || ValidationUtils.isBlank(form.password)) {
            return ValidationResult(
                isValid = false,
                message = "Моля, попълнете имейл и парола."
            )
        }

        if (!ValidationUtils.isValidEmail(form.email)) {
            return ValidationResult(
                isValid = false,
                message = "Моля, въведете валиден имейл."
            )
        }

        return ValidationResult(isValid = true)
    }
}
