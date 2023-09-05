package com.crow.base.copymanga

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Home page resp
 *
 * @property mCode 响应码
 * @property mMessage 消息
 * @property mResults 结果集
 * @constructor Create empty Home page resp
 */

@Serializable
data class BaseNullableResultResp<T>(

    @SerialName(value = "code")
    val mCode: Int,

    @SerialName(value = "message")
    val mMessage: String,

    @SerialName(value = "results")
    val mResults: T?
)