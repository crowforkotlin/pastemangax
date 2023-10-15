package com.crow.module_anime.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.copymanga.BaseUserConfig
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_anime.model.AnimeIntent
import com.crow.module_anime.model.entity.AccountEntity
import com.crow.module_anime.model.req.RegReq
import com.crow.module_anime.model.resp.discover.DiscoverPageResult
import com.crow.module_anime.model.resp.login.UserLoginResp
import com.crow.module_anime.model.source.DiscoverPageDataSource
import com.crow.module_anime.network.AnimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

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

    /**
     * ● 是否登录
     *
     * ● 2023-10-15 02:31:37 周日 上午
     */
    var mIsLogin: Boolean = false
        private set

    /**
     * ● 用户数据
     *
     * ● 2023-10-15 02:40:26 周日 上午
     */
    var mAccount: AccountEntity? = null
        private set

    init {
        viewModelScope.launch {
            toTypeEntity<AccountEntity>(DataStoreAgent.DATA_USER_RELA.asyncDecode())?.also { user ->
                mAccount = user
                user.mToken?.let { token -> BaseUserConfig.HOTMANGA_TOKEN = token }
            }
        }
    }

    override fun dispatcher(intent: AnimeIntent) {
        when(intent) {
            is AnimeIntent.DiscoverPageIntent -> onDiscoverPageIntent(intent)
            is AnimeIntent.PageInfoIntent -> onPageInfoIntent(intent)
            is AnimeIntent.ChapterListIntent -> onChapterListIntent(intent)
            is AnimeIntent.RegIntent -> onRegIntent(intent)
            is AnimeIntent.LoginIntent -> onLoginIntent(intent)
            is AnimeIntent.AnimeVideoIntent -> onAnimeVideoIntent(intent)
        }
    }

    private fun onAnimeVideoIntent(intent: AnimeIntent.AnimeVideoIntent) {
        flowResult(intent, repository.getVideo(intent.pathword, intent.chapterUUID)) { value ->
            intent.copy(video = value.mResults)
        }
    }

    private fun onLoginIntent(intent: AnimeIntent.LoginIntent) {
        flowResult(intent, repository.login(intent.username, intent.password)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                mIsLogin = true
                intent.copy(user = toTypeEntity<UserLoginResp>(value.mResults)?.also { user -> BaseUserConfig.HOTMANGA_TOKEN = user.mToken })
            }
            else {
                intent.copy(failureResp = (toTypeEntity(value.mResults) ?: return@flowResult intent))
            }
        }
    }

    private fun onRegIntent(intent: AnimeIntent.RegIntent) {
        flowResult(intent, repository.reg(intent.reg)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) intent.copy(user = (toTypeEntity(value.mResults) ?: return@flowResult intent))
            else intent.copy(failureResp = (toTypeEntity(value.mResults) ?: return@flowResult intent))
        }
    }

    private fun onChapterListIntent(intent: AnimeIntent.ChapterListIntent) {
        flowResult(intent, repository.getAnimeChapterList(intent.pathword)) { resp ->
            intent.copy(chapters = resp.mResults)
        }
    }

    private fun onPageInfoIntent(intent: AnimeIntent.PageInfoIntent) {
        flowResult(intent, repository.getAnimeInfoPage(intent.pathword)) { resp ->
            intent.copy(info = resp.mResults)
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

    private fun getRange36String(): StringBuffer {
        val MAX_LENGTH = 36
        val MIN_LENGTH = 6
        val stringBuffer = StringBuffer()
        val time = System.currentTimeMillis().toString().toCharArray()
        val whichMethod = (0..1).random()
        val value = when(whichMethod) {
            0 -> {
                time.shuffle()
                time.concatToString()
            }
            1 -> time.concatToString()
            else -> error("Invalid vethod value!")
        }
        when {
            value.length > MAX_LENGTH -> repeat(MAX_LENGTH) { stringBuffer.append(value.random()) }
            value.length < MIN_LENGTH -> repeat(MAX_LENGTH) { stringBuffer.append(value.random()) }
            else -> {
                val range = MAX_LENGTH - value.length
                stringBuffer.append(value)
                repeat(range) { stringBuffer.append(value.random()) }
            }
        }
        return stringBuffer
    }

    fun setOrder(order: String) {
        mOrder = order
    }

    fun setYear(year: String) {
        mYear = year
    }

    fun genReg(): RegReq {
        val content = getRange36String().toString()
        return RegReq(
            mUsername = content,
            mPassword = content,
        )
    }
}