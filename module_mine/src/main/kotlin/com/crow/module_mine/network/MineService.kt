package com.crow.module_mine.network

import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.resp.BaseResultResp
import com.crow.module_mine.model.resp.MineLoginResultsOkResp
import com.crow.module_mine.model.resp.MineUpdateInfoResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/network
 * @Time: 2023/3/18 21:20
 * @Author: CrowForKotlin
 * @Description: UserService
 * @formatter:on
 **************************/
interface MineService {

    @POST(BaseStrings.URL.Login)
    @FormUrlEncoded
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("salt") salt: String
    ): Flow<BaseResultResp<Any>>

    @POST(BaseStrings.URL.Reg)
    @FormUrlEncoded
    fun reg(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("source") source: String = "freeSite",
        @Field("version") version: String = "2024.01.01",
        @Field("platform") platform: String = "1"
    ): Flow<BaseResultResp<Any>>

    @GET(BaseStrings.URL.UserUpdateInfo)
    fun getUserUpdateInfo(): Flow<BaseResultResp<MineUpdateInfoResp>>

    @GET(BaseStrings.URL.UserInfo)
    fun getUserInfo(): Flow<BaseResultResp<MineLoginResultsOkResp>>
}