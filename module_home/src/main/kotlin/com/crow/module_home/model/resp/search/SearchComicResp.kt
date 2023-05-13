package com.crow.module_home.model.resp.search


import com.crow.module_home.model.resp.search.comic_reuslt.SearchComicResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Comic search resp
 *
 * @property mLimit 偏移量
 * @property mList  结果集
 * @property mOffset 起始位置
 * @property mTotal 总数
 * @constructor Create empty Comic search resp
 */
@JsonClass(generateAdapter = true)
data class SearchComicResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<SearchComicResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)