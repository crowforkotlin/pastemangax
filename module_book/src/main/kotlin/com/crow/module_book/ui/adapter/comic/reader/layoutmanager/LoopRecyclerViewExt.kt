package com.crow.module_book.ui.adapter.comic.reader.layoutmanager

import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.log

internal fun LoopLayoutManager.handleScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    if (RecyclerView.SCROLL_STATE_IDLE == newState && childCount > 0) {
        val v = snapHelper.findSnapView(this) ?: return
        val pos = getPosition(v)
        "state idle, cur selected pos is : $pos".log()
        if (pos >= 0 && mCurrentItem != pos) {
            mCurrentItem = pos
            dispatchOnPageSelected(mCurrentItem)
        }
    }
}


fun RecyclerView.curSelectedPage(): Int {
    return (layoutManager as? LoopLayoutManager)?.mCurrentItem ?: -1
}

fun RecyclerView.addOnPageChangeListener(l: OnPageChangeListener) {
    (layoutManager as? LoopLayoutManager)?.addPageChangeListener(l)
}

fun RecyclerView.removeOnPageChangeListener(l: OnPageChangeListener) {
    (layoutManager as? LoopLayoutManager)?.removePageChangeListener(l)
}