package com.crow.module_home.model.resp.topic


import com.squareup.moshi.Json

data class TopicResult(
    @Json(name = "author")
    val mAuthor: List<Author>,

    @Json(name = "c_type")
    val mCType: Int,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "females")
    val mFemales: List<Any>,

    @Json(name = "img_type")
    val mImgType: Int,

    @Json(name = "males")
    val males: List<Any>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "parodies")
    val mParodies: List<Any>,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "theme")
    val mTheme: List<Theme>,

    @Json(name = "type")
    val mType: Int
)