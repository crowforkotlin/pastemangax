package com.crow.module_book.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import androidx.paging.PagingData
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.mangax.copymanga.BaseLoadStateAdapter
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentInfoCommentBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.adapter.comic.ComicTotalCommentRvAdapter
import com.crow.module_book.ui.viewmodel.BookViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookInfoCommentBottomFragment : BaseMviBottomSheetDialogFragment<BookFragmentInfoCommentBinding>() {

    private val mVM by viewModel<BookViewModel>()

    private var mAdapter: ComicTotalCommentRvAdapter? = null

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentInfoCommentBinding.inflate(layoutInflater)

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            // 配置行为
            (dialog as BottomSheetDialog).apply {
                dismissWithAnimation = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.saveFlags = BottomSheetBehavior.SAVE_ALL
            }

            // 沉浸式
            dialog.window?.let { window ->
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = ColorUtils.setAlphaComponent(Color.WHITE, 1)
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightNavigationBars = !CatlogConfig.mDarkMode
                }
            }

            // 设置BottomSheet的 高度
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
                layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams!!.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    override fun initObserver(saveInstanceState: Bundle?) {

        onClollectState()

        mVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicTotalComment -> {
                    intent.mViewState
                        .doOnSuccess { if (mBinding.refresh.isRefreshing) { launchDelay(500L) { mBinding.refresh.isRefreshing = false } } }
                        .doOnResult {
                            intent.resp?.let {
                                val total = it.mTotal ?: 0
                                if (arguments == null) { arguments = bundleOf("total" to total) }
                                else requireArguments().putInt("total", total)
                                mBinding.commentTopbar.title = getString(R.string.book_comment_comic_title_count, total)
                            }
                        }
                }
            }
        }
    }

    override fun initListener() {
        mBinding.refresh.setOnRefreshListener {
            launchDelay(BASE_ANIM_300L shl 1) { mBinding.refresh.isRefreshing = false }
            onClollectState(true)
        }

        mBinding.commentTopbar.navigateIconClickGap { dismiss() }
    }

    override fun initView(bundle: Bundle?) {
        mBinding.refresh.isRefreshing = true

        arguments?.let {
            mBinding.commentTopbar.title = getString(R.string.book_comment_comic_title_count, it.getInt("total", 0))
            it.getString(BaseStrings.NAME)?.let { title ->
                mBinding.commentTopbar.subtitle = title
            }
        }

        // 初始化适配器
        mBinding.commentList.adapter = ComicTotalCommentRvAdapter(viewLifecycleOwner.lifecycleScope) { }.run {
            mAdapter = this
            withLoadStateFooter(BaseLoadStateAdapter { retry() })
        }
    }

    override fun onDestroyView() {
        mAdapter = null
        super.onDestroyView()
    }

    private fun onClollectState(reset: Boolean = false) {
        arguments?.let {
            it.getString(BaseStrings.ID)?.let { id  ->
                lifecycleScope.launch {
                    if (reset) {
                        mVM.mComicCommentFlowPage = null
                        mAdapter?.submitData(PagingData.empty())
                    }
                    withCreated {
                        if (mVM.mComicCommentFlowPage == null) { mVM.input(BookIntent.GetComicTotalComment(id)) }
                        repeatOnLifecycle {
                            mVM.mComicCommentFlowPage?.collect { data ->
                                mAdapter?.submitData(data)
                            }
                        }
                    }
                }
            }
        }
    }
}