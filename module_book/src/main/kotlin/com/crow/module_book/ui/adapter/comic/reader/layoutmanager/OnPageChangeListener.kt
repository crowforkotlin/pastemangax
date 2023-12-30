package com.crow.module_book.ui.adapter.comic.reader.layoutmanager

interface OnPageChangeListener {
    fun onPageSelected(pos: Int)

    fun onPageScrollState(state: Int)
}