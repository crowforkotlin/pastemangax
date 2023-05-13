package com.crow.module_home.model.resp.search.novel_result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Novel search result
 *
 * @property mAuthor 作者集
 * @property mImageUrl 图片路径
 * @property mName 名称
 * @property mPathWord 关键词
 * @property mPopular 热度
 * @constructor Create empty Novel search result
 */
@JsonClass(generateAdapter = true)
data class SearchNovelResult(

    @Json(name = "author")
    val mAuthor: List<Author>,

    @Json(name = "cover")
    val mImageUrl: String,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int
)