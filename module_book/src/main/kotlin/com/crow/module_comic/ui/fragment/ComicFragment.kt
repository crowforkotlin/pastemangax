package com.crow.module_comic.ui.fragment

import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.databinding.BookComicFragmentBinding
import com.crow.module_comic.model.intent.BookIntent
import com.crow.module_comic.model.resp.ComicPageResp
import com.crow.module_comic.ui.adapter.ComicRvAdapter
import com.crow.module_comic.ui.viewmodel.BookInfoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui
 * @Time: 2023/3/15 21:07
 * @Author: CrowForKotlin
 * @Description: ComicFragment
 * @formatter:on
 **************************/
class ComicFragment : BaseMviFragment<BookComicFragmentBinding>(){

    private lateinit var mOnBackCallback: OnBackPressedCallback
    private lateinit var mComicRvAdapter: ComicRvAdapter
    private val mComicVM by lazy { requireParentFragment().viewModel<BookInfoViewModel>().value }

    override fun getViewBinding(inflater: LayoutInflater) = BookComicFragmentBinding.inflate(inflater)

    override fun initObserver() {
        mComicVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                   intent.mViewState
                       .doOnLoading { showLoadingAnim() }
                       .doOnError { _, _ -> dismissLoadingAnim() }
                       .doOnResult { dismissLoadingAnim { showComicPage(intent.comicPage!!) } }
                }
                else -> {}
            }
        }
    }

    private fun showComicPage(comicPageResp: ComicPageResp) {
        mComicRvAdapter.setData(comicPageResp)
        mComicRvAdapter.notifyItemRangeChanged(0, mComicRvAdapter.getDataSize())
    }

    override fun initData() {
        val pathword = arguments?.getString("pathword") ?: return
        val uuid = arguments?.getString("uuid") ?: return
        mComicVM.input(BookIntent.GetComicPage(pathword, uuid))
    }

    override fun initView() {
        mComicRvAdapter = ComicRvAdapter()
        mBinding.comicRv.adapter = mComicRvAdapter
        val layoutManager = LinearLayoutManager(mContext)
        mBinding.comicRv.layoutManager = layoutManager
    }

    override fun initListener() {
        mOnBackCallback = object : OnBackPressedCallback(true) { override fun handleOnBackPressed() {
            requireActivity().findNavController(
            com.crow.base.R.id.app_main_fcv).navigateUp() } }
        requireActivity().onBackPressedDispatcher.addCallback(mOnBackCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mOnBackCallback.remove()
        mComicVM.clearAllData()
    }
}