package com.crow.module_book.ui.fragment.comic

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.BaseUserConfig
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.getSpannableString
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.px2dp
import com.crow.base.tools.extensions.px2sp
import com.crow.base.tools.extensions.removeWhiteSpace
import com.crow.base.tools.extensions.startActivity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.entity.AppConfig.Companion.mChineseConvert
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.module_book.R
import com.crow.module_book.model.entity.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicChapterResp
import com.crow.module_book.model.resp.comic_info.Status
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.ComicChapterRvAdapter
import com.crow.module_book.ui.fragment.BookFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import com.crow.mangax.R as mangaR
import com.crow.base.R as baseR

class BookComicFragment : BookFragment() {

    /**
     * ● Regist FlowBus
     *
     * ● 2023-06-24 23:45:12 周六 下午
     */
    init {
        FlowBus.with<BookChapterEntity>(BaseEventEnum.UpdateChapter.name).register(this) { chapterEntity ->
            mVM.updateBookChapterOnDB(chapterEntity)
        }
    }

    /**
     * ● 漫画章节Rv
     *
     * ● 2023-06-15 23:00:16 周四 下午
     */
    private var mAdapter: ComicChapterRvAdapter? = null

    /**
     * ● 显示漫画信息页面
     *
     * ● 2023-06-15 23:00:25 周四 下午
     */
    private fun showComicInfoPage() {
        val comicInfoPage = mVM.mComicInfoPage?.mComic ?: return

        // 在DB中查找已读章节
        mVM.findReadedBookChapterOnDB(comicInfoPage.mName, BookType.COMIC)

        mProgressFactory = AppProgressFactory.createProgressListener(comicInfoPage.mCover) { _, _, percentage, _, _ -> mBinding.bookInfoProgressText.text = AppProgressFactory.formateProgress(percentage) }

        app.imageLoader.enqueue(
            ImageRequest.Builder(mContext)
                .listener(
                    onSuccess = { _, _ ->
                        mBinding.bookInfoLoading.isInvisible = true
                        mBinding.bookInfoProgressText.isInvisible = true
                    },
                    onError = { _, _ -> mBinding.bookInfoProgressText.text = "-1%" },
                )
                .data(comicInfoPage.mCover)
                .target(mBinding.bookInfoImage)
                .build()
        )

        mBinding.author.text = getString(R.string.book_author, comicInfoPage.mAuthor.joinToString { it.mName })
        mBinding.hot.text = getString(R.string.book_hot, formatHotValue(comicInfoPage.mPopular))
        mBinding.update.text = getString(R.string.book_update, comicInfoPage.mDatetimeUpdated)
        val status = when (comicInfoPage.mStatus.mValue) {
            Status.LOADING -> getString(R.string.BookComicStatus, comicInfoPage.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.book_green), 3)
            Status.FINISH -> getString(R.string.BookComicStatus, comicInfoPage.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.book_red), 3)
            else -> ". . ."
        }.toString()
        if (mChineseConvert) {
            lifecycleScope.launch {
                mBinding.chapter.text = ChineseConverter.convert(getString(R.string.book_new_chapter, comicInfoPage.mLastChapter.mName))
                mBinding.status.text = ChineseConverter.convert(status)
                mBinding.name.text = ChineseConverter.convert(comicInfoPage.mName)
                mBinding.desc.text = ChineseConverter.convert(comicInfoPage.mBrief.removeWhiteSpace())
                comicInfoPage.mTheme.forEach { theme ->
                    mBinding.bookInfoThemeChip.addView(Chip(mContext).also {
                        it.text = ChineseConverter.convert(theme.mName)
                        it.textSize = app.px2sp(resources.getDimension(baseR.dimen.base_sp12_5))
                        it.chipStrokeWidth = app.px2dp(resources.getDimension(baseR.dimen.base_dp1))
                        it.isClickable = false
                    })
                }
            }
        } else {
            mBinding.chapter.text = getString(R.string.book_new_chapter, comicInfoPage.mLastChapter.mName)
            mBinding.status.text = status
            mBinding.name.text = comicInfoPage.mName
            mBinding.desc.text = comicInfoPage.mBrief.removeWhiteSpace()
            comicInfoPage.mTheme.forEach { theme ->
                mBinding.bookInfoThemeChip.addView(Chip(mContext).also {
                    it.text = theme.mName
                    it.textSize = app.px2sp(resources.getDimension(baseR.dimen.base_sp12_5))
                    it.chipStrokeWidth = app.px2dp(resources.getDimension(baseR.dimen.base_dp1))
                    it.isClickable = false
                })
            }
        }
    }

    /**
     * ● 处理添加漫画至书架 意图
     *
     * ● 2023-06-15 23:01:04 周四 下午
     */
    private fun processAddComicIntent(intent: BookIntent.AddComicToBookshelf) {
        intent.mViewState
            .doOnLoading { showLoadingAnim() }
            .doOnError { _, _ -> dismissLoadingAnim { toast(getString(mangaR.string.mangax_unknow_error)) } }
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
            mAdapter?.doNotify(comicChapterResp.mList.toMutableList())
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
    override fun onRefresh() {
        if (mVM.mComicInfoPage == null) {
            mVM.input(BookIntent.GetComicInfoPage(mPathword))
        }
        mVM.input(BookIntent.GetComicChapter(mPathword))
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-06-15 23:01:37 周四 下午
     */
    override fun onInitData() {

        if (BaseUserConfig.CURRENT_USER_TOKEN.isNotEmpty()) mVM.input(BookIntent.GetComicBrowserHistory(mPathword))

        mVM.input(BookIntent.GetComicInfoPage(mPathword))
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
        mAdapter = ComicChapterRvAdapter { comic  ->

            mContext.startActivity<ComicActivity> {
                putExtra(ComicViewModel.PREV_UUID, comic.prev)
                putExtra(ComicViewModel.NEXT_UUID, comic.next)
                putExtra(ComicViewModel.UUID, comic.uuid)
                putExtra(BaseStrings.PATH_WORD, comic.comicPathWord)
                mVM.mChapterEntity
            }
            if (Build.VERSION.SDK_INT >= 34) {
                requireActivity().overrideActivityTransition(AppCompatActivity.OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            if (BaseUserConfig.CURRENT_USER_TOKEN.isNotEmpty()) mAdapter?.mChapterName = comic.name
        }

        // 设置适配器
        mBinding.bookInfoRvChapter.adapter = mAdapter!!
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-06-15 23:07:45 周四 下午
     */
    override fun initObserver(saveInstanceState: Bundle?) {
        super.initObserver(saveInstanceState)

        mVM.mChapterEntity.onCollect(this) { chapter ->
            if (mBaseEvent.getBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED) == null && chapter != null) {
                mAdapter?.mChapterName = chapter.mChapterName
                mAdapter?.notifyItemRangeChanged(0, mAdapter?.itemCount ?: return@onCollect)
                if (BaseUserConfig.CURRENT_USER_TOKEN.isNotEmpty()) {
                    toast(getString(R.string.book_readed_chapter, chapter.mChapterName))
                } else {
                    toast(getString(R.string.book_readed_chapter_offline, chapter.mChapterName))
                }
            }
        }

        mVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicChapter -> doOnBookPageChapterIntent<ComicChapterResp>(intent)
                is BookIntent.GetComicInfoPage -> doOnBookPageIntent(intent) { showComicInfoPage() }
                is BookIntent.AddComicToBookshelf -> processAddComicIntent(intent)
                is BookIntent.GetComicBrowserHistory -> {
                    intent.mViewState
                        .doOnResult {
                            intent.comicBrowser?.apply {
                                if (mCollectId == null) setButtonAddToBookshelf() else setButtonRemoveFromBookshelf()
                                mAdapter!!.mChapterName = mBrowse?.chapterName ?: return@doOnResult
                                mBaseEvent.setBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED, true)
                                mAdapter!!.notifyItemRangeChanged(0, mAdapter!!.itemCount)
                                toast(getString(R.string.book_readed_chapter, mAdapter?.mChapterName))
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

        // 卡片
        mBinding.bookInfoCardview.doOnClickInterval {
            navigateImage(get<Fragment>(named(Fragments.Image.name)).also {
                it.arguments = bundleOf(
                    BaseStrings.IMAGE_URL to mVM.mComicInfoPage?.mComic?.mCover,
                    BaseStrings.NAME to mVM.mComicInfoPage?.mComic?.mName
                )
            })
        }

        // 添加到书架
        mBinding.bookInfoAddToBookshelf.doOnClickInterval {
            if (mVM.mComicInfoPage == null) return@doOnClickInterval
            if (BaseUserConfig.CURRENT_USER_TOKEN.isEmpty()) {
                toast(getString(R.string.book_add_invalid))
                return@doOnClickInterval
            }
            mVM.input(BookIntent.AddComicToBookshelf(mVM.mUuid ?: return@doOnClickInterval, if (mBinding.bookInfoAddToBookshelf.text == getString(R.string.book_comic_add_to_bookshelf)) 1 else 0))
        }

        // 阅读
        mBinding.bookInfoReadnow.doOnClickInterval {
            if ((mAdapter ?: return@doOnClickInterval).itemCount == 0 || mVM.mComicInfoPage == null) return@doOnClickInterval
            val chapter = mVM.mChapterEntity.value
            if (chapter == null) {
                val adapterChapter = mAdapter!!.getItem(0)
                startComicActivity(adapterChapter.prev, adapterChapter.next, adapterChapter.uuid, adapterChapter.comicPathWord, adapterChapter.name)
            } else {
                val pathword = mVM.mComicInfoPage?.mComic?.mPathWord ?: return@doOnClickInterval
                startComicActivity(chapter.mChapterPrevUUID, chapter.mChapterNextUUID, chapter.mChapterUUID, pathword, chapter.mChapterName)
            }
        }
    }

    private fun startComicActivity(prev: String?, next: String?, uuid: String, pathword: String, chapterName: String) {
        mContext.startActivity<ComicActivity> {
            putExtra(ComicViewModel.PREV_UUID, prev)
            putExtra(ComicViewModel.NEXT_UUID, next)
            putExtra(ComicViewModel.UUID, uuid)
            putExtra(BaseStrings.PATH_WORD, pathword)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            requireActivity().overrideActivityTransition(AppCompatActivity.OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        if (BaseUserConfig.CURRENT_USER_TOKEN.isNotEmpty()) mAdapter?.mChapterName = chapterName
    }

    /**
     * ● Lifecycle onDestoryView
     *
     * ● 2023-06-15 23:08:14 周四 下午
     */
    override fun onDestroyView() {
        super.onDestroyView()

        // 漫画适配器置空
        mAdapter = null
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