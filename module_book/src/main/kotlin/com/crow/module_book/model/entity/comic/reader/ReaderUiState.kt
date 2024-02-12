package com.crow.module_book.model.entity.comic.reader

data class ReaderUiState(
    val mReaderContent: ReaderContent,
    val mTotalPages: Int,
    val mCurrentPage: Int
)
