package com.crow.module_book.model.entity.comic

data class ComicActivityInfo(
    val mTitle: String,
    val mSubTitle: String,
    val mPathword: String,
    val mUuid: String,
    val mNext: String?,
    val mPrev: String?
)
