package com.crow.module_anime.model.resp.search


import com.squareup.moshi.Json

data class SearchResult(

    @Json(name = "alias")
    val mAlias: String,

    @Json(name = "company")
    val mCompany: Company,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "females")
    val mFemales: List<Any>,

    @Json(name = "males")
    val mMales: List<Any>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "parodies")
    val mParodies: Any?,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "theme")
    val mTheme: List<Any>,

    @Json(name = "years")
    val mYears: String
)