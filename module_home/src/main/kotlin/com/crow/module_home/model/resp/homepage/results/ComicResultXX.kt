package com.crow.module_home.model.resp.homepage.results

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Hot comic result
 *
 * @property mName 漫画名称
 * @property mPathWord 路径词
 * @property mAuthorResult 作者集
 * @property mTheme 主题
 * @property mImageUrl 图片路径
 * @property mImageType 图片类型
 * @property mPopular 热度（应该是浏览量）
 * @constructor Create empty Hot comic result
 */

@Serializable
data class ComicResultXX(
    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "author")
    val mAuthorResult: List<AuthorResult>,

    @SerialName(value = "img_type")
    val mImageType: Int,

    @SerialName(value = "theme")
    val mTheme: List<ThemeResult>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    )