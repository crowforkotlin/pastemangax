package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.novel_tag.Ordering
import com.crow.module_discover.model.resp.novel_tag.Theme
import com.crow.module_discover.model.resp.novel_tag.Top

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverNovelTagResp(

    @SerialName(value = "ordering")
    val mOrdering: List<Ordering>,

    @SerialName(value = "theme")
    val mTheme: List<Theme>,

    @SerialName(value = "top")
    val mTop: List<Top>
)