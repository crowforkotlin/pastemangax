package com.crow.base.ui.dialog

sealed interface ILoadingAnimEvent {
    fun interface ILoadingPreStart {
        fun doOnPreStart(loadingAnim: LoadingAnimDialog)
    }

    fun interface ILoadingProgress {
        fun doOnProgress(loadingAnim: LoadingAnimDialog)
    }

    fun interface ILoadingEnd {
        fun doOnEnd(loadingAnim: LoadingAnimDialog)
    }
}