package com.crow.base.ui.view.event

data class BaseEventEntity<T>(
    val mType: T,
    val mBaseEvent: BaseEvent,
)
