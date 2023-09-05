package com.crow.module_home.model.resp.search.comic_reuslt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Result
 *
 * @property mAlias 漫画别名
 * @property mAuthor 作者集
 * @property mImageUrl 图片路径
 * @property mImgType 图片类型 0和1 暂时未知区别
 * @property mName 漫画名称
 * @property mPathWord 关键词
 * @property mPopular
 * @constructor Create empty Result
 */

@Serializable
data class SearchComicResult (

    @SerialName(value = "alias")
    val mAlias: String?,

    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "img_type")
    val mImgType: Int?,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int
)