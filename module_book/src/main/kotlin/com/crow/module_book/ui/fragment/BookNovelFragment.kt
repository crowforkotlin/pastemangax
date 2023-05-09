package com.crow.module_book.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.getSpannableString
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.R
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.NovelChapterResp
import com.crow.module_book.model.resp.comic_info.Status
import com.crow.module_book.ui.adapter.NovelChapterRvAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class BookNovelFragment : BookFragment() {

    // 轻小说章节Rv
    private var mNovelChapterRvAdapter: NovelChapterRvAdapter? = null

    private fun showNovelInfoPage() {
        val novelInfoPage = mBookVM.mNovelInfoPage?.mNovel ?: return

        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(novelInfoPage.mCover) { _, _, percentage, _, _ -> mBinding.bookInfoProgressText.text = AppGlideProgressFactory.getProgressString(percentage) }

        Glide.with(this)
            .load(novelInfoPage.mCover)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { _, _ ->
                mBinding.bookInfoLoading.animateFadeOut().withEndAction { mBinding.bookInfoLoading.alpha = 1f }
                mBinding.bookInfoProgressText.animateFadeOut().withEndAction { mBinding.bookInfoProgressText.alpha = 1f }
                DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
            })
            .into(mBinding.bookInfoImage)

        mBinding.bookInfoAuthor.text = getString(R.string.BookComicAuthor, novelInfoPage.mAuthor.joinToString { it.mName })
        mBinding.bookInfoHot.text = getString(R.string.BookComicHot, formatValue(novelInfoPage.mPopular))
        mBinding.bookInfoUpdate.text = getString(R.string.BookComicUpdate, novelInfoPage.mDatetimeUpdated)
        mBinding.bookInfoNewChapter.text = getString(R.string.BookComicNewChapter, novelInfoPage.mLastChapter.mName)
        mBinding.bookInfoStatus.text = when (novelInfoPage.mStatus.mValue) {
            Status.LOADING -> getString(R.string.BookComicStatus, novelInfoPage.mStatus.mDisplay).getSpannableString(
                ContextCompat.getColor(mContext, R.color.book_green), 3)
            Status.FINISH -> getString(R.string.BookComicStatus, novelInfoPage.mStatus.mDisplay).getSpannableString(
                ContextCompat.getColor(mContext, R.color.book_red), 3)
            else -> null
        }
        mBinding.bookInfoName.text = novelInfoPage.mName
        mBinding.bookInfoDesc.text = novelInfoPage.mBrief
        novelInfoPage.mTheme.forEach { theme ->
            mBinding.bookInfoThemeChip.addView(Chip(mContext).also {
                it.text = theme.mName
                it.isClickable = false
            })
        }
    }

    override fun onChapter() {
        if (mChapterName != null) {
            mNovelChapterRvAdapter?.mChapterName = mChapterName
            mNovelChapterRvAdapter?.notifyItemRangeChanged(0, mNovelChapterRvAdapter?.itemCount ?: return)
        }
    }

    override fun onRefresh() { mBookVM.input(BookIntent.GetNovelChapter(mPathword)) }

    override fun onInitData() {

        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mBookVM.input(BookIntent.GetNovelBrowserHistory(mPathword))

        mBookVM.input(BookIntent.GetNovelInfo(mPathword))
    }

    private fun showChapterPage(novelChapterResp: NovelChapterResp) {
        // 添加章节选择器
        addBookChapterSlector(null, novelChapterResp)

        // 开启协程 数据交给适配器去做出调整
        viewLifecycleOwner.lifecycleScope.launch {
            mNovelChapterRvAdapter?.doNotify(novelChapterResp.mList.toMutableList())
        }
    }

    override fun initView(bundle: Bundle?) {

        // 初始化父View
        super.initView(bundle)

        // 初始化适配器
        mNovelChapterRvAdapter = NovelChapterRvAdapter { }

        // 设置适配器
        mBinding.bookInfoRvChapter.adapter = mNovelChapterRvAdapter!!

    }

    override fun initObserver() {
        super.initObserver()
        mBookVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetNovelChapter -> doOnBookPageChapterIntent<NovelChapterResp>(intent) { showChapterPage(it)}
                is BookIntent.GetNovelInfo -> doOnBookPageIntent(intent) { showNovelInfoPage() }
                is BookIntent.GetNovelBrowserHistory -> {
                    intent.mViewState.doOnResult {
                        mChapterName = intent.novelBrowser?.mBrowse?.chapterName ?: return@doOnResult
                        mNovelChapterRvAdapter?.mChapterName = mChapterName
                        toast(getString(R.string.BookComicReadedPage, mNovelChapterRvAdapter?.mChapterName))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 漫画适配器置空
        mNovelChapterRvAdapter = null
    }
}