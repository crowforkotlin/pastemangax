package com.crow.module_main.model.resp

import com.squareup.moshi.Json

data class MainNoticeResp(
    @Json(name = "author")
    val mAuthor: String,

    @Json(name = "content")
    val mContent: String,

    @Json(name = "time")
    val mTime: String,

    @Json(name = "force_time")
    val mForceTime: Int,

    @Json(name = "version")
    val mVersion: Long,

    @Json(name = "force_content")
    val mForceContent: String,

    @Json(name = "readed_button_text")
    val mReadedButtonText: String,

    @Json(name = "new_time")
    val mNewTime: String,

    @Json(name = "new_author")
    val mNewAuthor: String,

    @Json(name = "author_icon_link")
    val mAuthorLink: String
)