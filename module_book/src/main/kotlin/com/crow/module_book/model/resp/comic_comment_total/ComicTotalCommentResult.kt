package com.crow.module_book.model.resp.comic_comment_total


import com.squareup.moshi.Json

data class ComicTotalCommentResult(

    @Json(name = "comment")
    val mComment: String?,

    @Json(name = "count")
    val mCount: Int?,

    @Json(name = "create_at")
    val mCreateAt: String?,

    @Json(name = "id")
    val mId: Int?,

    @Json(name = "parent_id")
    val mParentId: Any?,

    @Json(name = "parent_user_id")
    val mParentUserId: Any?,

    @Json(name = "parent_user_name")
    val mParentUserName: Any?,

    @Json(name = "user_avatar")
    val mUserAvatar: String?,

    @Json(name = "user_id")
    val mUserId: String?,

    @Json(name = "user_name")
    val mUserName: String?
)