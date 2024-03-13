package com.crow.module_book.model.entity.comic.reader

data class ReaderPrevNextInfo(
    val mChapterID: Int,
    val mChapterPagePos: Int = 0,
    val mUuid: String?,
    val mIsNext: Boolean,
    val mInfo: String
)
