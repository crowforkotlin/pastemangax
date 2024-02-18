package com.crow.module_book.model.entity.comic.reader

data class ReaderUiState(
    val mReaderContent: ReaderContent,
    val mChapterID: Int,
    val mTotalPages: Int,
    val mCurrentPagePos: Int,
    val mCurrentPagePosOffset: Int
)
