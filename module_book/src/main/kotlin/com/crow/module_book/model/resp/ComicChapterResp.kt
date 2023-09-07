package com.crow.module_book.model.resp

import com.crow.module_book.model.resp.comic_chapter.ComicChapterResult
import com.squareup.moshi.Json

data class ComicChapterResp(

    @Json(name =  "limit")
    val mLimit: Int,

    @Json(name =  "list")
    val mList: List<ComicChapterResult>,

    @Json(name =  "offset")
    val mOffset: Int,

    @Json(name =  "total")
    val mTotal: Int,
)