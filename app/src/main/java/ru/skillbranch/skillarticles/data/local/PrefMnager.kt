package ru.skillbranch.skillarticles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class PrefManager(context: Context) {
    val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    fun clearAll() = preferences.all.clear()

}