package com.crow.module_home.model.resp.homepage.results

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Rec comics result
 *
 * @property mType 页数
 * @property mComic 结果集
 * @constructor Create empty Rec comics result
 */

@Serializable
data class RecComicsResult(
    @SerialName(value = "type")
    val mType: Int,

    @SerialName(value = "comic")
    val mComic: ComicResultXX,
)