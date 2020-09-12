package ru.skillbranch.skillarticles.data.local

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.JsonConverter.moshi
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveObjDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefObjDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings
import ru.skillbranch.skillarticles.data.models.User



@SuppressLint("RestrictedApi")
object PrefManager {

    internal val preferences : SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }
    var isDarkMode by PrefDelegate(false)
    var isBigText by PrefDelegate(false)
    var accessToken by PrefDelegate("")
    var refreshToken by PrefDelegate("")
    var profile: User? by PrefObjDelegate(moshi.adapter(User::class.java))

    val isAuthLive: LiveData<Boolean> by lazy {
        val token by PrefLiveDelegate("accessToken", "", preferences)
        token.map{it.isNotEmpty()}
    }
    val profileLive: LiveData<User?> by PrefLiveObjDelegate("profile", moshi.adapter(User::class.java), preferences)

    fun getAppSettings() = MediatorLiveData<AppSettings>().apply{
        val isDarkModeLive: LiveData<Boolean> by PrefLiveDelegate("isDarkMode",false, preferences)
        val isBigTextLive: LiveData<Boolean> by PrefLiveDelegate("isBigText",false, preferences)
        value = AppSettings()

        addSource(isDarkModeLive){
            value = value!!.copy(isDarkMode = it)
        }
        addSource(isBigTextLive){
            value = value!!.copy(isBigText = it)
        }

    }.distinctUntilChanged()

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    fun replaceAvatarUrl(url: String) {
        profile = profile!!.copy(avatar = url)
    }
}