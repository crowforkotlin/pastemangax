package com.crow.base.tools.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/11/29 10:30
 * @Author: CrowForKotlin
 * @Description: FragmentExt
 * @formatter:on
 **************************/
fun interface LifecycleCallBack {
    suspend fun onLifeCycle(scope: CoroutineScope)
}

fun Fragment.repeatOnLifecycle(
    state: Lifecycle.State = Lifecycle.State.CREATED,
    lifecycleCallBack: LifecycleCallBack,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state) {
            lifecycleCallBack.onLifeCycle(
                this
            )
        }
    }
}

inline fun Fragment.doAfterDelay(
    delayMs: Long,
    crossinline block: suspend CoroutineScope.(Fragment) -> Unit,
) {
    lifecycleScope.launch {
        delay(delayMs)
        block(this@doAfterDelay)
    }
}

suspend inline fun <T> T.doAfterDelay(delayMs: Long, crossinline block: suspend T.() -> Unit) {
    delay(delayMs)
    block()
}

fun Fragment.navigate(
    @IdRes idRes: Int, bundle: Bundle? = null,
    navOptions: NavOptions = NavOptions.Builder()
        .setEnterAnim(android.R.anim.fade_in)
        .setExitAnim(android.R.anim.fade_out)
        .setPopEnterAnim(android.R.anim.fade_in)
        .setPopExitAnim(android.R.anim.fade_out)
        .build(),
) {
    findNavController().navigate(idRes, bundle, navOptions)
}

