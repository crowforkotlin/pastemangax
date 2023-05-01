package com.crow.module_user.network

import android.util.Base64
import com.crow.base.current_project.BaseResultResp
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.module_user.model.resp.LoginResultsOkResp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/network
 * @Time: 2023/3/18 21:20
 * @Author: CrowForKotlin
 * @Description: UserRepository
 * @formatter:on
 **************************/
class UserRepository(val service: UserService) {

    fun login(username: String, password: String) : Flow<BaseResultResp<Any>> {
        val salt = Random.nextInt(10000).toString()
        val encodePwd = Base64.encode("$password-$salt".toByteArray(), Base64.DEFAULT).decodeToString()
        return service.login(username, encodePwd, salt).onEach { value ->
            if (value.mCode == 200)
                DataStoreAgent.DATA_USER.asyncEncode(toJson((toTypeEntity<LoginResultsOkResp>(value.mResults) ?: return@onEach).also { it.mPassword = password }))
        }
    }

    fun getUserUpdateInfo() = service.getUserUpdateInfo()

    fun getUserInfo() = service.getUserInfo()
    fun reg(username: String, password: String): Flow<BaseResultResp<Any>> {
        return service.reg(username, password)
    }
}

fun main() {
    val content = "api.copymanga.tv"
    val encodeContent = java.util.Base64.getEncoder().encode(content.toByteArray()).decodeToString()
    println(encodeContent)
    println(java.util.Base64.getDecoder().decode("YXBpLmNvcHltYW5nYS50dg==").decodeToString())
}