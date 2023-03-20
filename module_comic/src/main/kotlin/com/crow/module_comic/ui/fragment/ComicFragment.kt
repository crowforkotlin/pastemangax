package com.crow.module_comic.ui.fragment

import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_comic.databinding.ComicFragmentBinding
import com.crow.module_comic.model.intent.ComicIntent
import com.crow.module_comic.model.resp.ComicResultsResp
import com.crow.module_comic.ui.adapter.ComicRvAdapter
import com.crow.module_comic.ui.viewmodel.ComicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui
 * @Time: 2023/3/15 21:07
 * @Author: CrowForKotlin
 * @Description: ComicFragment
 * @formatter:on
 **************************/
class ComicFragment : BaseMviFragment<ComicFragmentBinding>(){

    private lateinit var mOnBackCallback: OnBackPressedCallback
    private lateinit var mComicRvAdapter: ComicRvAdapter
    private val mComicVM by lazy { requireParentFragment().viewModel<ComicViewModel>().value }

    override fun getViewBinding(inflater: LayoutInflater) = ComicFragmentBinding.inflate(inflater)

    override fun initObserver() {
        mComicVM.onOutput { intent ->
            when(intent) {
                is ComicIntent.GetComic -> {
                   intent.mViewState
                       .doOnLoading { showLoadingAnim() }
                       .doOnError { _, _ -> dismissLoadingAnim() }
                       .doOnResult { dismissLoadingAnim { showComicPage(intent.comicChapter!!) } }
                }
                else -> {}
            }
        }
    }

    private fun showComicPage(comicResultsResp: ComicResultsResp) {
        mComicRvAdapter.setData(comicResultsResp)
        mComicRvAdapter.notifyItemRangeChanged(0, mComicRvAdapter.getDataSize())
    }

    override fun initData() {
        val pathword = arguments?.getString("pathword") ?: return
        val uuid = arguments?.getString("uuid") ?: return
        mComicVM.input(ComicIntent.GetComic(pathword, uuid))
    }

    override fun initView() {
        mComicRvAdapter = ComicRvAdapter()
        mBinding.comicRv.adapter = mComicRvAdapter
        val layoutManager = LinearLayoutManager(mContext)
        mBinding.comicRv.layoutManager = layoutManager
    }

    override fun initListener() {
        mOnBackCallback = object : OnBackPressedCallback(true) { override fun handleOnBackPressed() { findNavController().navigateUp() } }
        requireActivity().onBackPressedDispatcher.addCallback(mOnBackCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mOnBackCallback.remove()
        mComicVM.clearAllData()
    }
}