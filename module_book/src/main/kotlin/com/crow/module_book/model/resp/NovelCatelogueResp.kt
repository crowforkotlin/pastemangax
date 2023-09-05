package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.novel_catelogue.Book
import com.crow.module_book.model.resp.novel_catelogue.Volume
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelCatelogueResp(
    @SerialName(value = "book")
    val book: Book,
    @SerialName(value = "is_lock")
    val isLock: Boolean,
    @SerialName(value = "is_login")
    val isLogin: Boolean,
    @SerialName(value = "is_mobile_bind")
    val isMobileBind: Boolean,
    @SerialName(value = "is_vip")
    val isVip: Boolean,
    @SerialName(value = "volume")
    val volume: Volume
)