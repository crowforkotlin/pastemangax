package com.crow.base.ui.dialog

import android.view.Window

sealed class IBaseDFLifeCycle private constructor() {
    fun interface IDFOnStart {
        fun doBeforeWhenOnStart(window: Window)
    }

    fun interface IDFOnProgress {
        fun doOnProgress(anim: LoadingAnimDialog)
    }

}
