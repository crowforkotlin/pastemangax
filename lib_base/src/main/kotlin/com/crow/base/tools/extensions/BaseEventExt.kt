@file:Suppress("unused")

package com.crow.base.tools.extensions

import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEvent.Companion.BASE_FLAG_TIME
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventInterval
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/7/10 2:23
 * @Author: CrowForKotlin
 * @Description: Event Extension
 * @formatter:off
 **************************/


private fun<T> T.getIntervalResult(flagTime: Long, msg: String? = null, baseEvent: BaseEvent): BaseEventEntity<T>? {
    baseEvent.mCurrentTime = System.currentTimeMillis()
    val gapTime = baseEvent.mCurrentTime - flagTime
    return if (gapTime > baseEvent.mLastClickGapTime) {
        baseEvent.mLastClickGapTime = baseEvent.mCurrentTime
        BaseEventEntity(this, baseEvent)
    } else {
        if (msg != null) toast(msg)
        null
    }
}

private fun<T> T.doOnIntervalResult(flagTime: Long, baseEvent: BaseEvent, iEven: BaseIEventIntervalExt<T>) {
    val result = getIntervalResult(flagTime, null, baseEvent)
    if (result != null) {
        iEven.onIntervalOk(result)
    } else {
        iEven.onIntervalFailure((flagTime / 1000) - ((baseEvent.mLastClickGapTime - baseEvent.mCurrentTime).absoluteValue / 1000))
    }
}

// 事件限制仅一次初始化
inline fun <T> BaseEvent.eventInitLimitOnce(block: () -> T) {
    if (!mInitOnce) {
        mInitOnce = true
        block()
    }
}

// View 点击事件间隔 默认1秒
fun View.doOnClickInterval(isGlobal:Boolean = true, flagTime: Long = BASE_FLAG_TIME, msg: String? = null, iEven: BaseIEventInterval<View>) {
    val baseEvent = if (isGlobal) BaseEvent.getSIngleInstance() else BaseEvent.newInstance()
    setOnClickListener { iEven.onIntervalOk(getIntervalResult(flagTime, msg, baseEvent) ?: return@setOnClickListener) }
}

// View 扩展 onFailure
fun View.doOnClickInterval(isGlobal:Boolean = true, flagTime: Long = BASE_FLAG_TIME, iEven: BaseIEventIntervalExt<View>) {
    val baseEvent = if (isGlobal) BaseEvent.getSIngleInstance() else BaseEvent.newInstance()
    setOnClickListener { doOnIntervalResult(flagTime, baseEvent, iEven) }
}

// MenuItem 点击事件间隔 默认1秒
fun MenuItem.doOnClickInterval(isGlobal:Boolean = true, flagTime: Long = BASE_FLAG_TIME, msg: String? = null, iEven: BaseIEventInterval<MenuItem>) {
    val baseEvent = if (isGlobal) BaseEvent.getSIngleInstance() else BaseEvent.newInstance()
    setOnMenuItemClickListener {
        iEven.onIntervalOk(getIntervalResult(flagTime, msg, baseEvent) ?: return@setOnMenuItemClickListener true)
        true
    }
}

// MenuItem 扩展 onFailure
fun MenuItem.doOnClickInterval(isGlobal:Boolean = true, flagTime: Long = BASE_FLAG_TIME, iEven: BaseIEventIntervalExt<MenuItem>) {
    val baseEvent = if (isGlobal) BaseEvent.getSIngleInstance() else BaseEvent.newInstance()
    setOnMenuItemClickListener {
        doOnIntervalResult(flagTime, baseEvent, iEven)
        true
    }
}

// MaterialToolbar 点击事件间隔 默认1秒
fun MaterialToolbar.navigateIconClickGap(isGlobal: Boolean = true, flagTime: Long = BASE_FLAG_TIME, msg: String? = null, iEven: BaseIEventInterval<MaterialToolbar>) {
    val baseEvent = if (isGlobal) BaseEvent.getSIngleInstance() else BaseEvent.newInstance()
    setNavigationOnClickListener {
        iEven.onIntervalOk(getIntervalResult(flagTime, msg, baseEvent) ?: return@setNavigationOnClickListener)
    }
}

// BaseEvent通用事件回调间隔 默认1秒，需手动创建EventGapTimeExt对象
fun BaseEvent.doOnInterval(flagTime: Long = BASE_FLAG_TIME, msg: String? = null, iEven: BaseIEventInterval<BaseEvent>) {
    iEven.onIntervalOk(getIntervalResult(flagTime, msg, this) ?: return)
}

// BaseEvent扩展 onFailure
fun BaseEvent.doOnInterval(flagTime: Long = BASE_FLAG_TIME, iEven: BaseIEventIntervalExt<BaseEvent>) {
    doOnIntervalResult(flagTime, this, iEven)
}


// BaseEvent扩展 onFailure 使用内联
inline fun SwipeRefreshLayout.setAutoCancelRefreshing(lifecycleOwner: LifecycleOwner, cancelTime: Long = 5_000L, crossinline block: () -> Unit) {
    setOnRefreshListener {
        block()
        lifecycleOwner.lifecycleScope.launch {
            delay(cancelTime)
            isRefreshing = false
        }
    }
}

// SwipeRefreshLayout 使用内联扩展刷新事件
inline fun SwipeRefreshLayout.doOnCoroutineRefreshListener(delayMs: Long = 0, lifecycleOwner: LifecycleOwner, crossinline block: suspend CoroutineScope.() -> Unit) {
    setOnRefreshListener {
        lifecycleOwner.lifecycleScope.launch {
            block(this)
            if (delayMs != 0L) delay(delayMs)
            isRefreshing = false
        }
    }
}

// SwipeRefreshLayout 使用内联扩展自动刷新
inline fun SwipeRefreshLayout.doOnCoroutineAutoRefresh(delayMs: Long = 0, lifecycleOwner: LifecycleOwner, crossinline block: suspend CoroutineScope.() -> Unit) {
    lifecycleOwner.lifecycleScope.launch {
        isRefreshing = true
        block(this)
        if (delayMs != 0L) delay(delayMs)
        isRefreshing = false
    }
}

// 用于简化对 EditText 组件设置 afterTextChanged 操作的扩展函数。
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) { afterTextChanged.invoke(editable.toString()) }
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}