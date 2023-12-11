package com.crow.module_anime.model.resp.video


import com.squareup.moshi.Json

data class Chapter(
    @Json(name = "count")
    val mCount: Int,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "lines")
    val mLines: Lines,

    @Json(name = "name")
    val mName: String,

    @Json(name = "uuid")
    val mUUID: String,

    @Json(name = "v_cover")
    val mVCover: String,

    @Json(name = "vid")
    val mVid: Any?,

    @Json(name = "video")
    val mVideo: String,

    @Json(name = "video_list")
    val mVideoList: Any?
)