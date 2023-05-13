package com.crow.module_user.network

import com.crow.base.copymanga.BaseResultResp
import com.crow.base.copymanga.BaseStrings
import com.crow.module_user.model.resp.LoginResultsOkResp
import com.crow.module_user.model.resp.UserUpdateInfoResp
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
interface UserService {

    @POST(BaseStrings.URL.Login)
    @FormUrlEncoded
    fun login(@Field("username") username: String, @Field("password") password: String, @Field("salt") salt: String) : Flow<BaseResultResp<Any>>

    @POST(BaseStrings.URL.Reg)
    @FormUrlEncoded
    fun reg(@Field("username") username: String, @Field("password") password: String): Flow<BaseResultResp<Any>>

    @GET(BaseStrings.URL.UserUpdateInfo)
    fun getUserUpdateInfo() : Flow<BaseResultResp<UserUpdateInfoResp>>

    @GET(BaseStrings.URL.UserInfo)
    fun getUserInfo() : Flow<BaseResultResp<LoginResultsOkResp>>
}