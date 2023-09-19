package com.crow.module_home.model.resp.homepage

import com.squareup.moshi.Json


/**
 * Finish comics
 *
 * @property mResult 漫画结果集
 * @property mTotal 漫画总数
 * @property mLimit 限制的个数
 * @property mOffset 起点
 * @property mPathWord 路径词
 * @property mName "已完结"
 * @property mType "top"
 * @constructor Create empty Finish comics
 */

data class FinishComicDatas(
    @Json(name =  "list")
    val mResult: List<FinishComic>,

    @Json(name =  "total")
    val mTotal: Int,

    @Json(name =  "limit")
    val mLimit: Int,

    @Json(name =  "offset")
    val mOffset: Int,

    @Json(name =  "path_word")
    val mPathWord: String,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "type")
    val mType: String,
) 