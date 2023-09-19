package com.crow.module_home.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.app.appContext
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_home.R
import com.crow.module_home.model.entity.HomeHeader
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.Banner
import com.crow.module_home.model.resp.search.comic_reuslt.SearchComicResult
import com.crow.module_home.model.resp.search.novel_result.SearchNovelResult
import com.crow.module_home.model.source.ComicSearchDataSource
import com.crow.module_home.model.source.NovelSearchDataSource
import com.crow.module_home.network.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:11
 * @Author: CrowForKotlin
 * @Description: HomeViewModel
 * @formatter:on
 **************************/
class HomeViewModel(private val repository: HomeRepository) : BaseMviViewModel<HomeIntent>() {

    private val mNewHomeDatas: MutableList<Any> = mutableListOf()

    private val mBanners: MutableList<Banner> = mutableListOf()

    private var mRefreshStartIndex = 3

    var mComicSearchFlowPage : Flow<PagingData<SearchComicResult>>? = null
    var mNovelSearchFlowPage : Flow<PagingData<SearchNovelResult>>? = null

    /**
     * ● 获取主页Banner 快照
     *
     * ● 2023-09-17 01:06:15 周日 上午
     */
    fun getSnapshotBanner() = mBanners.toMutableList()

    /**
     * ● 获取主页数据 快照
     *
     * ● 2023-09-17 01:06:32 周日 上午
     */
    fun getSnapshotHomeData() = mNewHomeDatas.toMutableList()

    /** ● 获取主页 （返回数据量很多）*/
    private fun getHomePage(intent: HomeIntent.GetHomePage) {
        flowResult(intent, repository.getHomePage()) { value ->
            mBanners.clear()
            mNewHomeDatas.clear()
            mBanners.addAll(value.mResults.mBanners.filter { banner -> banner.mType <= 2 }.toMutableList())
            mNewHomeDatas.add(HomeHeader(R.drawable.home_ic_recommed_24dp, appContext.getString(R.string.home_recommend_comic)))
            mNewHomeDatas.addAll(value.mResults.mRecComicsResult.mResult.toMutableList())
            mNewHomeDatas.add(Unit)
            mNewHomeDatas.add(HomeHeader(R.drawable.home_ic_new_24dp, appContext.getString(R.string.home_hot_comic)))
            mNewHomeDatas.addAll(value.mResults.mHotComics.toMutableList())
            mNewHomeDatas.add(HomeHeader(R.drawable.home_ic_new_24dp, appContext.getString(R.string.home_new_comic)))
            mNewHomeDatas.addAll(value.mResults.mNewComics.toMutableList())
            mNewHomeDatas.add(HomeHeader(R.drawable.home_ic_finish_24dp, appContext.getString(R.string.home_commit_finish)))
            mNewHomeDatas.addAll(value.mResults.mFinishComicDatas.mResult.toMutableList())
            mNewHomeDatas.add(HomeHeader(R.drawable.home_ic_finish_24dp, appContext.getString(R.string.home_topic_comic)))
            value.mResults.mTopics.mResult.forEach { mNewHomeDatas.add(it) }
            intent.copy(homePageData = value)
        }
    }

    // 通过刷新的方式 获取推荐
    private fun getRecPageByRefresh(intent: HomeIntent.GetRecPageByRefresh) {
        flowResult(intent, repository.getRecPageByRefresh(3, mRefreshStartIndex)) { value ->
            mNewHomeDatas[1] = value.mResults.mResult[0]
            mNewHomeDatas[2] = value.mResults.mResult[1]
            mNewHomeDatas[3] = value.mResults.mResult[2]
            mRefreshStartIndex += 3
            intent.copy(recPageData = value)
        }
    }

    private fun doSearchComic(intent: HomeIntent.SearchComic) {
        mComicSearchFlowPage = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                ComicSearchDataSource { position, pagesize ->
                    flowResult(repository.doSearchComic(intent.keyword, intent.type, position, pagesize), intent) { value -> intent.copy(searchComicResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    private fun doSearchNovel(intent: HomeIntent.SearchNovel) {
        mNovelSearchFlowPage = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                NovelSearchDataSource { position, pagesize ->
                    flowResult(repository.doSearchNovel(intent.keyword, intent.type, position, pagesize), intent) { value -> intent.copy(searchNovelResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    override fun dispatcher(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.GetHomePage -> getHomePage(intent)
            is HomeIntent.GetRecPageByRefresh -> getRecPageByRefresh(intent)
            is HomeIntent.SearchComic -> doSearchComic(intent)
            is HomeIntent.SearchNovel -> doSearchNovel(intent)
        }
    }
}