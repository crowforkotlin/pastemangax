package com.crow.module_discover.model.resp.comic_home

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverComicHomeResult (

    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,

    @SerialName(value = "females")
    val mFemales: List<@Polymorphic Any>,

    @SerialName(value = "free_type")
    val mFreeType: FreeType,

    @SerialName(value = "males")
    val mMales: List<@Polymorphic Any>,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "theme")
    val mTheme: List<Theme>
)