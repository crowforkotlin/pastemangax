package com.crow.module_book.model.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookChapterPairOf(val bookChapterName: String, val bookChapterType: Int)