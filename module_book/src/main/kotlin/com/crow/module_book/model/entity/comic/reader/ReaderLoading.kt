package com.crow.module_book.model.entity.comic.reader

data class ReaderLoading(
    val mChapterID: Int = 0,
    val mChapterPagePos: Int = 0,
    val mMessage: String?,
    val mPrevUuid: String?,
    val mNextUuid: String?,
    val mCurrentUuid: String,
    val mLoadNext: Boolean? = null,
    val mStateComplete: Boolean = false
)
