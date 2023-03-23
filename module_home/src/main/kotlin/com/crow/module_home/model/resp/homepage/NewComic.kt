package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.ComicResultX
import com.squareup.moshi.Json

/**
 * New comic
 *
 * @property mName 漫画名称
 * @property mDatetimeCreated 创建日期
 * @property mComic 新漫画结果集
 * @constructor Create empty New comic
 */
data class NewComic(

    @Json(name = "name")
    val mName: String,

    @Json(name = "datetime_created")
    val mDatetimeCreated: String,

    @Json(name = "comic")
    val mComic: ComicResultX,
)

