package com.crow.base.ui.view.event

import com.crow.base.tools.extensions.toast
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import kotlin.math.absoluteValue


/**
 * @author : crowforkotlin
 *
 * ⦁ 事件基类
 *
 * ⦁ 2023-09-10 23:11:17 周日 下午
 */
open class BaseEvent private constructor(val mFlagTime: Long) {

    private var mFlagMap: MutableMap<String, Boolean>? = null
    private var mInitOnce: Boolean = false
    private var mLastClickGapTime: Long = 0L
    private var mCurrentTime: Long = 0L
    var mCurrentFlagTime = mFlagTime
        internal set

    companion object {

        const val BASE_FLAG_TIME_400 = 400L
        const val BASE_FLAG_TIME_300 = 300L
        const val BASE_FLAG_TIME_1000 = 1000L

        private var mBaseEvent: BaseEvent? = null

        fun newInstance(initFlagTime: Long = BASE_FLAG_TIME_300): BaseEvent {
            return BaseEvent(initFlagTime)
        }

        fun getSIngleInstance(): BaseEvent {
            if (mBaseEvent == null) {
                synchronized(this) {
                    if (mBaseEvent == null) {
                        mBaseEvent = BaseEvent(BASE_FLAG_TIME_300)
                    }
                }
            }
            return mBaseEvent!!
        }
    }


    /**
     * ⦁ 处理事件间隔实现
     *
     * ⦁ 2023-09-10 23:09:46 周日 下午
     */
    internal fun <T> getIntervalResult(
        type: T,
        msg: String? = null,
        baseEvent: BaseEvent
    ): BaseEventEntity<T>? {
        baseEvent.mCurrentTime = System.currentTimeMillis()
        return if (baseEvent.mCurrentTime - baseEvent.mLastClickGapTime > mCurrentFlagTime) {
            baseEvent.mLastClickGapTime = baseEvent.mCurrentTime
            BaseEventEntity(type, baseEvent)
        } else {
            if (msg != null) toast(msg)
            null
        }
    }

    /**
     * ⦁ 处理事件间隔
     *
     * ⦁ 2023-09-10 23:10:36 周日 下午
     */
    internal fun <T> doOnIntervalResult(
        type: T,
        baseEvent: BaseEvent,
        iEven: BaseIEventIntervalExt<T>
    ) : Boolean {
        val result = getIntervalResult(type, null, baseEvent)
        return if (result != null) {
            iEven.onIntervalOk(result)
            true
        } else {
            iEven.onIntervalFailure(getGapTime())
            false
        }
    }

    internal fun getGapTime() = (mCurrentFlagTime - (mCurrentTime - mLastClickGapTime)).absoluteValue

    // 事件限制仅一次初始化
    fun eventInitLimitOnce(runnable: Runnable): Unit? {
        if (!mInitOnce) {
            mInitOnce = true
            runnable.run()
            return Unit
        }
        return null
    }

    private fun initFlagMap() {
        if (mFlagMap == null) mFlagMap = mutableMapOf()
    }

    // 事件限制仅一次初始化
    fun eventInitLimitOnceByTag(tag: String, runnable: Runnable) {
        initFlagMap()
        val value = mFlagMap!![tag]
        if (value == null || !value) {
            mFlagMap!![tag] = true
            runnable.run()
        }
    }

    fun setBoolean(tag: String, defaultValue: Boolean = false) {
        initFlagMap()
        mFlagMap!![tag] = defaultValue
    }

    fun getBoolean(tag: String): Boolean? {
        initFlagMap()
        return mFlagMap!![tag]
    }

    fun remove(tag: String) {
        mFlagMap?.remove(tag)
    }

    fun remove(vararg tag: String) {
        if (mFlagMap == null) return
        tag.forEach { mFlagMap!!.remove(it) }
    }

    fun clear() {
        mFlagMap?.clear()
    }

}