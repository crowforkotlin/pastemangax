package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import com.crow.base.R
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStriptRvAdapter
import com.crow.module_book.ui.fragment.BookFragment
import com.crow.module_book.ui.view.comic.rv.ComicLayoutManager
import com.crow.module_book.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.fragment.comic
 * @Time: 2023/6/28 0:41
 * @Author: CrowForKotlin
 * @Description: BookStripComicFragment
 * @formatter:on
 **************************/
class BookStriptComicFragment : BaseMviFragment<BookFragmentComicBinding>() {

    private val mComicVM by sharedViewModel<ComicViewModel>()

    private val mComicStriptRvAdapter: ComicStriptRvAdapter by lazy { ComicStriptRvAdapter() }

    private val mWindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
    }


    private val mBaseEvent  = BaseEvent.newInstance()

    override fun getViewBinding(inflater: LayoutInflater) =
        BookFragmentComicBinding.inflate(inflater)

    private suspend fun showComicPage(reader: ReaderContent) = coroutineScope {
        val setItemTask = async {
            // mComicStriptRvAdapter.submitList(contents)
            yield()
        }
        if (mBinding.comicRv.isInvisible) mBinding.comicRv.animateFadeIn()
        setItemTask.await()
    }

    private fun onErrorComicPage() {
        toast(getString(R.string.BaseLoadingError))
        BaseEvent.getSIngleInstance().setBoolean(BookFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun initData(savedInstanceState: Bundle?) {
        mComicVM.input(BookIntent.GetComicPage(mComicVM.mPathword ?: return, mComicVM.mUuid ?: return))
    }

    override fun initView(savedInstanceState: Bundle?) {

        // Set LayoutManager support zoom
        mBinding.comicRv.layoutManager = ComicLayoutManager(requireActivity() as ComicActivity)

        // Set RvAdapter
        mBinding.comicRv.adapter = mComicStriptRvAdapter

        // Show ComicPage
        // showComicPage(mComicVM.mContents)
    }

    override fun initListener() {
        mBinding.comicRv.setPreScrollListener { position ->
            mComicVM.onScroll(position)
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.comicRv.stopScroll()
    }

    override fun initObserver(savedInstanceState: Bundle?) {

        /*mComicVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicPage -> {
                    intent.mBaseViewState
                        .doOnSuccess { mWindowInsetsControllerCompat.isAppearanceLightStatusBars = true }
                        .doOnError { _, _ ->
                            onErrorComicPage()
                            mBaseEvent.setBoolean("loaded", false)
                        }
                        .doOnResult {
                            viewLifecycleOwner.lifecycleScope.launch {
                                showComicPage(intent.comicpage ?: return@launch)
                            }
                        }
                }
            }
        }*/
    }
}