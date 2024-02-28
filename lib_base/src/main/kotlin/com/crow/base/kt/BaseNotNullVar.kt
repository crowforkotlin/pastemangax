package com.crow.base.kt

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.base.kt
 * @Time: 2023/10/29 15:24
 * @Author: CrowForKotlin
 * @Description: BaseNotNullVar
 * @formatter:on
 **************************/

class BaseNotNullVar<T : Any>(
    val mInitializeOnce: Boolean = false,
    val setVal: (() -> Unit)? = null,
    val getVal: (() -> Unit)? = null
) : ReadWriteProperty<Any, T> {

    private var value: T? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        getVal?.invoke()
        return value ?: error("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (mInitializeOnce) {
            if (this.value == null) {
                this.value = value
                setVal?.invoke()
            }
        } else {
            this.value = value
            setVal?.invoke()
        }
    }
}