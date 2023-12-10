package com.crow.module_anime.network

import androidx.annotation.IntRange
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.resp.BaseResultResp
import com.crow.module_anime.model.req.RegReq
import com.crow.module_anime.model.resp.chapter.AnimeChapterResp
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import com.crow.module_anime.model.resp.info.AnimeInfoResp
import com.crow.module_anime.model.resp.search.SearchResp
import com.crow.module_anime.model.resp.site.SiteResp
import com.crow.module_anime.model.resp.video.AnimeVideoResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeService {

    @GET(BaseStrings.URL.HotManga_AnimeChapterList)
    fun getAnimeChapterList(@Path(BaseStrings.PATH_WORD) pathword: String): Flow<BaseResultResp<AnimeChapterResp>>

    @GET(BaseStrings.URL.HotManga_AnimeInfoPage)
    fun getAnimeInfo(@Path(BaseStrings.PATH_WORD) pathword: String): Flow<BaseResultResp<AnimeInfoResp>>

    @GET(BaseStrings.URL.HotManga_AnimePage)
    fun getAnimHome(
        @Query("ordering") order: String,
        @Query("year") year: String,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Flow<BaseResultResp<DiscoverPageResp>>

    @POST(BaseStrings.URL.HotManga_Reg)
    fun reg(@Body regReq: RegReq): Flow<BaseResultResp<Any>>

    @POST(BaseStrings.URL.HotManga_Login)
    @FormUrlEncoded
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("salt") salt: String,
        @Field("source") source: String = "Offical",
        @Field("platform") platform: String = "1",
    ): Flow<BaseResultResp<Any>>

    @GET(BaseStrings.URL.HotManga_AnimeVideo)
    fun getVideo(
        @Path(BaseStrings.PATH_WORD) pathword: String,
        @Path("chapter_uuid") chapterUUID: String
    ): Flow<BaseResultResp<AnimeVideoResp>>

    @GET(BaseStrings.URL.HotManga_Site)
    fun getSite(): Flow<BaseResultResp<SiteResp>>

    @GET(BaseStrings.URL.HotManga_Search)
    fun getSearchPage(
        @Query("q") query: String,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ): Flow<BaseResultResp<SearchResp>>
}