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
import com.crow.module_book.model.resp.ComicChapterResp
import com.crow.module_book.model.resp.comic_chapter.ComicChapterResult
import com.crow.module_book.model.resp.comic_info.Status
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.ComicChapterRvAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class BookComicFragment : BookFragment() {

    // 漫画章节Rv
    private var mComicChapterRvAdapter: ComicChapterRvAdapter? = null

    private fun showComicInfoPage() {
        val comicInfoPage = mBookVM.mComicInfoPage?.mComic ?: return

        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(comicInfoPage.mCover) { _, _, percentage, _, _ -> mBinding.bookInfoProgressText.text = AppGlideProgressFactory.getProgressString(percentage) }

        Glide.with(this)
            .load(comicInfoPage.mCover)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { _, _ ->
                mBinding.bookInfoLoading.animateFadeOut().withEndAction { mBinding.bookInfoLoading.alpha = 1f }
                mBinding.bookInfoProgressText.animateFadeOut().withEndAction { mBinding.bookInfoProgressText.alpha = 1f }
                DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
            })
            .into(mBinding.bookInfoImage)

        mBinding.bookInfoAuthor.text = getString(R.string.BookComicAuthor, comicInfoPage.mAuthor.joinToString { it.mName })
        mBinding.bookInfoHot.text = getString(R.string.BookComicHot, formatValue(comicInfoPage.mPopular))
        mBinding.bookInfoUpdate.text = getString(R.string.BookComicUpdate, comicInfoPage.mDatetimeUpdated)
        mBinding.bookInfoNewChapter.text = getString(R.string.BookComicNewChapter, comicInfoPage.mLastChapter.mName)
        mBinding.bookInfoStatus.text = when (comicInfoPage.mStatus.mValue) {
            Status.LOADING -> getString(R.string.BookComicStatus, comicInfoPage.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.book_green), 3)
            Status.FINISH -> getString(R.string.BookComicStatus, comicInfoPage.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.book_red), 3)
            else -> null
        }
        mBinding.bookInfoName.text = comicInfoPage.mName
        mBinding.bookInfoDesc.text = comicInfoPage.mBrief
        comicInfoPage.mTheme.forEach { theme ->
            mBinding.bookInfoThemeChip.addView(Chip(mContext).also {
                it.text = theme.mName
                it.isClickable = false
            })
        }
    }

    private fun showChapterPage(comicChapterResp: ComicChapterResp) {
        // 添加章节选择器
        addBookChapterSlector(comicChapterResp, null)

        // 开启协程 数据交给适配器去做出调整
        viewLifecycleOwner.lifecycleScope.launch {
            mComicChapterRvAdapter?.doNotify(comicChapterResp.mList.toMutableList())
        }
    }

    override fun onChapter() {
        if (mChapterName != null) {
            mComicChapterRvAdapter?.mChapterName = mChapterName
            mComicChapterRvAdapter?.notifyItemRangeChanged(0, mComicChapterRvAdapter?.itemCount ?: return)
        }
    }

    override fun onInitData() {

        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mBookVM.input(BookIntent.GetComicBrowserHistory(mPathword))

        mBookVM.input(BookIntent.GetComicInfo(mPathword))
    }

    override fun onRefresh() { mBookVM.input(BookIntent.GetComicChapter(mPathword)) }

    override fun initView(bundle: Bundle?) {

        // 初始化父View
        super.initView(bundle)

        // 漫画
        mComicChapterRvAdapter = ComicChapterRvAdapter { pos, comic: ComicChapterResult ->

            // 设置章节名称 用于下次返回重建View时让adapter定位到已读章节名称
            mChapterName = comic.name
            mPos = pos

            ComicActivity.newInstance(mContext, comic.comicPathWord, comic.uuid)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // 设置适配器
        mBinding.bookInfoRvChapter.adapter = mComicChapterRvAdapter!!
    }

    override fun initObserver() {
        super.initObserver()
        mBookVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicChapter -> doOnBookPageChapterIntent<ComicChapterResp>(intent) { showChapterPage(it) }
                is BookIntent.GetComicInfo -> doOnBookPageIntent(intent) { showComicInfoPage() }
                is BookIntent.GetComicBrowserHistory -> {
                    intent.mViewState.doOnResult {
                        mChapterName = intent.comicBrowser?.mBrowse?.chapterName ?: return@doOnResult
                        mComicChapterRvAdapter?.mChapterName = mChapterName
                        toast(getString(R.string.BookComicReadedPage, mComicChapterRvAdapter?.mChapterName))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 轻小说适配器置空
        mComicChapterRvAdapter = null
    }
}