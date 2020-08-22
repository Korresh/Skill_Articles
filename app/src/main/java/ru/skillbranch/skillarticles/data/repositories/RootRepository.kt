package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager

object RootRepository {
    val preferences = PrefManager
    fun isAuth() : LiveData<Boolean> = preferences.isAuth()
    fun setAuth(auth:Boolean) = preferences.setAuth(auth)
}