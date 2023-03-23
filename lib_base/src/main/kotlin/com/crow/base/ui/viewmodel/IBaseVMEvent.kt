package com.crow.base.ui.viewmodel

sealed interface IBaseVMEvent {
    fun interface OnSuccess<T> {
        fun onSuccess(value: T)
    }

    fun interface OnFailure<T> {
        fun onFailure(value: T)
    }
}