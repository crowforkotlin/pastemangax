package com.crow.module_book.model.entity.comic.reader

import com.crow.module_book.ui.fragment.comic.reader.ComicCategories

data class ReaderUiState(
    val mReaderMode: ComicCategories.Type,
    val mReaderContent: ReaderContent,
    val mChapterID: Int,
    val mTotalPages: Int,
    val mCurrentPagePos: Int,
    val mCurrentPagePosOffset: Int
)
