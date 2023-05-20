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
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.removeWhiteSpace
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.R
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.NovelChapterResp
import com.crow.module_book.model.resp.comic_info.Status
import com.crow.module_book.ui.adapter.NovelChapterRvAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named

class BookNovelFragment : BookFragment() {

    init {
        FlowBus.with<String>(BaseStrings.Key.UPDATE_CHAPTER).register(this) {
            mBookVM.updateBookChapter(mBookVM.mComicInfoPage!!.mComic!!.mName, it, BookType.COMIC )
            mNovelChapterRvAdapter?.mChapterName = it
        }
    }

    // 轻小说章节Rv
    private var mNovelChapterRvAdapter: NovelChapterRvAdapter? = null

    private fun showNovelInfoPage() {
        val novelInfoPage = mBookVM.mNovelInfoPage?.mNovel ?: return

        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(novelInfoPage.mCover) { _, _, percentage, _, _ -> mBinding.bookInfoProgressText.text = AppGlideProgressFactory.getProgressString(percentage) }

        Glide.with(this)
            .load(novelInfoPage.mCover)
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
        mBinding.bookInfoDesc.text = novelInfoPage.mBrief.removeWhiteSpace()
        novelInfoPage.mTheme.forEach { theme ->
            mBinding.bookInfoThemeChip.addView(Chip(mContext).also {
                it.text = theme.mName
                it.isClickable = false
            })
        }

        mBinding.bookInfoCardview.animateFadeIn()
        buttonGroupFadeIn()
    }

    private fun showChapterPage(novelChapterResp: NovelChapterResp) {
        // 添加章节选择器
        addBookChapterSlector(null, novelChapterResp)

        // 开启协程 数据交给适配器去做出调整
        viewLifecycleOwner.lifecycleScope.launch {
            mNovelChapterRvAdapter?.doNotify(novelChapterResp.mList.toMutableList())
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
                mNovelChapterRvAdapter?.mChapterName = mNovelChapterRvAdapter?.mChapterName
                mNovelChapterRvAdapter?.notifyItemRangeChanged(0, mNovelChapterRvAdapter?.itemCount ?: return@onCollect)
            } else {
                mNovelChapterRvAdapter?.mChapterName = if(BaseUser.CURRENT_USER_TOKEN.isEmpty()) chapter.bookChapterName else {
                    mNovelChapterRvAdapter?.mChapterName ?: run {
                        mBaseEvent.setBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED, false)
                        return@onCollect
                    }
                }
                mNovelChapterRvAdapter?.notifyItemRangeChanged(0, mNovelChapterRvAdapter?.itemCount ?: return@onCollect)
            }
        }
    }

    private fun processAddNovelIntent(intent: BookIntent.AddNovelToBookshelf) {
        intent.mBaseViewState
            .doOnLoading { showLoadingAnim() }
            .doOnError { _, _ -> dismissLoadingAnim { toast(getString(com.crow.base.R.string.BaseUnknowError)) } }
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

        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mBookVM.input(BookIntent.GetNovelBrowserHistory(mPathword))

        mBookVM.input(BookIntent.GetNovelInfoPage(mPathword))
    }

    override fun onRefresh() { mBookVM.input(BookIntent.GetNovelChapter(mPathword)) }

    override fun initView(bundle: Bundle?) {

        // 初始化父View
        super.initView(bundle)

        // 初始化适配器
        mNovelChapterRvAdapter = NovelChapterRvAdapter {
            toast("很抱歉暂未开发完成...")
        }

        // 设置适配器
        mBinding.bookInfoRvChapter.adapter = mNovelChapterRvAdapter!!

    }

    override fun initListener() {
        super.initListener()

        mBinding.bookInfoCardview.doOnClickInterval {
            val fragment = get<Fragment>(named(Fragments.Image))
            fragment.arguments =Bundle().also { bundle -> bundle.putString(BaseStrings.IMAGE_URL, mBookVM.mNovelInfoPage?.mNovel?.mCover) }
            navigateImage(fragment)
        }

        mBinding.bookInfoAddToBookshelf.doOnClickInterval{
            if (BaseUser.CURRENT_USER_TOKEN.isNullOrEmpty()) {
                toast(getString(R.string.book_add_invalid))
                return@doOnClickInterval
            }
            mBookVM.input(BookIntent.AddNovelToBookshelf(mBookVM.mUuid ?: return@doOnClickInterval, if (mBinding.bookInfoAddToBookshelf.text == getString(R.string.book_comic_add_to_bookshelf)) 1 else 0))
        }

    }

    override fun initObserver() {
        super.initObserver()

        mBookVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetNovelChapter -> doOnBookPageChapterIntent<NovelChapterResp>(intent) { showChapterPage(it)}
                is BookIntent.GetNovelInfoPage -> doOnBookPageIntent(intent) { showNovelInfoPage() }
                is BookIntent.GetNovelBrowserHistory -> {
                    intent.mBaseViewState
                        .doOnResult {
                            intent.novelBrowser!!.apply {
                                if (mCollect == null) setButtonAddToBookshelf() else setButtonRemoveFromBookshelf()
                                mNovelChapterRvAdapter?.mChapterName = mBrowse?.chapterName ?: return@doOnResult
                                if (mBaseEvent.getBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED) == false) {
                                    mBaseEvent.setBoolean(LOGIN_CHAPTER_HAS_BEEN_SETED, true)
                                    mNovelChapterRvAdapter?.notifyItemRangeChanged(0, mNovelChapterRvAdapter?.itemCount ?: return@doOnResult)
                                }
                                toast(getString(R.string.BookComicReadedPage, mNovelChapterRvAdapter?.mChapterName))
                            }
                        }
                }
                is BookIntent.AddNovelToBookshelf -> { processAddNovelIntent(intent) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 轻小说适配器置空
        mNovelChapterRvAdapter = null
    }
}