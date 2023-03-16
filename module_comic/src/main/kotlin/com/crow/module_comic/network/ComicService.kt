package com.crow.module_comic.network

import com.crow.module_comic.model.resp.ComicChapterResp
import com.crow.module_comic.model.resp.ComicInfoResp
import com.crow.module_comic.model.resp.ComicResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/network
 * @Time: 2023/3/15 0:15
 * @Author: CrowForKotlin
 * @Description: ComicService
 * @formatter:on
 **************************/
interface ComicService {

    @GET("/api/v3/comic2/{pathword}?platform=1&_update=true")
    fun getComicInfo(@Path("pathword") pathword: String): Flow<ComicInfoResp>

    @GET("/api/v3/comic/{pathword}/group/default/chapters?_update=true")
    fun getComicChapter(
        @Path("pathword") pathword: String,
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
    ): Flow<ComicChapterResp>

    @GET("/api/v3/comic/{pathword}/chapter2/{uuid}?platform=3&_update=true")
    fun getComic(@Path("pathword") pathword: String, @Path("uuid") uuid: String): Flow<ComicResp>
}