package com.crow.module_book.model.entity

data class ComicLoadMorePage(
    val mIsNext: Boolean,
    val mHasNext: Boolean,
    val mIsLoading: Boolean,
    val mPrevContent: String,
    val mNextContent: String,
)
