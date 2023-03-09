@file:Suppress("unused")

package com.crow.base.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/11/30 11:12
 * @Author: CrowForKotlin
 * @Description: LiveDataExt
 * @formatter:on
 **************************/

fun interface ILiveDataEvent<T> {
    suspend fun doOnCoroutineScope(value: T, coroutineScope: CoroutineScope)
}

fun<T> LiveData<T>.doOnCoroutineObserver(lifecycleOwner: LifecycleOwner, iLiveDataEvent: ILiveDataEvent<T>) {
    observe(lifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            iLiveDataEvent.doOnCoroutineScope(it, this)
        }
    }
}

suspend fun<T> MutableLiveData<T>.setState(state: T) = if (currentCoroutineContext() == Dispatchers.Main) value = state else postValue(state)
