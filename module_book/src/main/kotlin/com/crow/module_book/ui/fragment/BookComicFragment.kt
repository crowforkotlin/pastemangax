package com.crow.module_book.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
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
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
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

    /**
     * ● Regist FlowBus
     *
     * ● 2023-06-24 23:45:12 周六 下午
     */
    init {
        FlowBus.with<String>(BaseEventEnum.UpdateChapter.name).register(this) {
            mBookVM.updateBookChapterOnDB(mBookVM.mComicInfoPage?.mComic?.mName ?: return@register, it, BookType.COMIC)
        }
    }

    /**
     * ● 漫画章节Rv
     *
     * ● 2023-06-15 23:00:16 周四 下午
     */
    private var mComicChapterRvAdapter: ComicChapterRvAdapter? = null

    /**
     * ● 显示漫画信息页面
     *
     * ● 2023-06-15 23:00:25 周四 下午
     */
    private fun showComicInfoPage() {
        val comicInfoPage = mBookVM.mComicInfoPage?.mComic ?: return
        mBookVM.findReadedBookChapterOnDB(comicInfoPage.mName, BookType.COMIC)
        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(comicInfoPage.mCover) { _, _, percentage, _, _ -> mBinding.bookInfoProgressText.text = AppGlideProgressFactory.getProgressString(percentage) }

        Glide.with(this)
            .load(comicInfoPage.mCover)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    mBinding.bookInfoLoading.isInvisible = true
                    mBinding.bookInfoProgressText.isInvisible = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    mBinding.bookInfoLoading.isInvisible = true
                    mBinding.bookInfoProgressText.isInvisible = true
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

    /**
     * ● 处理添加漫画至书架 意图
     *
     * ● 2023-06-15 23:01:04 周四 下午
     */
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

    /**
     * ● 通知章节页面 显示出来
     *
     * ● 2023-06-15 23:00:49 周四 下午
     */
    private fun notifyChapterPageShowNow(comicChapterResp: ComicChapterResp) {

        // 添加章节选择器
        addBookChapterSlector(comicChapterResp, null)

        // 开启协程 数据交给适配器去做出调整
        viewLifecycleOwner.lifecycleScope.launch {
            mComicChapterRvAdapter?.doNotify(comicChapterResp.mList.toMutableList())
        }
    }

    /**
     * ● 处理章节
     *
     * ● 2023-06-15 23:01:51 周四 下午
     */
    override fun <T> showChapterPage(chapterResp: T?, invalidResp: String?) {
        if (chapterResp == null) {
            processChapterFailureResult(invalidResp)
            return
        }

        if (chapterResp is ComicChapterResp) {

            if (!mBinding.bookInfoRefresh.isRefreshing) { dismissLoadingAnim() }

            if (mBinding.comicInfoErrorTips.isVisible) { mBinding.comicInfoErrorTips.animateFadeOutWithEndInVisibility() }

            if (!mBinding.bookInfoLinearChapter.isVisible) mBinding.bookInfoLinearChapter.animateFadeIn()

            if (!mBinding.bookInfoRvChapter.isVisible) { mBinding.bookInfoRvChapter.animateFadeIn() }

            notifyChapterPageShowNow(chapterResp)
        }
    }

    /**
     * ● 下拉刷新
     *
     * ● 2023-06-15 23:02:55 周四 下午
     */
    override fun onRefresh() { mBookVM.input(BookIntent.GetComicChapter(mPathword)) }

    /**
     * ● 初始化数据
     *
     * ● 2023-06-15 23:01:37 周四 下午
     */
    override fun onInitData() {

        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mBookVM.input(BookIntent.GetComicBrowserHistory(mPathword))

        mBookVM.input(BookIntent.GetComicInfoPage(mPathword))
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-06-15 23:03:06 周四 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 初始化父View
        super.initView(savedInstanceState)

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

    /**
     * ● 初始化观察者
     *
     * ● 2023-06-15 23:07:45 周四 下午
     */
    override fun initObserver(savedInstanceState: Bundle?) {
        super.initObserver(savedInstanceState)

        mBookVM.bookChapterEntity.onCollect(this) { chapter ->
            if (mBaseEvent.getBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED) == null && chapter != null) {
                toast(getString(R.string.book_readed_chapter, chapter.mChapterName))
                mComicChapterRvAdapter?.mChapterName = chapter.mChapterName
                mComicChapterRvAdapter?.notifyItemRangeChanged(0, mComicChapterRvAdapter?.itemCount ?: return@onCollect)
            }
        }

        mBookVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicChapter -> doOnBookPageChapterIntent<ComicChapterResp>(intent)
                is BookIntent.GetComicInfoPage -> doOnBookPageIntent(intent) { showComicInfoPage() }
                is BookIntent.AddComicToBookshelf -> processAddComicIntent(intent)
                is BookIntent.GetComicBrowserHistory -> {
                    intent.mBaseViewState
                        .doOnResult {
                            intent.comicBrowser?.apply {
                                if (mCollectId == null) setButtonAddToBookshelf() else setButtonRemoveFromBookshelf()
                                mComicChapterRvAdapter!!.mChapterName = mBrowse?.chapterName ?: return@doOnResult
                                mBaseEvent.setBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED, true)
                                mComicChapterRvAdapter!!.notifyItemRangeChanged(0, mComicChapterRvAdapter!!.itemCount)
                                toast(getString(R.string.book_readed_chapter, mComicChapterRvAdapter?.mChapterName))
                            }
                         }
                }
            }
        }
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-06-15 23:08:01 周四 下午
     */
    override fun initListener() {
        super.initListener()

        mBinding.bookInfoCardview.doOnClickInterval {
            navigateImage(get<Fragment>(named(Fragments.Image.name)).also { it.arguments = bundleOf(BaseStrings.IMAGE_URL to mBookVM.mComicInfoPage?.mComic?.mCover) })
        }

        mBinding.bookInfoAddToBookshelf.doOnClickInterval{
            if (BaseUser.CURRENT_USER_TOKEN.isEmpty()) {
                toast(getString(R.string.book_add_invalid))
                return@doOnClickInterval
            }
            mBookVM.input(BookIntent.AddComicToBookshelf(mBookVM.mUuid ?: return@doOnClickInterval, if (mBinding.bookInfoAddToBookshelf.text == getString(R.string.book_comic_add_to_bookshelf)) 1 else 0))
        }
    }

    /**
     * ● Lifecycle onDestoryView
     *
     * ● 2023-06-15 23:08:14 周四 下午
     */
    override fun onDestroyView() {
        super.onDestroyView()

        // 漫画适配器置空
        mComicChapterRvAdapter = null
    }

    /**
     * ● Lifectcle onStop
     *
     * ● 2023-06-24 23:32:57 周六 下午
     */
    override fun onStop() {
        super.onStop()
        mBaseEvent.remove(LOGIN_CHAPTER_HAS_BEEN_SETED)
    }
}