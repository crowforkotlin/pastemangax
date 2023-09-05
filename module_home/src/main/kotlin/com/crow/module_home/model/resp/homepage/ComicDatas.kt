package com.crow.module_home.model.resp.homepage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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

@Serializable
data class ComicDatas<T>(

    @SerialName(value = "list")
    val mResult: List<T>,

    @SerialName(value = "offset")
    val offset: Int,

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "total")
    val total: Int
)