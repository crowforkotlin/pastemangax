package com.crow.module_anime.network

import androidx.annotation.IntRange
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.resp.BaseResultResp
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimeService {

    @GET(BaseStrings.URL.HotManga_AnimePage)
    fun getAnimHome(
        @Query("ordering") order: String,
        @Query("year") year: String,
        @Query("offset") offset: Int,
        @Query("limit") @IntRange(from = 1, to = 50) limit: Int
    ) : Flow<BaseResultResp<DiscoverPageResp>>
}