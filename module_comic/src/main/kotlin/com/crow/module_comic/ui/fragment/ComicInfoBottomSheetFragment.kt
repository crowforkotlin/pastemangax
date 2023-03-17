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
import com.crow.base.extensions.*
import com.crow.base.fragment.BaseMviBottomSheetDF
import com.crow.base.viewmodel.doOnError
import com.crow.base.viewmodel.doOnLoading
import com.crow.base.viewmodel.doOnResult
import com.crow.module_comic.R
import com.crow.module_comic.databinding.ComicFragmentInfoBinding
import com.crow.module_comic.model.intent.ComicIntent
import com.crow.module_comic.model.resp.comic_chapter.Results
import com.crow.module_comic.model.resp.comic_info.Status
import com.crow.module_comic.ui.adapter.ComicInfoChapterRvAdapter
import com.crow.module_comic.ui.viewmodel.ComicViewModel
import com.google.android.material.chip.Chip
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic
 * @Time: 2023/3/14 0:04
 * @Author: CrowForKotlin
 * @Description: ComicInfoFragment
 * @formatter:on
 **************************/
class ComicInfoBottomSheetFragment constructor() : BaseMviBottomSheetDF<ComicFragmentInfoBinding>() {

    constructor(pathword: String, isNeedLoadDataByNetwork: Boolean) : this() {
        mPathword = pathword
        mIsNeedLoadDataByNetwork = isNeedLoadDataByNetwork
        mPathword.logMsg()
    }

    companion object { val TAG = this::class.java.simpleName }

    private val mComicVM by lazy { requireParentFragment().viewModel<ComicViewModel>().value }
    private var mIsNeedLoadDataByNetwork = false
    private var mPathword: String? = null
    private lateinit var mComicChapterRvAdapter: ComicInfoChapterRvAdapter

    override fun getViewBinding(inflater: LayoutInflater) = ComicFragmentInfoBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()
        // 设置BottomSheet的 高度
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
        /* if (mComicVM.mComicInfoPage != null) {
            if (!mComicVM.mComicInfoPage!!.mResults.mComic?.mPathWord.contentEquals(mPathword)) {
                mComicVM.input(ComicIntent.GetComicInfo(mPathword ?: return))
            }
            return
        } */
    }

    override fun initObserver() {
        mComicVM.onOutput { intent ->
            when (intent) {
                is ComicIntent.GetComicInfo -> {
                    intent.mViewState
                        .doOnLoading { showLoadingAnim() }
                        .doOnError { _, _ -> doOnDismissDialogByError() }
                        .doOnResult {
                            dismissLoadingAnim {
                                showComicInfoPage()
                                mComicVM.input(ComicIntent.GetComicChapter(mPathword ?: return@dismissLoadingAnim))
                            }
                        }
                }
                is ComicIntent.GetComicChapter -> {
                    intent.mViewState
                        .doOnError { _, _ -> doOnDismissDialogByError() }
                        .doOnResult { showComicChapaterPage(intent.comicChapter!!.results) }
                }
                else -> { }
            }
        }
    }

    override fun initView() {
        mComicChapterRvAdapter = if (mComicVM.mComicInfoPage?.mResults?.mComic?.mPathWord.contentEquals(mPathword) && !mIsNeedLoadDataByNetwork) {
            showComicInfoPage()
            ComicInfoChapterRvAdapter(mComicVM.mComicChapterPage?.results)
        } else  {
            ComicInfoChapterRvAdapter()
        }
        mBinding.comicInfoName.doOnLayout { (it.layoutParams as ConstraintLayout.LayoutParams).topMargin = mBinding.comicInfoDragView.height / 2 }
        mBinding.comicInfoRvChapter.adapter = mComicChapterRvAdapter
    }

    override fun initListener() {
        mComicChapterRvAdapter.addListener { comic ->
            dismissAllowingStateLoss()
            findNavController().navigate(
                com.crow.base.R.id.mainComicfragment,
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
        if (mComicVM.mComicInfoPage != null && !mIsNeedLoadDataByNetwork) return
        mComicVM.input(ComicIntent.GetComicInfo(mPathword ?: return))
    }

    private fun showComicInfoPage() {

        val comic = (mComicVM.mComicInfoPage ?: return).mResults.mComic ?: return

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

    private fun showComicChapaterPage(comics: Results) {
        mComicChapterRvAdapter.setData(comics)
        mComicChapterRvAdapter.notifyItemRangeChanged(0, mComicChapterRvAdapter.getDataSize())
        mBinding.comicInfoRvChapter.animateFadeIn()
    }

    private fun doOnDismissDialogByError() {
        dismissLoadingAnim {
            toast("加载失败，请重试~")
            dismissAllowingStateLoss()
        }
    }

    
}
