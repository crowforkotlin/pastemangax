package com.crow.module_home.network

import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.resp.BaseResultResp
import com.crow.module_home.model.resp.homepage.ComicDatas
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.model.resp.search.SearchComicResp
import com.crow.module_home.model.resp.search.SearchNovelResp
import com.crow.module_home.model.resp.topic.TopicResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/network
 * @Time: 2023/3/6 9:13
 * @Author: CrowForKotlin
 * @Description: HomeService
 * @formatter:on
 **************************/
interface HomeService {

    @GET(BaseStrings.URL.HomePage)
    fun getHomePage(): Flow<BaseResultResp<Results>>

    @GET(BaseStrings.URL.RefreshRec)
    fun getRecPage(
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
        @Query("pos") pos: Int = 3200102
    ): Flow<BaseResultResp<ComicDatas<RecComicsResult>>>

    @GET(BaseStrings.URL.ComicSearch)
    fun doSearchComic(
        @Query("q") keyword: String,
        @Query("q_type") type: String,
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
        @Query("platform") platform: Int = 3
    ) : Flow<BaseResultResp<SearchComicResp>>

    @GET(BaseStrings.URL.NovelSearch)
    fun doSearchNovel(
        @Query("q") keyword: String,
        @Query("q_type") type: String,
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
        @Query("platform") platform: Int = 3
    ) : Flow<BaseResultResp<SearchNovelResp>>

    @GET(BaseStrings.URL.ComicTopic)
    fun getTopic(
        @Path(BaseStrings.PATH_WORD) pathword: String,
        @Query("offset") start: Int,
        @Query("limit") limit: Int
    ) : Flow<BaseResultResp<TopicResp>>
}