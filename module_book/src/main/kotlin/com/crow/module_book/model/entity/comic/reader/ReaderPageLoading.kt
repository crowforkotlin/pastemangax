package com.crow.module_book.model.entity.comic.reader

data class ReaderPageLoading(
    val mChapterID: Int = 0,
    val mChapterPagePos: Int = 0,
    val mPrevMessage: String?,
    val mNextMessage: String?,
    val mPrevUuid: String?,
    val mNextUuid: String?,
    val mCurrentUuid: String,
    val mLoadNext: Boolean? = null
)
