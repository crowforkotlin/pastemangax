package com.crow.module_book.model.resp.comic_comment_total


import com.squareup.moshi.Json

data class ComicTotalCommentResp(

    @Json(name = "limit")
    val mLimit: Int?,

    @Json(name = "list")
    val mList: List<ComicTotalCommentResult>?,

    @Json(name = "offset")
    val mOffset: Int?,

    @Json(name = "total")
    val mTotal: Int?
)