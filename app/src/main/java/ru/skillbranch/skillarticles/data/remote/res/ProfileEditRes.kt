package ru.skillbranch.skillarticles.data.remote.res

import java.util.*

data class ProfileEditRes(
    val id: String,
    val name: String,
    val avatar: String,
    val about: String,
    val rating: Int,
    val respect: Int,
    val updatedAt: Date = Date()
)