package com.crow.base.tools.extensions

import android.R.anim
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crow.base.R
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
    state: Lifecycle.State = Lifecycle.State.STARTED,
    lifecycleCallBack: LifecycleCallBack,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state) {
            lifecycleCallBack.onLifeCycle(this)
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
        .setEnterAnim(anim.fade_in)
        .setExitAnim(anim.fade_out)
        .setPopEnterAnim(anim.fade_in)
        .setPopExitAnim(anim.fade_out)
        .build(),
) {
    findNavController().navigate(idRes, bundle, navOptions)
}

fun Fragment.navigateUp() = findNavController().navigateUp()

fun FragmentTransaction.setFadeAnimation() =
    setCustomAnimations(anim.fade_in, anim.fade_out, anim.fade_in, anim.fade_out)

fun FragmentTransaction.setSlideAnimation() = setCustomAnimations(
    anim.slide_in_left,
    anim.slide_out_right,
    anim.slide_in_left,
    anim.slide_out_right
)

fun FragmentTransaction.setCenterAnimWithFadeOut() =
    setCustomAnimations(R.anim.base_anim_center_in, anim.fade_out, 0, R.anim.base_anim_center_out)

fun FragmentTransaction.setFadeInAnimOutWithCenter() =
    setCustomAnimations(anim.fade_in, anim.fade_out, R.anim.base_anim_center_in, R.anim.base_anim_center_out)


inline fun FragmentManager.hide(
    fragment: Fragment,
    backStackName: String?,
    crossinline transaction: (FragmentTransaction) -> FragmentTransaction = { it.setFadeAnimation() }
) = transaction(beginTransaction()).addToBackStack(backStackName).hide(fragment).commit()

inline fun FragmentManager.remove(
    fragment: Fragment,
    crossinline  transaction: (FragmentTransaction) -> FragmentTransaction = { it.setFadeAnimation() }
) = transaction(beginTransaction()).remove(fragment).commit()

inline fun FragmentManager.show(
    fragment: Fragment,
    crossinline  transaction: (FragmentTransaction) -> FragmentTransaction = { it.setFadeAnimation() }
) = transaction(beginTransaction()).show(fragment).commit()


inline fun<reified T: Fragment> FragmentManager.navigateByAddWithBackStack(
    @IdRes id: Int,
    bundle: Bundle? = null,
    backStackName: String? = null,
    tag: String? = null,
    crossinline transaction: (FragmentTransaction) -> FragmentTransaction = { it.setFadeAnimation() },
) {
    transaction(beginTransaction())
        .addToBackStack(backStackName)
        .add(id, T::class.java, bundle, tag)
        .commit()
}

inline fun<reified T: Fragment> FragmentManager.navigateToWithBackStack(
    @IdRes id: Int,
    hideTarget: Fragment,
    bundle: Bundle? = null,
    tag: String? = null,
    backStackName: String? = null,
    crossinline transaction: (FragmentTransaction) -> FragmentTransaction = { it.setCenterAnimWithFadeOut() },
) {
    transaction(beginTransaction())
        .addToBackStack(backStackName)
        .add(id, T::class.java, bundle, tag)
        .hide(hideTarget)
        .commit()
}

inline fun FragmentManager.navigateToWithBackStack(
    @IdRes id: Int,
    hideTarget: Fragment,
    addedTarget: Fragment,
    tag: String? = null,
    backStackName: String? = null,
    crossinline transaction: (FragmentTransaction) -> FragmentTransaction = { it.setCenterAnimWithFadeOut() },
) {
    transaction(beginTransaction())
        .addToBackStack(backStackName)
        .add(id, addedTarget, tag)
        .hide(hideTarget)
        .commit()
}

inline fun<reified T: Fragment> FragmentManager.navigateByAdd(
    @IdRes id: Int,
    bundle: Bundle? = null,
    tag: String? = null,
    crossinline transaction: (FragmentTransaction) -> FragmentTransaction = { it.setFadeAnimation() },
) {
    transaction(beginTransaction())
        .add(id, T::class.java, bundle, tag)
        .commit()
}



fun FragmentManager.popSyncWithClear(vararg backStackName: String?, flags: Int = FragmentManager.POP_BACK_STACK_INCLUSIVE) {
    backStackName.forEach {
        popBackStackImmediate(it, flags)
        clearBackStack(it ?: return)
    }
}

fun FragmentManager.popAsyncWithClear(vararg backStackName: String?, flags: Int = FragmentManager.POP_BACK_STACK_INCLUSIVE) {
    backStackName.forEach {
        popBackStack(it, flags)
        clearBackStack(it ?: return)
    }
}