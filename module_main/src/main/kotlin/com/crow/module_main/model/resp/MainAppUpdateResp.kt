package com.crow.module_main.model.resp

import com.crow.module_main.model.resp.update.Update
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/model/resp
 * @Time: 2023/4/7 15:57
 * @Author: CrowForKotlin
 * @Description: MainAppUpdateResp
 * @formatter:on
 **************************/
@JsonClass(generateAdapter = true)
data class MainAppUpdateResp(

    @Json(name = "updates")
    val mUpdates: List<Update>,

    @Json(name = "update_force")
    val mForceUpdate: Boolean,

    @Json(name = "update_force_version_code")
    val mForceUpdateVersionCode: Int,
)

