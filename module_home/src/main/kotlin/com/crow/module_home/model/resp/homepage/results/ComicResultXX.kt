package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json


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
data class ComicResultXX(
    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "author")
    val mAuthorResult: List<AuthorResult>,

    @Json(name = "img_type")
    val mImageType: Int,

    @Json(name = "theme")
    val mTheme: List<ThemeResult>,

    @Json(name = "cover")
    val mImageUrl: String,

    @Json(name = "popular")
    val mPopular: Int,

    )