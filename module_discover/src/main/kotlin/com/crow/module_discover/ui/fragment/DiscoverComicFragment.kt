package com.crow.module_discover.ui.fragment

import android.view.LayoutInflater
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.current_project.BaseLoadStateAdapter
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.current_project.entity.BookType
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentComicBinding
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.ui.adapter.DiscoverComicAdapter
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/ui/fragment
 * @Time: 2023/3/28 23:56
 * @Author: CrowForKotlin
 * @Description: DiscoverComicFragment
 * @formatter:on
 **************************/
class DiscoverComicFragment : BaseMviFragment<DiscoverFragmentComicBinding>() {

    // 共享 发现VM
    private val mDiscoverVM by sharedViewModel<DiscoverViewModel>()

    // 漫画适配器
    private lateinit var mDiscoverComicAdapter: DiscoverComicAdapter

    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentComicBinding.inflate(inflater)

    override fun initData() {

        // 获取标签
        mDiscoverVM.input(DiscoverIntent.GetComicTag())
    }

    override fun initListener() {
        mBinding.discoverComicRefresh.setOnRefreshListener {
            mDiscoverComicAdapter.refresh()
        }
    }

    override fun initView() {

        mDiscoverComicAdapter = DiscoverComicAdapter {
            FlowBus.with<BookTapEntity>(BaseStrings.Key.OPEN_BOOK_INFO).post(lifecycleScope, BookTapEntity(BookType.Comic, it.mPathWord))
        }
        mBinding.discoverComicRv.adapter = mDiscoverComicAdapter.withLoadStateFooter(BaseLoadStateAdapter { mDiscoverComicAdapter.retry() })
        (mBinding.discoverComicRv.layoutManager as GridLayoutManager).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == mDiscoverComicAdapter.itemCount  && mDiscoverComicAdapter.itemCount > 0) 3
                    else 1
                }
            }
        }



        mBinding.discoverComicRv.setOnScrollChangeListener { _, _, _, _, _ ->
            val layoutManager = mBinding.discoverComicRv.layoutManager
            if(layoutManager is LinearLayoutManager) {
                mBinding.discoverComicAppbar.discoverAppbarTextPos.text = getString(R.string.discover_comic_count, layoutManager.findLastVisibleItemPosition() + 1)
            }
        }
    }

    override fun initObserver() {

        // 获取发现主页
        mDiscoverVM.input(DiscoverIntent.GetComicHome())

        // 收集状态 通知适配器
        repeatOnLifecycle { mDiscoverVM.mDiscoverComicHomeFlowPager?.collect { mDiscoverComicAdapter.submitData(it) } }

        // 意图观察者
        mDiscoverVM.onOutput { intent ->
            when(intent) {
                is DiscoverIntent.GetComicHome -> {
                    intent.mViewState
                        .doOnError { code, msg ->

                            // 解析地址失败 且 Resumed的状态才提示
                            if (code == ViewState.Error.UNKNOW_HOST && isResumed) mBinding.root.showSnackBar(msg ?: getString(com.crow.base.R.string.BaseLoadingError))
                            if (mDiscoverComicAdapter.itemCount == 0) {
                                if (mDiscoverVM.mCurrentItem == 1) {
                                    mBinding.discoverComicTipsError.animateFadeIn()
                                    mBinding.discoverComicAppbar.discoverAppbarTagText.animateFadeOut().withEndAction { mBinding.discoverComicAppbar.discoverAppbarTagText.isInvisible = true }
                                    mBinding.discoverComicRv.animateFadeOut().withEndAction { mBinding.discoverComicRv.isInvisible = true }
                                    mBinding.discoverComicAppbar.discoverAppbarTextPos.animateFadeOut().withEndAction { mBinding.discoverComicRv.isInvisible = true }
                                } else {
                                    mBinding.discoverComicTipsError.isVisible = true
                                    mBinding.discoverComicRv.isInvisible = true
                                    mBinding.discoverComicAppbar.discoverAppbarTagText.isInvisible = true
                                    mBinding.discoverComicAppbar.discoverAppbarTextPos.isInvisible = true
                                }
                            }
                        }
                        .doOnResult {
                            // 错误提示 可见
                            if (mBinding.discoverComicTipsError.isVisible) {
                                mBinding.discoverComicAppbar.discoverAppbarTagText.text = "全部 — 全部 （${intent.comicHomeResp!!.mTotal}）"

                                // 若VP 显示的是当前页 则动画淡入 否则直接显示
                                if (mDiscoverVM.mCurrentItem == 1) {
                                    mBinding.discoverComicTipsError.animateFadeOut().withEndAction { mBinding.discoverComicTipsError.isVisible = false }
                                    mBinding.discoverComicAppbar.discoverAppbarTagText.animateFadeIn()
                                    mBinding.discoverComicAppbar.discoverAppbarTextPos.animateFadeIn()
                                    mBinding.discoverComicRv.animateFadeIn()
                                } else {
                                    mBinding.discoverComicTipsError.isVisible = false
                                    mBinding.discoverComicAppbar.discoverAppbarTagText.isVisible = true
                                    mBinding.discoverComicAppbar.discoverAppbarTextPos.isVisible = true
                                    mBinding.discoverComicRv.isVisible = true
                                }
                            }
                        }
                        .doOnSuccess { if (mBinding.discoverComicRefresh.isRefreshing) mBinding.discoverComicRefresh.finishRefresh() }
                }
                else -> {}
            }
        }
    }
}