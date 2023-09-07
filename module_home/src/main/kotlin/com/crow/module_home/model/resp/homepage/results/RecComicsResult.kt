package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json


/**
 * Rec comics result
 *
 * @property mType 页数
 * @property mComic 结果集
 * @constructor Create empty Rec comics result
 */

data class RecComicsResult(
    @Json(name =  "type")
    val mType: Int,

    @Json(name =  "comic")
    val mComic: ComicResultXX,
)