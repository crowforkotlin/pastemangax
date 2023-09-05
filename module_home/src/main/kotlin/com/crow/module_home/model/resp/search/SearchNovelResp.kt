package com.crow.module_home.model.resp.search


import com.crow.module_home.model.resp.search.novel_result.SearchNovelResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Novel search resp
 *
 * @property mLimit 偏移量
 * @property mList 结果集
 * @property mOffset 起始位置
 * @property mTotal 总数
 * @constructor Create empty Novel search resp
 */

@Serializable
data class SearchNovelResp(

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<SearchNovelResult>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int
)