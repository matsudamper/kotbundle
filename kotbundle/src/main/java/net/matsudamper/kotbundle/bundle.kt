package net.matsudamper.kotbundle

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.io.Serializable
import kotlin.reflect.KProperty

inline fun <T> Activity.bundle(
    name: String? = null,
    default: T? = null
) = ReadWriteKotBundle(
    name = name,
    default = default,
    getBundle = { intent.extras },
    activity = this
)

inline fun <T> Fragment.bundle(
    name: String? = null,
    default: T? = null
) = ReadWriteKotBundle(
    name = name,
    default = default,
    getBundle = { arguments },
    fragment = this
)

fun <T> bundle(
    name: String? = null,
    default: T? = null,
    getBundle: () -> Bundle?
) = ReadOnlyKotBundle<T>(name, default, getBundle)

open class ReadOnlyKotBundle<T>(
    val name: String?,
    default: T? = null,
    val getBundle: () -> Bundle?
) : Serializable {

    protected var value_: Any? = default

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {

        val bundle = getBundle()
        val key = name ?: property.name
        value_ = if (bundle?.containsKey(key) == true) bundle.get(key) else value_

        return value_ as T
    }
}

class ReadWriteKotBundle<T>(
    name: String?,
    default: T? = null,
    getBundle: () -> Bundle?,
    val activity: Activity? = null,
    val fragment: Fragment? = null
) : ReadOnlyKotBundle<T>(name, default, getBundle) {
    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        value_ = value

        if (activity != null) {
            activity.intent.putExtras(Bundle().apply {
                putSerializable(property.name, value as? Serializable)
            })
        } else if (fragment != null) {
            val argument = fragment.arguments
            if (argument == null) {
                fragment.arguments = Bundle().apply {
                    putSerializable(property.name, value as? Serializable)
                    putAll(fragment.arguments ?: return@apply)
                }
            } else {
                argument.putAll(Bundle().apply {
                    putSerializable(property.name, value as? Serializable)
                })
            }
        } else {
            throw IllegalStateException("Cannot set to Bundle.")
        }
    }
}