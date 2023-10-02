package com.crow.module_discover.model.resp.novel_home

import com.squareup.moshi.Json


data class DiscoverNovelHomeResult (

    @Json(name =  "author")
    val mAuthor: List<Author>,

    @Json(name =  "cover")
    val mImageUrl: String,

    @Json(name =  "datetime_updated")
    val mDatetimeUpdated: String?,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,

    @Json(name =  "popular")
    val mPopular: Int,

    @Json(name =  "status")
    val mStatus: Int,
)