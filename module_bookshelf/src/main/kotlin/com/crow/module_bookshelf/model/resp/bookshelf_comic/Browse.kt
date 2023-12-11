package com.crow.module_bookshelf.model.resp.bookshelf_comic

import com.squareup.moshi.Json


data class Browse(
    @Json(name =  "chapter_name")
    val mChapterName: String,

    @Json(name =  "chapter_uuid")
    val mChapterUuid: String,

    @Json(name =  "comic_uuid")
    val mComicUuid: String,

    @Json(name =  "path_word")
    val mPathWord: String
)