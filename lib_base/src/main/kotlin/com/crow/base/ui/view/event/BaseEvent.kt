package com.crow.base.ui.view.event

import com.crow.base.tools.extensions.toast
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import kotlin.math.absoluteValue


// 事件基类
open class BaseEvent {

    private constructor(flagTime: Long) { mCurrentFlagTime = flagTime }

    private var mInitOnce: Boolean = false
    private var mLastClickGapTime: Long = 0L
    private var mCurrentTime: Long = 0L
    var mCurrentFlagTime = 0L
        private set

    companion object {

        const val BASE_FLAG_TIME = 500L

        private var mBaseEvent: BaseEvent? = null

        fun newInstance(flagTime: Long = BASE_FLAG_TIME) = BaseEvent(flagTime)

        fun getSIngleInstance(flagTime: Long = BASE_FLAG_TIME) : BaseEvent {
            if (mBaseEvent == null) mBaseEvent = BaseEvent(flagTime)
            return mBaseEvent!!
        }
    }

    internal fun<T> getIntervalResult(type: T, msg: String? = null, baseEvent: BaseEvent): BaseEventEntity<T>? {
        baseEvent.mCurrentTime = System.currentTimeMillis()
        return if (baseEvent.mCurrentTime - baseEvent.mLastClickGapTime > mCurrentFlagTime) {
            baseEvent.mLastClickGapTime = baseEvent.mCurrentTime
            BaseEventEntity(type, baseEvent)
        } else {
            if (msg != null) toast(msg)
            null
        }
    }

    internal fun<T> doOnIntervalResult(type: T, baseEvent: BaseEvent, iEven: BaseIEventIntervalExt<T>) {
        val result = getIntervalResult(type, null, baseEvent)
        if (result != null) iEven.onIntervalOk(result) else iEven.onIntervalFailure(getGapTime())

    }

    internal fun getGapTime() = (mCurrentFlagTime - (mCurrentTime - mLastClickGapTime)).absoluteValue

    // 事件限制仅一次初始化
    fun <T> eventInitLimitOnce(block: () -> T) {
        if (!mInitOnce) {
            mInitOnce = true
            block()
        }
    }
}