package com.crow.module_anime.network

import android.util.Base64
import com.crow.base.copymanga.resp.BaseResultResp
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.module_anime.model.entity.AccountEntity
import com.crow.module_anime.model.req.RegReq
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import com.crow.module_anime.model.resp.login.UserLoginResp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

class AnimeRepository(val service: AnimeService) {

    fun getAnimeChapterList(pathword: String) = service.getAnimeChapterList(pathword).flowOn(Dispatchers.IO)

    fun getAnimeInfoPage(pathword: String) = service.getAnimeInfo(pathword).flowOn(Dispatchers.IO)

    fun getAnimeDiscoverPage(
        order: String,
        year: String,
        offset: Int,
        limit: Int
    ): Flow<BaseResultResp<DiscoverPageResp>> {
        return service.getAnimHome(
            order = order,
            year = year,
            offset = offset,
            limit = limit
        ).flowOn(Dispatchers.IO)
    }

    fun reg(regReq: RegReq): Flow<BaseResultResp<Any>> {
        return service.reg(regReq).flowOn(Dispatchers.IO)
    }

    fun login(username: String, password: String): Flow<BaseResultResp<Any>> {
        val salt = Random.nextInt(10000).toString()
        val encodePwd = Base64.encode("$password-$salt".toByteArray(), Base64.DEFAULT).decodeToString()

        return service.login(
            username = username,
            password = encodePwd,
            salt = salt
        )
            .onEach { value ->
                if (value.mCode == 200) {
                    val user = toTypeEntity<UserLoginResp>(value.mResults)
                    if (user != null) {
                        DataStoreAgent.DATA_USER_RELA.asyncEncode(
                            toJson(
                                AccountEntity(
                                    mUsername = user.mUsername,
                                    mPassword = user.mUsername,
                                    mToken = user.mToken
                                )
                            )
                        )
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }
}