package com.velimir_gurguriev.dentalclinic.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val accountType: String = "",
    val createdAt: Long = System.currentTimeMillis()
)