package ru.skillbranch.skillarticles

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho

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

        //TODO set default Night Mode

        Stetho.initializeWithDefaults(this)
    }
}