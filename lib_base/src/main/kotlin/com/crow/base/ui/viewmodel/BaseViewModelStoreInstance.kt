package com.crow.base.ui.viewmodel

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.crow.base.tools.coroutine.createCoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


/**
 * @Description 全局ViewModelStore实例
 * @author lei , edit by crowforkotlin
 * @date 2023-09-05 23:55:31 周二 下午
 */
object BaseViewModelStore: ViewModelStoreOwner {

    /**
     * ⦁ 懒加载 ViewModelStore
     *
     * ⦁ 2023-09-05 23:59:43 周二 下午
     */
    private val mAppViewModelStore: ViewModelStore by lazy { ViewModelStore() }

    /**
     * ⦁ ViewModelStore Lazy Coroutine
     *
     * ⦁ 2023-09-06 00:00:06 周三 上午
     */
    val mIoScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob() + createCoroutineExceptionHandler("Base Application ViewModelStore")) }

    /**
     * ⦁ 获取ViewModelStore
     *
     * ⦁ 2023-09-06 00:00:24 周三 上午
     */
    override val viewModelStore: ViewModelStore get() = mAppViewModelStore

}