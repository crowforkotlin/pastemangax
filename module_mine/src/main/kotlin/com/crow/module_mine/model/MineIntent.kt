package com.crow.module_mine.model

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_mine.model.resp.MineLoginResultsOkResp
import com.crow.module_mine.model.resp.MineResultErrorResp
import com.crow.module_mine.model.resp.MineResultsOkResp
import com.crow.module_mine.model.resp.MineUpdateInfoResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/model
 * @Time: 2023/3/18 21:17
 * @Author: CrowForKotlin
 * @Description: UserIntent
 * @formatter:on
 **************************/
open class MineIntent : BaseMviIntent() {

    data class Login(
        var username: String,
        var password: String,
        val mineLoginResultsOkResp: MineLoginResultsOkResp? = null,
        val mineResultErrorResp: MineResultErrorResp? = null,
    ) : MineIntent()

    data class Reg(
        var username: String,
        var password: String,
        val mineResultsOkResp: MineResultsOkResp? = null,
        val mineResultErrorResp: MineResultErrorResp? = null,
    ) : MineIntent()


    data class GetMineUpdateInfo(val mineUpdateInfoResp: MineUpdateInfoResp? = null): MineIntent()

    data class GetMineInfo(val userInfo: MineLoginResultsOkResp? = null) : MineIntent()
}