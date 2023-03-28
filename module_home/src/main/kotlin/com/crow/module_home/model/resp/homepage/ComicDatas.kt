package com.crow.module_home.model.resp.homepage

import com.squareup.moshi.Json

/**
 * ComicPage datas
 *
 * @param T
 * @property mLimit 页数
 * @property mResult 结果集
 * @property offset 起点
 * @property total 漫画总数
 * @constructor Create empty ComicPage datas
 */
data class ComicDatas<T>(


    @Json(name = "list")
    val mResult: List<T>,

    @Json(name = "offset")
    val offset: Int,

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "total")
    val total: Int
)