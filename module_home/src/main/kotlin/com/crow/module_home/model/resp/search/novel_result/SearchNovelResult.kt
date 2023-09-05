package com.crow.module_home.model.resp.search.novel_result

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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

@Serializable
data class SearchNovelResult(

    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int
)