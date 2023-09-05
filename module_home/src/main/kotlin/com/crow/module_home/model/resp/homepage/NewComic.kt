package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.ComicResultX
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * New comic
 *
 * @property mName 漫画名称
 * @property mDatetimeCreated 创建日期
 * @property mComic 新漫画结果集
 * @constructor Create empty New comic
 */

@Serializable
data class NewComic(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "datetime_created")
    val mDatetimeCreated: String,

    @SerialName(value = "comic")
    val mComic: ComicResultX,
)

