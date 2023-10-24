package com.crow.module_mine.ui.tools

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.leandroborgesferreira.loadingbutton.customViews.ProgressButton

// Fix Memory Leak
fun ProgressButton.updateLifecycleObserver(lifecycle: Lifecycle?) {
    getContext().removeLifecycleObserver(this) // to fix the leak.
    lifecycle?.addObserver(this)  // to fix leaking after the fragment's view is destroyed.
}

// Fix Memory Leak
private fun Context.removeLifecycleObserver(observer: LifecycleObserver) {
    when (this) {
        is LifecycleOwner -> lifecycle.removeObserver(observer)
        is ContextThemeWrapper -> baseContext.removeLifecycleObserver(observer)
        is androidx.appcompat.view.ContextThemeWrapper -> baseContext.removeLifecycleObserver(observer)
    }
}