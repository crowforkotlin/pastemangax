package com.crow.base.ui.view.event.click

import com.crow.base.ui.view.event.BaseEventEntity

// 点击事件接口
fun interface BaseIEventInterval<T> { fun onIntervalOk(baseEventEntity: BaseEventEntity<T>) }
