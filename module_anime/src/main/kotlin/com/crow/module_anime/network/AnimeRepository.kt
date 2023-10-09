package com.crow.module_anime.network

import com.crow.base.copymanga.resp.BaseResultResp
import com.crow.base.tools.extensions.logger
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepository(val service: AnimeService) {

    fun getAnimeDiscoverPage(
        order: String,
        year: String,
        offset: Int,
        limit: Int
    ): Flow<BaseResultResp<DiscoverPageResp>> {
        logger(year)
        return service.getAnimHome(
            order = order,
            year = year,
            offset = offset,
            limit = limit
        ).flowOn(Dispatchers.IO)
    }
}