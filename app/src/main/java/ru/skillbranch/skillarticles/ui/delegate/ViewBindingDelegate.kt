package ru.skillbranch.skillarticles.ui.delegate

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T: ViewBinding>(
    private val activity: AppCompatActivity,
    private val initializer:(LayoutInflater)->T
): ReadOnlyProperty<AppCompatActivity, T>, LifecycleObserver{
    private var value:T? = null
    init {
        activity.lifecycle.addObserver(this)
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE )
    fun onCreate(){
        //run on activity create callback called
        if (value == null){
            value = initializer(activity.layoutInflater)
        }
        activity.setContentView(value!!.root) //set main view
        activity.lifecycle.removeObserver(this) //unregister observe this
    }
    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        if (value == null){
            value = initializer(thisRef.layoutInflater)
        }
        return value!!
    }
}

inline fun <reified T: ViewBinding> AppCompatActivity.viewBinding(noinline initializer: (LayoutInflater) -> T) = ViewBindingDelegate(this,initializer)