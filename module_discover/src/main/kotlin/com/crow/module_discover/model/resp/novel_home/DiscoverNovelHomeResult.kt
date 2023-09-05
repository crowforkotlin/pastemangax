package com.crow.module_discover.model.resp.novel_home



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverNovelHomeResult (

    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "status")
    val mStatus: Int,
)