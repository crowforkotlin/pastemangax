package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.ComicResultX
import com.squareup.moshi.Json


/**
 * Hot comic
 *
 * @property mName 漫画名称
 * @property mDatetimeCreated 创建时间
 * @property mComic
 * @constructor Create empty Hot comic
 */

data class HotComic(

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "datetime_created")
    val mDatetimeCreated: String,

    @Json(name =  "comic")
    val mComic: ComicResultX,
)
