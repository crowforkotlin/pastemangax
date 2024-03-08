package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStriptRvAdapter
import com.crow.module_book.ui.adapter.comic.reader.layoutmanager.LoopLayoutManager
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR

class ComicPageFragment1123123 : BaseMviFragment<BookFragmentComicBinding>() {

    /**
     * ⦁ 漫画VM
     *
     * ⦁ 2023-09-01 22:22:54 周五 下午
     */
    private val mVM by activityViewModel<ComicViewModel>()

    /**
     * ⦁ 漫画RV
     *
     * ⦁ 2023-09-04 21:56:28 周一 下午
     */
    private var mAdapter: ComicStriptRvAdapter? = null

    /**
     * ⦁ 获取VB
     *
     * ⦁ 2023-09-04 21:56:47 周一 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBinding.inflate(inflater)

    /**
     * ⦁ 初始化视图
     *
     * ⦁ 2023-09-04 21:56:53 周一 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        mBinding.list.adapter = mAdapter
        mBinding.list.layoutManager = LoopLayoutManager(requireActivity() as ComicActivity)

        // 显示漫画页
        // showComicPage(mComicVM.mComicPage ?: return)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = ComicStriptRvAdapter { uuid, isNext ->
            launchDelay(BASE_ANIM_300L) { mVM.input(BookIntent.GetComicPage(mVM.mPathword, uuid, isNext)) }
        }
        super.onViewCreated(view, savedInstanceState)
    }
    
    /**
     * ⦁ 初始化监听器
     *
     * ⦁ 2023-09-04 21:56:59 周一 下午
     */
    override fun initListener() {
        mBinding.list.setOnScrollChangeListener { _, _, _, _, _ ->
            val reader = mVM.mContent.value
            /*mVM.updateUiState(ReaderState(
                mReaderContent = reader,
                mTotalPages = reader.mPages.size + 2,
                mCurrentPage = (mBinding.comicRv.layoutManager as LoopLayoutManager).findLastVisibleItemPosition() + 1
            ))*/
        }
    }

    /**
     * ⦁ Lifecycle OnPause Stop Rv Scroll
     *
     * ⦁ 2023-11-05 02:28:14 周日 上午
     * @author crowforkotlin
     */
    override fun onPause() {
        super.onPause()
        mBinding.list.stopScroll()
    }

    /**
     * ⦁ 初始化观察者
     *
     * ⦁ 2023-11-05 02:28:34 周日 上午
     * @author crowforkotlin
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mVM.mContent.onCollect(this) { reader ->
            if(reader.mPages.isNotEmpty()) {
//                showComicPage(reader)
            }
        }
    }


    private fun onErrorComicPage() {
        toast(getString(baseR.string.base_loading_error))
        BaseEvent.getSIngleInstance().setBoolean(InfoFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}