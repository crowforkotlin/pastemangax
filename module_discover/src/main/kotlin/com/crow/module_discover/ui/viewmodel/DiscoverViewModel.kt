package com.crow.module_discover.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.model.resp.DiscoverComicHomeResp
import com.crow.module_discover.model.resp.DiscoverComicTagResp
import com.crow.module_discover.model.resp.DiscoverNovelHomeResp
import com.crow.module_discover.model.resp.DiscoverNovelTagResp
import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult
import com.crow.module_discover.model.resp.novel_home.DiscoverNovelHomeResult
import com.crow.module_discover.model.source.DiscoverComicHomeDataSource
import com.crow.module_discover.model.source.DiscoverNovelHomeDataSource
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

    var mCurrentItem : Int = 0

    // 暴露的 发现漫画主页流
    var mDiscoverComicHomeFlowPager : Flow<PagingData<DiscoverComicHomeResult>>? = null

    // 暴露的 发现轻小说主页流
    var mDiscoverNovelHomeFlowPager : Flow<PagingData<DiscoverNovelHomeResult>>? = null

    // 漫画标签数据
    private var mComicTagResp: DiscoverComicTagResp? = null

    // 轻小说标签数据
    private var mNovelTagResp: DiscoverNovelTagResp? = null

    var mComicHomeData: DiscoverComicHomeResp? = null
        private set

    var mNovelHomeData: DiscoverNovelHomeResp? = null
        private set

    /**
     * ● RecyclerView Position
     *
     * ● 2023-09-08 22:46:26 周五 下午
     */
    var mRvPos: Int = 0

    /**
     * ● RecyclerView 可见的ChildView 个数
     *
     * ● 2023-09-09 01:53:44 周六 上午
     */
    var mVisiblePos: Int? = null

    // 排序方式
    private var mOrder: String = "-datetime_updated"

    private fun getComicTag(intent: DiscoverIntent.GetComicTag) {
        flowResult(intent, repository.getComicTag()) { value ->
            mComicTagResp = value.mResults
            intent.copy(comicTagResp = value.mResults)
        }
    }

    private fun getNovelTag(intent: DiscoverIntent.GetNovelTag) {
        flowResult(intent, repository.getNovelTag()) { value ->
            mNovelTagResp = value.mResults
            intent.copy(novelTagResp = value.mResults)
        }
    }

    private fun getComicHome(intent: DiscoverIntent.GetComicHome) {
        mDiscoverComicHomeFlowPager = Pager(
            config = PagingConfig (
                pageSize = 30,
                initialLoadSize = 30,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                DiscoverComicHomeDataSource { position, pageSize ->
                    mComicHomeData = flowResult(repository.getComicHome(position, pageSize, mOrder), intent) { value -> intent.copy(comicHomeResp = value.mResults) }.mResults
                    mComicHomeData
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    private fun getNovelHome(intent: DiscoverIntent.GetNovelHome) {
        mDiscoverNovelHomeFlowPager = Pager(
            config = PagingConfig (
                pageSize = 30,
                initialLoadSize = 30,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                DiscoverNovelHomeDataSource { position, pageSize ->
                    mNovelHomeData = flowResult(repository.getNovelHome(position, pageSize, mOrder), intent) { value -> intent.copy(novelHomeResp = value.mResults) }.mResults
                    mNovelHomeData
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    override fun dispatcher(intent: DiscoverIntent) {
        when(intent) {
            is DiscoverIntent.GetComicTag -> getComicTag(intent)
            is DiscoverIntent.GetComicHome -> getComicHome(intent)
            is DiscoverIntent.GetNovelTag -> getNovelTag(intent)
            is DiscoverIntent.GetNovelHome -> getNovelHome(intent)
        }
    }
}