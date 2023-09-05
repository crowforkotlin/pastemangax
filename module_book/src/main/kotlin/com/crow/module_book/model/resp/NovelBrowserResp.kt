package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.novel_browser.Browse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelBrowserResp(

    @SerialName(value = "browse")
    val mBrowse: Browse?,

    @SerialName(value = "collect")
    val mCollect: Int?,

    @SerialName(value = "is_lock")
    val mIsLock: Boolean,

    @SerialName(value = "is_login")
    val mIsLogin: Boolean,

    @SerialName(value = "is_mobile_bind")
    val mIsMobileBind: Boolean,

    @SerialName(value = "is_vip")
    val mIsVip: Boolean
)