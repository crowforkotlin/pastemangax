package com.crow.base.tools.extensions

import androidx.annotation.MainThread
import com.crow.base.ui.activity.BaseActivityImpl
import com.crow.base.ui.fragment.BaseFragmentImpl
import com.crow.base.ui.viewmodel.BaseViewModel
import com.crow.base.ui.viewmodel.BaseViewModelStore
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * @Description: ViewModel Ext
 * @author lei , edit by crowforkotlin
 * @date 2023-09-05 23:55:31 周二 下午
 */

/**
 * ● 全局ViewModel For Activity
 *
 * ● 2023-09-06 00:11:05 周三 上午
 */
@MainThread
inline fun <reified VM : BaseViewModel> BaseFragmentImpl.applicationViewModels(): Lazy<Unit> {
    return lazy(LazyThreadSafetyMode.NONE) {
        getViewModel<VM>(
            qualifier = null,
            owner = { BaseViewModelStore },
            parameters = null
        )
    }
}

/**
 * ● 全局ViewModel For Activity
 *
 * ● 2023-09-06 00:11:05 周三 上午
 */
@MainThread
inline fun <reified VM : BaseViewModel> BaseActivityImpl.applicationViewModels(): Lazy<VM> {
    return lazy(LazyThreadSafetyMode.NONE) {
        getViewModel<VM>(
            qualifier = null,
            owner = BaseViewModelStore,
            parameters = null
        )
    }
}




