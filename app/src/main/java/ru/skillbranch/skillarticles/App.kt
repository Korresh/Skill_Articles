package ru.skillbranch.skillarticles

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.stetho.Stetho
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkMonitor

class App: Application() {

    companion object{
        private var instanse : App? = null

        fun applicationContext() : Context {
            return instanse!!.applicationContext
        }
    }
    init {
        instanse = this
    }

    override fun onCreate() {
        super.onCreate()
        //start network monitoring
        NetworkMonitor.registerNetworkMonitor(applicationContext)

        //set saved night/day mode
        val mode = if (PrefManager.isDarkMode == true) AppCompatDelegate.MODE_NIGHT_YES
        else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        Stetho.initializeWithDefaults(this)
    }
}