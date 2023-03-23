package com.crow.module_comic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.crow.base.current_project.*
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.logMsg
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviBottomSheetDF
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_comic.R
import com.crow.module_comic.databinding.ComicFragmentInfoBinding
import com.crow.module_comic.model.intent.ComicIntent
import com.crow.module_comic.model.resp.ChapterResultsResp
import com.crow.module_comic.model.resp.comic_info.Status
import com.crow.module_comic.ui.adapter.ComicInfoChapterRvAdapter
import com.crow.module_comic.ui.viewmodel.ComicViewModel
import com.google.android.material.chip.Chip
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic
 * @Time: 2023/3/14 0:04
 * @Author: CrowForKotlin
 * @Description: ComicInfoFragment
 * @formatter:on
 **************************/
class ComicInfoBottomSheetFragment constructor() : BaseMviBottomSheetDF<ComicFragmentInfoBinding>() {

    constructor(pathword: String, isNeedGetComicPageInfo: Boolean) : this() {
        mPathword = pathword
        mIsNeedGetComicPageInfo = isNeedGetComicPageInfo
        mPathword.logMsg()
    }

    companion object { val TAG = this::class.java.simpleName }

    private val mComicVM by lazy { requireParentFragment().viewModel<ComicViewModel>().value }
    private var mPathword: String? = null
    private var mChapterName: String? = null
    private var mIsNeedGetComicPageInfo = false
    private lateinit var mComicChapterRvAdapter: ComicInfoChapterRvAdapter

    override fun getViewBinding(inflater: LayoutInflater) = ComicFragmentInfoBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()

        // 设置BottomSheet的 高度
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun initObserver() {
        mComicVM.onOutput { intent ->
            when (intent) {
                is ComicIntent.GetComicInfo -> {
                    intent.mViewState
                        .doOnLoading { showLoadingAnim() }
                        .doOnError { _, _ -> doOnDismissDialogByError() }
                        .doOnResult { mComicVM.input(ComicIntent.GetComicChapter(mPathword ?: return@doOnResult)) }
                }
                is ComicIntent.GetComicChapter -> {
                    intent.mViewState
                        .doOnError { _, _ -> doOnDismissDialogByError() }
                        .doOnResult {
                            dismissLoadingAnim {
                                showComicInfoPage()
                                showComicChapaterPage(intent.comicChapter!!)
                            }
                        }
                }
                is ComicIntent.GetComicBrowserHistory -> {
                    intent.mViewState.doOnResult { mComicChapterRvAdapter.mChapterName = intent.browserHistory?.browse?.chapterName }
                }
                else -> { }
            }
        }
    }

    override fun initView() {

        // 设置 漫画图的卡片 宽高
        mBinding.comicInfoCard.layoutParams.apply {
            height = getComicCardHeight()
            width = getComicCardWidth()
        }

        mComicChapterRvAdapter = if (mComicVM.mComicInfoPage?.mComic?.mPathWord.contentEquals(mPathword) && !mIsNeedGetComicPageInfo) {
            showComicInfoPage()
            ComicInfoChapterRvAdapter(mComicVM.mComicChapterPage?.list?.toMutableList() ?: mutableListOf())
        } else  {
            ComicInfoChapterRvAdapter()
        }

        mBinding.comicInfoRvChapter.adapter = mComicChapterRvAdapter

        mBinding.comicInfoName.doOnLayout { (it.layoutParams as ConstraintLayout.LayoutParams).topMargin = mBinding.comicInfoDragView.height / 2 }

    }

    override fun initListener() {
        mComicChapterRvAdapter.addListener { comic ->
            dismissAllowingStateLoss()
            findNavController().navigate(
                baseR.id.mainComicfragment,
                Bundle().also {
                    it.putString("pathword", comic.comicPathWord)
                    it.putString("uuid", comic.uuid)
                },
                NavOptions.Builder()
                    .setEnterAnim(android.R.anim.fade_in)
                    .setExitAnim(android.R.anim.fade_out)
                    .setPopEnterAnim(android.R.anim.fade_in)
                    .setPopExitAnim(android.R.anim.fade_out)
                    .build())
        }
    }

    override fun initData() {
        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mComicVM.input(ComicIntent.GetComicBrowserHistory(mPathword ?: return))
        if (mComicVM.mComicInfoPage == null || mIsNeedGetComicPageInfo) mComicVM.input(ComicIntent.GetComicInfo(mPathword ?: return))
    }

    private fun showComicInfoPage() {

        val comic = (mComicVM.mComicInfoPage ?: return).mComic ?: return

        Glide.with(this).load(comic.mCover).into(mBinding.comicInfoImage)
        mBinding.comicInfoName.text = comic.mName
        mBinding.comicInfoAuthor.text = getString(R.string.ComicAuthor, comic.mAuthor.joinToString { it.mName })
        mBinding.comicInfoHot.text = getString(R.string.ComicHot, formatValue(comic.mPopular))
        mBinding.comicInfoUpdate.text = getString(R.string.ComicUpdate, comic.mDatetimeUpdated)
        mBinding.comicInfoNewChapter.text = getString(R.string.ComicNewChapter, comic.mLastChapter.mName)
        mBinding.comicInfoStatus.text = when (comic.mStatus.mValue) {
            Status.LOADING -> getString(R.string.ComicStatus, comic.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.comic_green), 3)
            Status.FINISH -> getString(R.string.ComicStatus, comic.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.comic_red), 3)
            else -> null
        }
        mBinding.comicInfoDesc.text = comic.mBrief

        comic.mTheme.forEach { theme ->
            mBinding.comicInfoThemeChip.addView(Chip(mContext).also {
                it.text = theme.mName
                it.isClickable = false
            })
        }

        mBinding.root.animateFadeIn()
    }

    private fun showComicChapaterPage(comics: ChapterResultsResp) {
        mComicChapterRvAdapter.setData(comics.list)
        mComicChapterRvAdapter.notifyItemRangeInserted(0, mComicChapterRvAdapter.itemCount)
        mBinding.comicInfoRvChapter.animateFadeIn(BASE_ANIM_300L)
    }

    private fun doOnDismissDialogByError() {
        dismissLoadingAnim {
            toast(getString(baseR.string.BaseLoadingError))
            dismissAllowingStateLoss()
        }
    }
}