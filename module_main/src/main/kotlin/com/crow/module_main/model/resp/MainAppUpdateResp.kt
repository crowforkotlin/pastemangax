package com.crow.module_main.model.resp

import com.crow.module_main.model.resp.update.Update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/model/resp
 * @Time: 2023/4/7 15:57
 * @Author: CrowForKotlin
 * @Description: MainAppUpdateResp
 * @formatter:on
 **************************/

@Serializable
data class MainAppUpdateResp(

    @SerialName(value = "updates")
    val mUpdates: MutableList<Update>,

    @SerialName(value = "update_force")
    val mForceUpdate: Boolean,

    @SerialName(value = "update_force_version_code")
    val mForceUpdateVersionCode: Int,
)

