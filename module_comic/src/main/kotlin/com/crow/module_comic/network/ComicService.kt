package com.crow.module_comic.network

import com.crow.base.current_project.BaseResultResp
import com.crow.base.current_project.BaseStrings
import com.crow.module_comic.model.resp.ChapterResultsResp
import com.crow.module_comic.model.resp.ComicResultsResp
import com.crow.module_comic.model.resp.InfoResultsResp
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

    @GET(BaseStrings.URL.ComicInfo)
    fun getComicInfo(@Path("pathword") pathword: String): Flow<BaseResultResp<InfoResultsResp>>

    @GET(BaseStrings.URL.ComicChapter)
    fun getComicChapter(
        @Path("pathword") pathword: String,
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
    ): Flow<BaseResultResp<ChapterResultsResp>>

    @GET(BaseStrings.URL.Comic)
    fun getComic(@Path("pathword") pathword: String, @Path("uuid") uuid: String): Flow<BaseResultResp<ComicResultsResp>>
}