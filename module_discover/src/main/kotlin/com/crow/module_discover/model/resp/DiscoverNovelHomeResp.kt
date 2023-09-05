package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.novel_home.DiscoverNovelHomeResult

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverNovelHomeResp(
    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<DiscoverNovelHomeResult>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int
)