package com.crow.module_anime.network

import com.crow.base.copymanga.resp.BaseResultResp
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepository(val service: AnimeService) {

    fun getAnimeChapterList(pathword: String) = service.getAnimeChapterList(pathword)

    fun getAnimeInfoPage(pathword: String) = service.getAnimeInfo(pathword)

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
}