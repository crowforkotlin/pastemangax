package com.crow.base.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/11/29 10:30
 * @Author: BarryAllen
 * @Description: FragmentExt
 * @formatter:on
 **************************/
inline fun Fragment.repeatOnLifecycle(state: Lifecycle.State = Lifecycle.State.STARTED, crossinline block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(state) { block() } }
}

inline fun Fragment.doAfterDelay(delayMs: Long, crossinline block: suspend CoroutineScope.(Fragment) ->Unit) {
    lifecycleScope.launch {
        delay(delayMs)
        block(this@doAfterDelay)
    }
}

suspend inline fun<T> T.doAfterDelay(delayMs: Long, crossinline block: suspend T.() ->Unit) {
    delay(delayMs)
    block()
}