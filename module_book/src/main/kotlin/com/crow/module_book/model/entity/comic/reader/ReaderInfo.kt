package com.crow.module_book.model.entity.comic.reader

data class ReaderInfo(
    val mChapterIndex: Int,
    val mChapterID: String,
    val mChapterName: String,
    val mChapterCount: Int,
    val mChapterUpdate: String,
    val mNextUUID: String?,
    val mPrevUUID: String?
)
