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

class BaseNotNullVar<T : Any>(val mInitializeOnce: Boolean = false) :
    ReadWriteProperty<Any, T> {

    private var value: T? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value ?: error("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (mInitializeOnce) {
            if (this.value == null) this.value = value
        } else {
            this.value = value
        }
    }
}