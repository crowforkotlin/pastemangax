package com.crow.base.extensions

import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/7/10 2:23
 * @Author: BarryAllen
 * @Description: Event Extension
 * @formatter:on
 **************************/

/* 点击事件接口 */
fun interface IViewClick {
    fun onClick(view: View?, event: EventExt)
}

fun interface IMenuClick {
    fun onClick(menuItem: MenuItem, event: EventExt)
}

interface IViewClickExt : IViewClick {
    fun onFilure(event: EventClick, gapTime: Long)
}

interface IMenuClickExt : IMenuClick {
    fun onFilure(event: EventClick)
}

enum class EventClick {
    GapTimeError, CountLimitError
}

/* 事件基类 */
open class EventExt internal constructor() {

    var FLAG_INIT_ONCE: Boolean = false
    var FLAG_Failure = false
    var count = 0
}

/* 通用事件回调间隔类 扩展 存放标志位 与 数据 */
class EventGapTime : EventExt() {

    var backGapTime: Long = 0L
    var clickGapTime = 0L
    var currentTime: Long = 0L

}

/* 事件限制仅一次初始化 */
inline fun <T> EventExt.eventInitLimitOnce(block: () -> T) {
    if (!FLAG_INIT_ONCE) {
        FLAG_INIT_ONCE = true
        block()
    }
}

/* View 点击事件间隔 默认1秒 */
fun View.clickGap(flagTime: Long = 1_000L, msg: String? = null, iClick: IViewClick) {
    val eventGapTime = EventGapTime()
    setOnClickListener {
        eventGapTime.currentTime = System.currentTimeMillis()
        if (eventGapTime.currentTime - flagTime > eventGapTime.clickGapTime) {
            eventGapTime.clickGapTime = eventGapTime.currentTime
            iClick.onClick(this, eventGapTime)
        } else {
            if (msg != null) toast(msg)
        }
    }
}

/* 扩展 onFailure */
fun View.clickGap(flagTime: Long = 1_000L, iClick: IViewClickExt) {
    val eventGapTime = EventGapTime()
    setOnClickListener {
        eventGapTime.currentTime = System.currentTimeMillis()
        val gapTime = eventGapTime.currentTime - flagTime
        if (gapTime > eventGapTime.clickGapTime) {
            eventGapTime.clickGapTime = eventGapTime.currentTime
            iClick.onClick(this, eventGapTime)
        } else {
            iClick.onFilure(
                EventClick.GapTimeError,
                (flagTime / 1000) - ((eventGapTime.clickGapTime - eventGapTime.currentTime).absoluteValue / 1000)
            )
        }
    }
}

/* MenuItem 点击事件间隔 默认1秒 */
fun MenuItem.clickGap(flagTime: Long = 1_000L, msg: String? = null, iClick: IMenuClick) {
    val eventGapTime = EventGapTime()
    setOnMenuItemClickListener {
        eventGapTime.currentTime = System.currentTimeMillis()
        if (eventGapTime.currentTime - flagTime > eventGapTime.clickGapTime) {
            eventGapTime.clickGapTime = eventGapTime.currentTime
            iClick.onClick(this, eventGapTime)
        } else {
            if (msg != null) toast(msg)
        }
        true
    }
}

/* 扩展 onFailure */
fun MenuItem.clickGap(flagTime: Long = 1_000L, iClick: IMenuClickExt) {
    val eventGapTime = EventGapTime()
    setOnMenuItemClickListener {
        eventGapTime.currentTime = System.currentTimeMillis()
        if (eventGapTime.currentTime - flagTime > eventGapTime.clickGapTime) {
            eventGapTime.clickGapTime = eventGapTime.currentTime
            iClick.onClick(this, eventGapTime)
        } else {
            iClick.onFilure(EventClick.GapTimeError)
        }
        true
    }
}

/* 通用事件回调间隔 默认1秒，需手动创建EventGapTimeExt对象 */
fun EventGapTime.callbackGap(flagTime: Long = 1_000L, msg: String? = null, iClick: IViewClick) {
    currentTime = System.currentTimeMillis()
    if (currentTime - flagTime > backGapTime) {
        backGapTime = currentTime
        iClick.onClick(null, this)
    } else {
        if (msg != null) toast(msg)
    }
}

/* 扩展 onFailure */
fun EventGapTime.callbackGap(flagTime: Long = 1_000L, iClick: IViewClickExt) {
    currentTime = System.currentTimeMillis()
    val gapTime = currentTime - flagTime
    if (gapTime > backGapTime) {
        backGapTime = currentTime
        iClick.onClick(null, this)
    } else {
        iClick.onFilure(
            EventClick.GapTimeError,
            (flagTime / 1000) - ((clickGapTime - currentTime).absoluteValue / 1000)
        )
    }
}

/* 通用事件回调间隔 默认1秒，需手动创建EventGapTimeExt对象 */
inline fun EventGapTime.callbackGap(
    flagTime: Long = 1_000L,
    msg: String? = null,
    successBlock: (eventExt: EventGapTime) -> Unit,
) {
    currentTime = System.currentTimeMillis()
    if (currentTime - flagTime > backGapTime) {
        backGapTime = currentTime
        successBlock(this)
    } else {
        if (msg != null) toast(msg)
    }
}

suspend inline fun SwipeRefreshLayout.setAutoCancelRefreshing(
    cancelTime: Long = 5_000L,
    crossinline block: () -> Unit,
) {
    delay(cancelTime)
    if (isRefreshing) {
        isRefreshing = false
        block()
    }
}

inline fun SwipeRefreshLayout.doOnCoroutineRefresh(
    delayMs: Long = 0,
    lifecycleOwner: LifecycleOwner,
    crossinline block: suspend CoroutineScope.() -> Unit,
) {
    setOnRefreshListener {
        lifecycleOwner.lifecycleScope.launch {
            block(this)
            if (delayMs != 0L) delay(delayMs)
            isRefreshing = false
        }
    }
}

inline fun SwipeRefreshLayout.doOnCoroutineRefreshCancel(
    delayMs: Long = 0,
    lifecycleOwner: LifecycleOwner,
    crossinline block: suspend CoroutineScope.() -> Unit,
) {
    lifecycleOwner.lifecycleScope.launch {
        isRefreshing = true
        block(this)
        if (delayMs != 0L) delay(delayMs)
        isRefreshing = false
    }
}