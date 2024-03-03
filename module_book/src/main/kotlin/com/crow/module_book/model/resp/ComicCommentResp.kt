package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.comic_comment.ComicCommentListResult
import com.squareup.moshi.Json

data class ComicCommentResp(
    @Json(name = "detail")
    val mDetail: String,
)