package com.crow.module_discover.ui.viewmodel

import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.getTag
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.model.resp.DiscoverTagResp
import com.crow.module_discover.model.resp.home.DiscoverHomeResult
import com.crow.module_discover.model.source.DiscoverHomeDataSource
import com.crow.module_discover.network.DiscoverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discovery/src/main/kotlin/com/crow/module_discovery
 * @Time: 2023/3/9 13:24
 * @Author: CrowForKotlin
 * @Description: DiscoveryViewModel
 * @formatter:on
 **************************/
class DiscoverViewModel(val repository: DiscoverRepository) : BaseMviViewModel<DiscoverIntent>() {

    // 暴露的 发现主页流
    var mDiscoverHomeFlowPager : Flow<PagingData<DiscoverHomeResult>>? = null

    // 标签数据
    private var mTagResp: DiscoverTagResp? = null

    // 排序方式
    private var mOrder: String = "-datetime_updated"

    private fun getTag(intent: DiscoverIntent.GetTag) {
        flowResult(intent, repository.getTag()) { value ->
            mTagResp = value.mResults
            intent.copy(tagResp = value.mResults)
        }
    }

    private fun getHome(intent: DiscoverIntent.GetHome): Flow<PagingData<DiscoverHomeResult>>? {
        mDiscoverHomeFlowPager = Pager(
            config = PagingConfig (
                pageSize = 30,
                initialLoadSize = 30,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                DiscoverHomeDataSource { position, pageSize ->
                    flowResult(repository.getHome(position, pageSize, mOrder), intent) { value -> intent.copy(homeResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
        return mDiscoverHomeFlowPager
    }

    override fun dispatcher(intent: DiscoverIntent) {
        when(intent) {
            is DiscoverIntent.GetTag -> getTag(intent)
            is DiscoverIntent.GetHome -> getHome(intent)
        }
    }
}