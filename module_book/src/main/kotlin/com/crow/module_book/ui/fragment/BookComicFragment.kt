package com.crow.module_book.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.getSpannableString
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.logMsg
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.removeWhiteSpace
import com.crow.base.tools.extensions.startActivity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.R
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicChapterResp
import com.crow.module_book.model.resp.comic_info.Status
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.ComicChapterRvAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import com.crow.base.R as baseR

class BookComicFragment : BookFragment() {

    init {
        FlowBus.with<String>(BaseEventEnum.UpdateChapter.name).register(this) {
            mBookVM.updateBookChapter(mBookVM.mComicInfoPage!!.mComic!!.mName, it, BookType.COMIC )
            mComicChapterRvAdapter?.mChapterName = it
        }
    }

    // 漫画章节Rv
    private var mComicChapterRvAdapter: ComicChapterRvAdapter? = null

    private fun showComicInfoPage() {
        val comicInfoPage = mBookVM.mComicInfoPage?.mComic ?: return

        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(comicInfoPage.mCover) { _, _, percentage, _, _ -> mBinding.bookInfoProgressText.text = AppGlideProgressFactory.getProgressString(percentage) }

        Glide.with(this)
            .load(comicInfoPage.mCover)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    mBinding.bookInfoLoading.animateFadeOut()
                    mBinding.bookInfoProgressText.animateFadeOut()
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    mBinding.bookInfoLoading.alpha = 0f
                    mBinding.bookInfoProgressText.alpha = 0f
                    NoTransition()
                }
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
        mBinding.bookInfoDesc.text = comicInfoPage.mBrief.removeWhiteSpace()
        comicInfoPage.mTheme.forEach { theme ->
            mBinding.bookInfoThemeChip.addView(Chip(mContext).also {
                it.text = theme.mName
                it.isClickable = false
            })
        }

        mBinding.bookInfoCardview.animateFadeIn()
        buttonGroupFadeIn()
    }

    private fun showChapterPage(comicChapterResp: ComicChapterResp) {

        // 添加章节选择器
        addBookChapterSlector(comicChapterResp, null)

        // 开启协程 数据交给适配器去做出调整
        viewLifecycleOwner.lifecycleScope.launch {
            mComicChapterRvAdapter?.doNotify(comicChapterResp.mList.toMutableList())
        }

        /*
        * 观察书页章节实体 写在这里观察是因为，防止在获取数据前未得到comicInfoPage从而导致剩下逻辑不生效
        * 1：为空退出
        * 2：获取已读章节数据
        * 3：根据章节是否为空 设置适配器的Item
        * 4：根据Token是否空（登录状态）获取对应的章节名称
        * 5：未登录 -> 本地数据 ，已登录 -> 获取历史记录后会给适配器设置已读章节，当获取失败时， 设置状态false（代表历史记录可能还在请求中，或者是请求失败了）
        * */
        mBookVM.bookChapterEntity.onCollect(this) { chapters ->
            if (chapters == null) return@onCollect

            val comic = mBookVM.mComicInfoPage?.mComic ?: return@onCollect

            val chapter = chapters.datas[comic.mName]
            if (chapter == null) {
                mComicChapterRvAdapter?.mChapterName = mComicChapterRvAdapter?.mChapterName
                mComicChapterRvAdapter?.notifyItemRangeChanged(0, mComicChapterRvAdapter?.itemCount ?: return@onCollect)
            } else {
                mComicChapterRvAdapter?.mChapterName = if(BaseUser.CURRENT_USER_TOKEN.isEmpty()) chapter.bookChapterName else {
                    mComicChapterRvAdapter?.mChapterName ?: run {
                        mBaseEvent.setBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED, false)
                        return@onCollect
                    }
                }
                mComicChapterRvAdapter?.notifyItemRangeChanged(0, mComicChapterRvAdapter?.itemCount ?: return@onCollect)
            }
        }
    }

    private fun processAddComicIntent(intent: BookIntent.AddComicToBookshelf) {
        intent.mBaseViewState
            .doOnLoading { showLoadingAnim() }
            .doOnError { _, _ -> dismissLoadingAnim { toast(getString(baseR.string.BaseUnknowError)) } }
            .doOnResult {
                dismissLoadingAnim {
                    if (intent.isCollect == 1) {
                        toast(getString(R.string.book_add_success))
                        setButtonRemoveFromBookshelf()
                    } else {
                        toast(getString(R.string.book_remove_success))
                        setButtonAddToBookshelf()
                    }
                }
            }
    }

    override fun onInitData() {


        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mBookVM.input(BookIntent.GetComicBrowserHistory(mPathword))

        if (mBookVM.mComicInfoPage == null) mBookVM.input(BookIntent.GetComicInfoPage(mPathword))
    }

    override fun onRefresh() { mBookVM.input(BookIntent.GetComicChapter(mPathword)) }

    override fun initView(bundle: Bundle?) {

        // 初始化父View
        super.initView(bundle)

        if (mBookVM.mComicInfoPage != null) { showComicInfoPage() }

        // 漫画
        mComicChapterRvAdapter = ComicChapterRvAdapter { comic  ->

            mContext.startActivity<ComicActivity> {
                putExtra(BaseStrings.PATH_WORD, comic.comicPathWord)
                putExtra(BaseStrings.UUID, comic.uuid)
            }
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mComicChapterRvAdapter?.mChapterName = comic.name
        }

        // 设置适配器
        mBinding.bookInfoRvChapter.adapter = mComicChapterRvAdapter!!
    }

    override fun initObserver() {
        super.initObserver()

        mBookVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicChapter -> doOnBookPageChapterIntent<ComicChapterResp>(intent) { showChapterPage(it) }
                is BookIntent.GetComicInfoPage -> doOnBookPageIntent(intent) { showComicInfoPage() }
                is BookIntent.GetComicBrowserHistory -> {
                    intent.mBaseViewState
                        .doOnResult {
                            intent.comicBrowser!!.apply {
                                if (mCollectId == null) setButtonAddToBookshelf() else setButtonRemoveFromBookshelf()
                                mComicChapterRvAdapter?.mChapterName = mBrowse?.chapterName ?: return@doOnResult
                                if (mBaseEvent.getBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED) == false) {
                                    mBaseEvent.setBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED, true)
                                    mComicChapterRvAdapter?.notifyItemRangeChanged(0, mComicChapterRvAdapter?.itemCount ?: return@doOnResult)
                                }
                                toast(getString(R.string.BookComicReadedPage, mComicChapterRvAdapter?.mChapterName))
                            }
                         }
                }
                is BookIntent.AddComicToBookshelf -> { processAddComicIntent(intent) }
            }
        }
    }

    override fun initListener() {
        super.initListener()

        mBinding.bookInfoCardview.doOnClickInterval {
            val fragment = get<Fragment>(named(Fragments.Image.name))
            fragment.arguments =Bundle().also { bundle -> bundle.putString(BaseStrings.IMAGE_URL, mBookVM.mComicInfoPage?.mComic?.mCover) }
            navigateImage(fragment)
        }

        mBinding.bookInfoAddToBookshelf.doOnClickInterval{
            if (BaseUser.CURRENT_USER_TOKEN.isEmpty()) {
                toast(getString(R.string.book_add_invalid))
                return@doOnClickInterval
            }
            mBookVM.input(BookIntent.AddComicToBookshelf(mBookVM.mUuid ?: return@doOnClickInterval, if (mBinding.bookInfoAddToBookshelf.text == getString(R.string.book_comic_add_to_bookshelf)) 1 else 0))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 漫画适配器置空
        mComicChapterRvAdapter = null
    }
}