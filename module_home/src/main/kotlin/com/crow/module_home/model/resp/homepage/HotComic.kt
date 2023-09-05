package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.ComicResultX
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hot comic
 *
 * @property mName 漫画名称
 * @property mDatetimeCreated 创建时间
 * @property mComic
 * @constructor Create empty Hot comic
 */

@Serializable
data class HotComic(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "datetime_created")
    val mDatetimeCreated: String,

    @SerialName(value = "comic")
    val mComic: ComicResultX,
)
