package com.crow.module_anime.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_anime.model.AnimeIntent
import com.crow.module_anime.model.resp.discover.DiscoverPageResult
import com.crow.module_anime.model.source.DiscoverPageDataSource
import com.crow.module_anime.network.AnimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class AnimeViewModel(val repository: AnimeRepository) : BaseMviViewModel<AnimeIntent>(){

    /**
     * ● PagingFlow
     *
     * ● 2023-10-10 00:50:44 周二 上午
     */
    var mDiscoverPageFlow: Flow<PagingData<DiscoverPageResult>>? = null
        private set

    /**
     * ● 排序方式
     *
     * ● 2023-10-10 00:50:54 周二 上午
     */
    private var mOrder: String = "-datetime_updated"

    /**
     * ● 年份 不填则默认 选填 20xx
     *
     * ● 2023-10-10 01:52:48 周二 上午
     */
    private var mYear: String = ""

    /**
     * ● 总数
     *
     * ● 2023-10-10 00:51:02 周二 上午
     */
    var mTotals: Int = 0
        private set


    override fun dispatcher(intent: AnimeIntent) {
        when(intent) {
            is AnimeIntent.DiscoverPageIntent -> onDiscoverPageIntent(intent)
        }
    }

    private fun onDiscoverPageIntent(intent: AnimeIntent.DiscoverPageIntent) {
        mDiscoverPageFlow = Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                DiscoverPageDataSource { position, pageSize ->
                    flowResult(
                        repository.getAnimeDiscoverPage(
                            order = mOrder,
                            year = mYear,
                            offset = position,
                            limit = pageSize
                        ), intent
                    ) { value ->
                        if (mTotals == 0) mTotals = value.mResults.mTotal
                        intent.copy(pages = value.mResults)
                    }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun setOrder(order: String) {
        mOrder = order
    }

    fun setYear(year: String) {
        mYear = year
    }
}