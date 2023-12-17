
package com.crow.module_home.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.crow.mangax.copymanga.BaseLoadStateAdapter
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentSearchComicBinding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.ui.adapter.SearchComicRvAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.search.SearchView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchComicFragment : BaseMviFragment<HomeFragmentSearchComicBinding>() {

    companion object {
        fun newInstance( mSearchView: SearchView,  mOnTap: (name: String, pathword: String) ->Unit): SearchComicFragment {
            val comicFragment = SearchComicFragment()
            comicFragment.mSearchView = mSearchView
            comicFragment.mOnTap = mOnTap
            return comicFragment
        }
    }

    private var mSearchView: SearchView? = null

    private var mOnTap: ((name: String, pathword: String) -> Unit)? = null

    private val mHomeVM by viewModel<HomeViewModel>()

    private val mBaseEvent = BaseEvent.newInstance()

    private var mComicRvAdapter = SearchComicRvAdapter { mOnTap?.invoke(it.mName,  it.mPathWord) }

    fun doInputSearchComicIntent() {

        val keyword = mSearchView?.text.toString().ifEmpty {
            mBinding.homeSearchComicTips.text = getString(R.string.home_search_tips)
            if (mBinding.homeSearchComicRv.isVisible) mBinding.homeSearchComicRv.animateFadeOutWithEndInVisibility()
            mBinding.homeSearchComicTips.animateFadeIn()
            return
        }

        if (mBaseEvent.getBoolean(NewHomeFragment.SEARCH_TAG) == true) return
        mHomeVM.input(HomeIntent.SearchComic(keyword, when(mBinding.homeSearchComicChipGroup.checkedChipId) {
            mBinding.homeSearchComicChipAll.id -> ""
            mBinding.homeSearchComicChipName.id -> "name"
            mBinding.homeSearchComicChipAuthor.id -> "author"
            mBinding.homeSearchComicChipLocal.id -> "local"
            else -> ""
        }))
        mBaseEvent.setBoolean(NewHomeFragment.SEARCH_TAG, true)
        repeatOnLifecycle {
            mHomeVM.mComicSearchFlowPage?.onCollect(this) {
                mComicRvAdapter.submitData(it)
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentSearchComicBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {

        // 设置适配器
        mBinding.homeSearchComicRv.adapter = mComicRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mComicRvAdapter.retry() })

        // 设置加载动画独占1行，漫画卡片3行
        (mBinding.homeSearchComicRv.layoutManager as GridLayoutManager).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == mComicRvAdapter.itemCount  && mComicRvAdapter.itemCount > 0) 3
                    else 1
                }
            }
        }
    }

    override fun initListener() {

        // 选中标签事件
        mBinding.homeSearchComicChipGroup.setOnCheckedStateChangeListener { _, _ ->

            mBaseEvent.setBoolean("ChipChecked", true)

            // 1秒间隔处理
            mBaseEvent.doOnInterval {

                // 淡入
                when(mBinding.homeSearchComicChipGroup.checkedChipId) {
                    mBinding.homeSearchComicChipAll.id -> mBinding.homeSearchComicChipAll.animateFadeIn(BASE_ANIM_300L)
                    mBinding.homeSearchComicChipName.id -> mBinding.homeSearchComicChipName.animateFadeIn(BASE_ANIM_300L)
                    mBinding.homeSearchComicChipAuthor.id -> mBinding.homeSearchComicChipAuthor.animateFadeIn(BASE_ANIM_300L)
                    mBinding.homeSearchComicChipLocal.id -> mBinding.homeSearchComicChipLocal.animateFadeIn(BASE_ANIM_300L)
                }

                doInputSearchComicIntent()
            }
        }
    }

    override fun initObserver(saveInstanceState: Bundle?) {

        mHomeVM.onOutput { intent ->
            val mTag = mBaseEvent.getBoolean(NewHomeFragment.SEARCH_TAG) ?: false
            when(intent) {
                is HomeIntent.SearchComic -> {
                    intent.mViewState
                        .doOnLoading { if(mTag) showLoadingAnim() }
                        .doOnSuccess { mBaseEvent.setBoolean(NewHomeFragment.SEARCH_TAG, false) }
                        .doOnError { _, _ ->
                            dismissLoadingAnim()
                            toast(getString(com.crow.base.R.string.BaseUnknowError))
                        }
                        .doOnResult {
                            if (!mTag) return@doOnResult
                            dismissLoadingAnim()
                            if (intent.searchComicResp!!.mTotal == 0) {
                                if (mBinding.homeSearchComicTips.isGone) {
                                    mBinding.homeSearchComicRv.animateFadeOutWithEndInVisibility()
                                    mBinding.homeSearchComicTips.animateFadeIn()
                                }
                            } else {
                                if (mBinding.homeSearchComicTips.isVisible) {
                                    mBinding.homeSearchComicRv.animateFadeIn()
                                    mBinding.homeSearchComicTips.animateFadeOut().withEndAction {
                                        mBinding.homeSearchComicTips.isGone = true
                                        mBinding.homeSearchComicTips.text = getString(R.string.home_saerch_null_result)
                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}