package com.crow.module_book.model.entity.comic.reader

data class ReaderContent(
    val mComicName: String,
    val mComicUUID: String,
    val mComicPathword: String,
    val mPages: List<Any>,
    val mChapterInfo: ReaderInfo?
)
