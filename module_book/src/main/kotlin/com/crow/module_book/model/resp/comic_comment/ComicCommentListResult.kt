package com.crow.module_book.model.resp.comic_comment


import com.squareup.moshi.Json

data class ComicCommentListResult(

    @Json(name = "comment")
    val mComment: String?,

    @Json(name = "create_at")
    val mCreateAt: String?,

    @Json(name = "id")
    val mId: Int?,

    @Json(name = "user_avatar")
    val mUserAvatar: String?,

    @Json(name = "user_id")
    val mUserId: String?,

    @Json(name = "user_name")
    val mUserName: String?
)