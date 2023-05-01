package com.crow.base.ui.view.event


// 事件基类
open class BaseEvent private constructor() {

    companion object {

        const val BASE_FLAG_TIME = 500L

        private var mBaseEvent: BaseEvent? = null

        fun getSIngleInstance() : BaseEvent {
            if (mBaseEvent == null) mBaseEvent = BaseEvent()
            return mBaseEvent!!
        }

        fun newInstance() = BaseEvent()
    }

    var mInitOnce: Boolean = false
    var mCount = 0
    var mLastClickGapTime: Long = 0L
    var mBackGapTime: Long = 0L
    var mCurrentTime: Long = 0L
}