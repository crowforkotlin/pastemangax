package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.novel_info.NovelInfoResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelInfoResp(
    @SerialName(value = "book")
    val mNovel: NovelInfoResult,

    @SerialName(value = "is_lock")
    val isLock: Boolean,

    @SerialName(value = "is_login")
    val isLogin: Boolean,

    @SerialName(value = "is_mobile_bind")
    val isMobileBind: Boolean,

    @SerialName(value = "is_vip")
    val isVip: Boolean,

    @SerialName(value = "popular")
    val popular: Int
)