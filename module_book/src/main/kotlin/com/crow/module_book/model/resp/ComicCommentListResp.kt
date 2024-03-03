package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.comic_comment.ComicCommentListResult
import com.squareup.moshi.Json

data class ComicCommentListResp(
    @Json(name = "limit")
    val mLimit: Int?,

    @Json(name = "list")
    val mList: List<ComicCommentListResult>?,

    @Json(name = "offset")
    val mOffset: Int?,

    @Json(name = "total")
    val mTotal: Int?
)