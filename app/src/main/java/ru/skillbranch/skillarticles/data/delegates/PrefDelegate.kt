package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        with(thisRef.preferences) {
            return when (defaultValue) {
                is Boolean -> (getBoolean(property.name, defaultValue) as? T) ?: defaultValue
                is String -> (getString(property.name, defaultValue) as? T) ?: defaultValue
                is Float -> (getFloat(property.name, defaultValue) as? T) ?: defaultValue
                is Int -> (getInt(property.name, defaultValue) as? T) ?: defaultValue
                is Long -> (getLong(property.name, defaultValue) as? T) ?: defaultValue
                else ->  error("This type can`t support")

            }
        }
    }
    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        with(thisRef.preferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(property.name, value)
                is String -> putString(property.name, value)
                is Float -> putFloat(property.name, value)
                is Int -> putInt(property.name, value)
                is Long -> putLong(property.name, value)
                else ->  error("This type can`t support")
            }
            apply()
        }
    }
}