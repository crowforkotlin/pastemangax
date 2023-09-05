package com.crow.module_home.model.resp.homepage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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

@Serializable
data class FinishComicDatas(
    @SerialName(value = "list")
    val mResult: List<FinishComic>,

    @SerialName(value = "total")
    val mTotal: Int,

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "type")
    val mType: String,
) 