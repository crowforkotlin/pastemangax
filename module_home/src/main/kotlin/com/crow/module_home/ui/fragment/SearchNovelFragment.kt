package com.crow.module_home.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.logError
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.removeWhiteSpace
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentSearchNovelBinding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.ui.adapter.SearchNovelRvAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.search.SearchView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchNovelFragment : BaseMviFragment<HomeFragmentSearchNovelBinding>() {

    companion object {
        fun newInstance( mSearchView: SearchView,  mOnTap: (pathword: String) ->Unit): SearchNovelFragment {
            val searchNovelFragment = SearchNovelFragment()
            searchNovelFragment.mSearchView = mSearchView
            searchNovelFragment.mOnTap = mOnTap
            return searchNovelFragment
        }
    }

    private var mSearchView: SearchView? = null
    private var mOnTap: ((pathword: String) -> Unit)? = null

    private val mHomeVM by viewModel<HomeViewModel>()

    private val mBaseEvent = BaseEvent.getSIngleInstance()

    private var mNovelRvAdapter = SearchNovelRvAdapter { mOnTap?.invoke(it.mPathWord) }

    fun doInputSearchNovelIntent() {

        val keyword = mSearchView?.text.toString().removeWhiteSpace().ifEmpty {
            toast(getString(R.string.home_keyword_not_null))
            return
        }

        if (mBaseEvent.getBoolean(HomeFragment.SEARCH_TAG) == true) return

        mHomeVM.input(HomeIntent.SearchNovel(keyword, when(mBinding.homeSearchNovelChipGroup.checkedChipId) {
            mBinding.homeSearchNovelChipAll.id -> ""
            mBinding.homeSearchNovelChipName.id -> "name"
            mBinding.homeSearchNovelChipAuthor.id -> "author"
            else -> ""
        }))
        mBaseEvent.setBoolean(HomeFragment.SEARCH_TAG, true)
        repeatOnLifecycle {
            mHomeVM.mNovelSearchFlowPage?.onCollect(this) {
                mNovelRvAdapter.submitData(it)
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentSearchNovelBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {
        mBinding.homeSearchNovelRv.adapter = mNovelRvAdapter
    }

    override fun initListener() {

        // 选中标签事件
        mBinding.homeSearchNovelChipGroup.setOnCheckedStateChangeListener { _, _ ->

            // 1秒间隔处理
            mBaseEvent.doOnInterval {

                // 淡入
                when(mBinding.homeSearchNovelChipGroup.checkedChipId) {
                    mBinding.homeSearchNovelChipAll.id -> mBinding.homeSearchNovelChipAll.animateFadeIn(
                        BASE_ANIM_300L
                    )
                    mBinding.homeSearchNovelChipName.id -> mBinding.homeSearchNovelChipName.animateFadeIn(
                        BASE_ANIM_300L
                    )
                    mBinding.homeSearchNovelChipAuthor.id -> mBinding.homeSearchNovelChipAuthor.animateFadeIn(
                        BASE_ANIM_300L
                    )
                }

                // 搜索内容不为空
                if(!mSearchView?.text?.toString().isNullOrEmpty()) doInputSearchNovelIntent()
            }
        }
    }

    override fun initObserver() {


        mHomeVM.onOutput { intent ->
            if (intent is HomeIntent.SearchNovel) {
                val mTag = mBaseEvent.getBoolean(HomeFragment.SEARCH_TAG) ?: false
                intent.mBaseViewState
                    .doOnLoading { if(mTag) showLoadingAnim() }
                    .doOnError { _, msg ->
                        dismissLoadingAnim()
                        msg?.logError()
                        toast(getString(com.crow.base.R.string.BaseUnknowError))
                    }
                    .doOnSuccess { mBaseEvent.setBoolean(HomeFragment.SEARCH_TAG, false) }
                    .doOnResult {
                        if (!mTag) return@doOnResult
                        if (intent.searchNovelResp!!.mTotal == 0) {
                            if (!mBinding.homeSearchNovelTips.isVisible) {
                                mBinding.homeSearchNovelRv.animateFadeOutWithEndInVisibility()
                                mBinding.homeSearchNovelTips.animateFadeIn()
                            }
                            dismissLoadingAnim()
                            return@doOnResult
                        } else {
                            if (mBinding.homeSearchNovelTips.isVisible) {
                                mBinding.homeSearchNovelRv.animateFadeIn()
                                mBinding.homeSearchNovelTips.animateFadeOut().withEndAction {
                                    mBinding.homeSearchNovelTips.isInvisible = true
                                    mBinding.homeSearchNovelTips.text = getString(R.string.home_saerch_null_result)
                                }
                            }
                        }
                        mHandler.postDelayed({
                            dismissLoadingAnim()
                            mBinding.homeSearchNovelRv.smoothScrollToPosition(0)
                        },  BASE_ANIM_300L)
                    }
            }
        }
    }
}