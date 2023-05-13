package com.crow.module_book.model.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookChapterEntity(val datas: MutableMap<String, BookChapterPairOf>)


