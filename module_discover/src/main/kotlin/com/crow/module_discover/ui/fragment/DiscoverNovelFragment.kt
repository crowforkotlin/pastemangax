package com.crow.module_discover.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.current_project.BaseLoadStateAdapter
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.current_project.entity.BookType
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentNovelBinding
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.ui.adapter.DiscoverNovelAdapter
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/ui/fragment
 * @Time: 2023/3/28 23:56
 * @Author: CrowForKotlin
 * @Description: DiscoverComicFragment
 * @formatter:on
 **************************/
class DiscoverNovelFragment : BaseMviFragment<DiscoverFragmentNovelBinding>() {

    // 共享 发现VM
    private val mDiscoverVM by viewModel<DiscoverViewModel>()

    // 轻小说适配器
    private lateinit var mDiscoverNovelAdapter: DiscoverNovelAdapter

    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentNovelBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDiscoverVM.input(DiscoverIntent.GetNovelHome())    // 获取发现主页
    }

    override fun initListener() {

        // 刷新 发送获取轻小说主页意图
        mBinding.discoverNovelRefresh.setOnRefreshListener { mDiscoverNovelAdapter.refresh() }

        // 滑动 同时更新text
        mBinding.discoverNovelRv.setOnScrollChangeListener { _, _, _, _, _ ->
            val layoutManager = mBinding.discoverNovelRv.layoutManager
            if(layoutManager is LinearLayoutManager) mBinding.discoverNovelAppbar.discoverAppbarTextPos.text = getString(R.string.discover_comic_count, layoutManager.findLastVisibleItemPosition() + 1)
        }
    }

    override fun initView(bundle: Bundle?) {

        // 初始化适配器
        mDiscoverNovelAdapter = DiscoverNovelAdapter { FlowBus.with<BookTapEntity>(BaseStrings.Key.OPEN_BOOK_INFO).post(lifecycleScope, BookTapEntity(BookType.Novel, it.mPathWord)) }

        // 设置Rv适配器 添加页脚 回调则重试
        mBinding.discoverNovelRv.adapter = mDiscoverNovelAdapter.withLoadStateFooter(BaseLoadStateAdapter { mDiscoverNovelAdapter.retry() })

        // 设置加载动画独占1行，轻小说卡片3行
        (mBinding.discoverNovelRv.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() { override fun getSpanSize(position: Int) = if (position == mDiscoverNovelAdapter.itemCount  && mDiscoverNovelAdapter.itemCount > 0) 3 else 1 }
    }

    override fun initObserver() {

        // 意图观察者
        mDiscoverVM.onOutput { intent ->
            when(intent) {
                is DiscoverIntent.GetNovelHome -> {
                    intent.mViewState
                        .doOnError { code, msg ->

                            // 解析地址失败 且 Resumed的状态才提示
                            if (code == ViewState.Error.UNKNOW_HOST && isResumed) mBinding.root.showSnackBar(msg ?: getString(com.crow.base.R.string.BaseLoadingError))
                            if (mDiscoverNovelAdapter.itemCount == 0) {
                                if (mDiscoverVM.mCurrentItem == 1) {
                                    mBinding.discoverNovelTipsError.animateFadeIn()
                                    mBinding.discoverNovelAppbar.discoverAppbarTagText.animateFadeOut().withEndAction { mBinding.discoverNovelAppbar.discoverAppbarTagText.isInvisible = true }
                                    mBinding.discoverNovelRv.animateFadeOut().withEndAction { mBinding.discoverNovelRv.isInvisible = true }
                                    mBinding.discoverNovelAppbar.discoverAppbarTextPos.animateFadeOut().withEndAction { mBinding.discoverNovelRv.isInvisible = true }
                                } else {
                                    mBinding.discoverNovelTipsError.isVisible = true
                                    mBinding.discoverNovelRv.isInvisible = true
                                    mBinding.discoverNovelAppbar.discoverAppbarTagText.isInvisible = true
                                    mBinding.discoverNovelAppbar.discoverAppbarTextPos.isInvisible = true
                                }
                            }
                        }
                        .doOnResult {
                            // 错误提示 可见
                            if (mBinding.discoverNovelTipsError.isVisible) {
                                mBinding.discoverNovelAppbar.discoverAppbarTagText.text = "全部 — 全部 （${intent.novelHomeResp!!.mTotal}）"

                                // 若VP 显示的是当前页 则动画淡入 否则直接显示
                                if (mDiscoverVM.mCurrentItem == 1) {
                                    mBinding.discoverNovelTipsError.animateFadeOut().withEndAction { mBinding.discoverNovelTipsError.isVisible = false }
                                    mBinding.discoverNovelAppbar.discoverAppbarTagText.animateFadeIn()
                                    mBinding.discoverNovelAppbar.discoverAppbarTextPos.animateFadeIn()
                                    mBinding.discoverNovelRv.animateFadeIn()
                                } else {
                                    mBinding.discoverNovelTipsError.isVisible = false
                                    mBinding.discoverNovelAppbar.discoverAppbarTagText.isVisible = true
                                    mBinding.discoverNovelAppbar.discoverAppbarTextPos.isVisible = true
                                    mBinding.discoverNovelRv.isVisible = true
                                }
                            }
                        }
                        .doOnSuccess { if (mBinding.discoverNovelRefresh.isRefreshing) mBinding.discoverNovelRefresh.finishRefresh() }
                }
                else -> {}
            }
        }

        // 收集状态 通知适配器
        repeatOnLifecycle { mDiscoverVM.mDiscoverNovelHomeFlowPager?.collect { mDiscoverNovelAdapter.submitData(it) } }
    }
}