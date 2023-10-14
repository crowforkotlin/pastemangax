package com.crow.module_anime.model.resp.discover


import com.squareup.moshi.Json

data class DiscoverPageResult(

    @Json(name = "b_subtitle")
    val mBSubtitle: Boolean,

    @Json(name = "count")
    val mCount: Int,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "datetime_updated")
    val mDatetimeUpdated: String?,

    @Json(name = "females")
    val mFemales: List<Any>,

    @Json(name = "males")
    val mMales: List<Any>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "theme")
    val mTheme: List<Any>,

    @Json(name = "years")
    val mYears: String
)