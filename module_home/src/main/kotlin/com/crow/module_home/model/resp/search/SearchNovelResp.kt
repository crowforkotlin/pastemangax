package com.crow.module_home.model.resp.search


import com.crow.module_home.model.resp.search.novel_result.SearchNovelResult
import com.squareup.moshi.Json


/**
 * Novel search resp
 *
 * @property mLimit 偏移量
 * @property mList 结果集
 * @property mOffset 起始位置
 * @property mTotal 总数
 * @constructor Create empty Novel search resp
 */

data class SearchNovelResp(

    @Json(name =  "limit")
    val mLimit: Int,

    @Json(name =  "list")
    val mList: List<SearchNovelResult>,

    @Json(name =  "offset")
    val mOffset: Int,

    @Json(name =  "total")
    val mTotal: Int
)