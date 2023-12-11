package com.crow.module_book.model.resp

import com.crow.module_book.model.resp.novel_chapter.NovelChapterResult
import com.squareup.moshi.Json


data class NovelChapterResp(
    @Json(name =  "limit")
    val mLimit: Int,

    @Json(name =  "list")
    val mList: List<NovelChapterResult>,

    @Json(name =  "offset")
    val mOffset: Int,

    @Json(name =  "total")
    val mTotal: Int
)