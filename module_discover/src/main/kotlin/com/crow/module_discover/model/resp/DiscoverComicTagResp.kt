package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.comic_tag.Ordering
import com.crow.module_discover.model.resp.comic_tag.Theme
import com.crow.module_discover.model.resp.comic_tag.Top

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverComicTagResp(
    @SerialName(value = "ordering")
    val ordering: List<Ordering>,
    @SerialName(value = "theme")
    val theme: List<Theme>,
    @SerialName(value = "top")
    val top: List<Top>
)