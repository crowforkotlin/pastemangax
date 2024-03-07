package com.crow.module_main.network

import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.resp.BaseResultResp
import com.crow.module_main.model.resp.comic_history.ComicHistoryResp
import com.crow.module_main.model.resp.novel_history.NovelHistoryResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

interface MainService {

    /**
     * ⦁ 获取漫画浏览历史记录
     *
     * ⦁ 2023-10-03 18:58:22 周二 下午
     */
    @GET(BaseStrings.URL.ComicHistory)
    fun getComicHistory(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("ordering") order: String
    ) : Flow<BaseResultResp<ComicHistoryResp>>

    /**
     * ⦁ 获取漫画浏览历史记录
     *
     * ⦁ 2023-10-03 18:58:22 周二 下午
     */
    @GET(BaseStrings.URL.NovelHistory)
    fun getNovelHistory(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("ordering") order: String
    ) : Flow<BaseResultResp<NovelHistoryResp>>
}