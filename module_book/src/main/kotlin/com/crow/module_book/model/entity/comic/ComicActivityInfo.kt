package com.crow.module_book.model.entity.comic

data class ComicActivityInfo(
    val mTitle: String,
    val mSubTitle: String,
    val mPathword: String,
    val mComicUuid: String,
    val mChapterCurrentUuid: String,
    val mChapterNextUuid: String?,
    val mChapterPrevUuid: String?
)
