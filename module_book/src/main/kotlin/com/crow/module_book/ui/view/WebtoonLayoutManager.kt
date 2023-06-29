package com.crow.module_book.ui.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.module_book.ui.activity.ComicActivity

/**
 * Layout manager used by the webtoon viewer. Item prefetch is disabled because the extra layout
 * space feature is used which allows setting the image even if the holder is not visible,
 * avoiding (in most cases) black views when they are visible.
 *
 * This layout manager uses the same package name as the support library in order to use a package
 * protected method.
 */
class WebtoonLayoutManager(activity: ComicActivity) : LinearLayoutManager(activity) {

    /**
     * Extra layout space is set to half the screen height.
     */
    private val extraLayoutSpace = activity.resources.displayMetrics.heightPixels / 2

    init {
        isItemPrefetchEnabled = false
    }

    /**
     * Returns the custom extra layout space.
     */
    override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
        return extraLayoutSpace
    }

}