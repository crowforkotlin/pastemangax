package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverComicHomeResp(

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<DiscoverComicHomeResult>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int
)