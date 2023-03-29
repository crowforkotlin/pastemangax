package com.crow.module_discover.model.resp.home


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscoverHomeResult (

    @Json(name = "author")
    val mAuthor: List<Author>,

    @Json(name = "cover")
    val mImageUrl: String,

    @Json(name = "datetime_updated")
    val mDatetimeUpdated: String,

    @Json(name = "females")
    val mFemales: List<Any>,

    @Json(name = "free_type")
    val mFreeType: FreeType,

    @Json(name = "males")
    val mMales: List<Any>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "theme")
    val mTheme: List<Theme>
)