package com.crow.module_book.model.entity.comic.reader

data class ReaderLoading(
    val mChapterID: Int = 0,
    val mChapterPagePos: Int = 0,
    val mMessage: String?,
    val mPrevUUID: String?,
    val mNextUUID: String?,
    val mLoadNext: Boolean? = null,
    val mStateComplete: Boolean = false
)
