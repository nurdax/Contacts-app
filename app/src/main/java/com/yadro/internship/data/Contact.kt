package com.yadro.internship.data

data class Contact(
    val id: String,
    val name: String,
    val phone: String?,
    val phoneType: String,
    val photoUri: String?
)