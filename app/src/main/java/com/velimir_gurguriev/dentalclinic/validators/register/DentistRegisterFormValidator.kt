package com.velimir_gurguriev.dentalclinic.validators.register

import com.velimir_gurguriev.dentalclinic.models.forms.DentistRegisterForm
import com.velimir_gurguriev.dentalclinic.utils.ValidationUtils
import com.velimir_gurguriev.dentalclinic.validators.ValidationResult

class DentistRegisterFormValidator {

    fun validate(
        form: DentistRegisterForm
    ): ValidationResult {
        if (
            ValidationUtils.isBlank(form.name) ||
            ValidationUtils.isBlank(form.email) ||
            ValidationUtils.isBlank(form.password) ||
            ValidationUtils.isBlank(form.phone) ||
            ValidationUtils.isBlank(form.city) ||
            ValidationUtils.isBlank(form.specialization) ||
            ValidationUtils.isBlank(form.clinicName) ||
            ValidationUtils.isBlank(form.clinicAddress)
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

        return ValidationResult(
            isValid = true
        )
    }
}