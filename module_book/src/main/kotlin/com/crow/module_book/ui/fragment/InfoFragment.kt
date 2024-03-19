@file:Suppress("FunctionName", "NonAsciiCharacters")

package com.crow.module_book.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.updatePadding
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicChapterResp
import com.crow.module_book.model.resp.NovelChapterResp
import com.crow.module_book.ui.viewmodel.BookViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.mangax.R as mangaR
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic
 * @Time: 2023/3/14 0:04
 * @Author: CrowForKotlin
 * @Description: ComicInfoFragment
 * @formatter:on
 **************************/

abstract class InfoFragment : BaseMviFragment<BookFragmentBinding>() {

    companion object {
        const val LOGIN_CHAPTER_HAS_BEEN_SETED = "LOGIN_CHAPTER_HAS_BEEN_SETED"
        const val HIDDEN_CHANED = "HIDDEN_CHANGED"
    }

    /** ⦁ AppProgressFactory 进度加载 */
    protected var mProgressFactory: AppProgressFactory? = null

    /** ⦁ 书架VM */
    protected val mVM by viewModel<BookViewModel>()

    /** ⦁ 漫画点击实体 */
    protected val mPathword: String by lazy {
        arguments?.getString(BaseStrings.PATH_WORD) ?: run {
            toast(getString(mangaR.string.mangax_unknow_error))
            navigateUp()
            ""
        }
    }

    /** ⦁ 漫画点击实体 */
    protected val mName: String by lazy {
        arguments?.getString(BaseStrings.NAME) ?: run {
            toast(getString(mangaR.string.mangax_unknow_error))
            navigateUp()
            ""
        }
    }

    /** ⦁ 是否添加选项卡 */
    protected var mIsTabAlreadyAdded = false

    /** ⦁ BaseEvent 单例 */
    protected val mBaseEvent = BaseEvent.getSIngleInstance()

    /** ⦁ 添加章节选择器 */
    protected fun addBookChapterSlector(comicChapterResp: ComicChapterResp?, novelChapterResp: NovelChapterResp?) {

        // 计算选项卡个数，使用向上取整的方式
        val tabItemCount = ((comicChapterResp?.mTotal?.plus(99) ?: novelChapterResp?.mTotal?.plus(99)) ?: return) / 100

        // 没有添加选项卡则执行内部逻辑
        if (!mIsTabAlreadyAdded) {

            // 选项卡个数 大于 1
            if (tabItemCount > 1) {

                // 循环选项卡个数
                repeat(tabItemCount) {

                    // 创建tab
                    val tab = mBinding.bookInfoRvChapterSelector.newTab()

                    // 获取并设置text
                    tab.text = if (it == 0) "1-100" else "${it * 100 + 1}-${it * 100 + 100}"

                    // 添加Tab
                    mBinding.bookInfoRvChapterSelector.addTab(tab)
                }

                // 章节选择器 淡入
                mBinding.bookInfoRvChapterSelector.animateFadeIn(BASE_ANIM_300L)
            }

            // 设置已经添加选项卡为true
            mIsTabAlreadyAdded = true
        }
    }

    /**
     * ⦁ 书页内容意图处理
     * @param intent 意图
     * @param onResult 交给子类处理View
     */
    protected fun doOnBookPageIntent(intent: BookIntent, onResult: Runnable) {
        intent.mViewState

            // 发生错误 取消动画 退出界面 提示
            .doOnError { _, _ ->
//                dismissLoadingAnim {}
                toast(getString(baseR.string.base_loading_error))
//                navigateUp()
            }

            // 显示书页内容 根据意图类型 再次发送获取章节意图的请求
            .doOnResult {
                onResult.run()
                if (intent is BookIntent.GetComicInfoPage) mVM.input(BookIntent.GetComicChapter(intent.pathword))
                else if (intent is BookIntent.GetNovelInfoPage) mVM.input(BookIntent.GetNovelChapter(intent.pathword))
            }

    }

    /**
     * ⦁ 书页章节意图处理
     * @param T 类型
     * @param intent 意图
     */
    protected fun<T> doOnBookPageChapterIntent(intent: BookIntent) {
        intent.mViewState
            .doOnError { _, _ ->
                if (mBinding.chapterList.adapter?.itemCount == 0) {
                    if (mBinding.bookInfoLinearChapter.isVisible) mBinding.bookInfoLinearChapter.animateFadeOutInVisibility()
                }
                if (mBinding.bookInfoRefresh.isRefreshing) processChapterErrorResult()
                dismissLoadingAnim { processChapterErrorResult() }
            }
            .doOnSuccess {
                mBinding.bookInfoRvChapterSelector.isEnabled = true
                if (mBinding.bookInfoRefresh.isRefreshing) mBinding.bookInfoRefresh.finishRefresh()
            }
            .doOnResult {
                when(intent) {
                    is BookIntent.GetComicChapter -> showChapterPage(intent.comicChapter, intent.invalidResp)
                    is BookIntent.GetNovelChapter -> showChapterPage(intent.novelChapter, intent.invalidResp)
                    else -> {}
                }
            }
    }

    /**
     * ⦁ 返回上一个界面
     */
    protected fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.BookComicInfo.name, Fragments.BookNovelInfo.name)

    /**
     * ⦁ 处理章节错误响应
     */
    protected fun processChapterErrorResult() {
        if (!mBinding.comicInfoErrorTips.isVisible) {
            mBinding.comicInfoErrorTips.animateFadeIn()
            mBinding.chapterList.animateFadeOutInVisibility()
        } else mBinding.comicInfoErrorTips.animateFadeIn()
    }

    /**
     * ⦁ 处理章节失败的结果
     * @param invalidResp 失败的结果
     */
    protected fun processChapterFailureResult(invalidResp: String?) {
        if (mBinding.bookInfoRefresh.isRefreshing) mBinding.root.showSnackBar(invalidResp ?: getString(mangaR.string.mangax_unknow_error))
        else dismissLoadingAnim {
            mBinding.comicInfoErrorTips.animateFadeIn()
            mBinding.root.showSnackBar(invalidResp ?: getString(mangaR.string.mangax_unknow_error))
        }
    }

    /**
     * ⦁ 导航至图片Fragment
     * @param fragment
     */
    protected fun navigateImage(fragment: Fragment) {
        val tag = Fragments.Image.name
        parentFragmentManager.navigateToWithBackStack(mangaR.id.app_main_fcv, this, fragment, tag, tag)
    }

    /**
     * ⦁ 设置AddToBookshelf按钮
     */
    protected fun setButtonAddToBookshelf() {
        mBinding.bookInfoAddToBookshelf.text = getString(R.string.book_comic_add_to_bookshelf)
        mBinding.bookInfoAddToBookshelf.setIconResource(R.drawable.book_ic_add_to_bookshelf_24dp)
    }

    /**
     * ⦁ 设置RemoveFromBookshelf按钮
     */
    protected fun setButtonRemoveFromBookshelf() {
        mBinding.bookInfoAddToBookshelf.setIconResource(R.drawable.book_ic_remove_from_bookshelf_24dp)
        mBinding.bookInfoAddToBookshelf.text = getString(R.string.book_comic_remove_from_bookshelf)
    }

    /** ⦁ 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentBinding.inflate(inflater)

    /** ⦁ Lifecycle onStart */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    /** ⦁ 初始化视图 */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root) { view, insets, _ ->
            view.updatePadding(top = insets.top)
            mBinding.chapterList.updatePadding(bottom = insets.bottom)
            mBinding.bottomAppbar.updatePadding(bottom = insets.bottom)
        }

        // 设置 漫画图的卡片 宽高
        mBinding.bookInfoCardview.layoutParams.height = appComicCardHeight
        mBinding.bookInfoCardview.layoutParams.width = appComicCardWidth

        // 设置刷新时不允许列表滚动
        mBinding.bookInfoRefresh.setDisableContentWhenRefresh(true)

        val more = ". . ."
        /*mBinding.desc.apply {
            mFontSize = resources.getDimension(baseR.dimen.base_sp13)
            mFontBold = true
            mAnimationTop = true
            mFontMonoSpace = true
            mMultipleLineEnable = true
            mEnableAntiAlias = true
            mAnimationMode = BaseAttrTextLayout.ANIMATION_MOVE_Y
            mFontColor = ContextCompat.getColor(mContext, mangaR.color.base_color_asc)
            mGravity = BaseAttrTextLayout.GRAVITY_CENTER_START
            mScrollSpeed = 11
            mResidenceTime = 3 * 1000
            mText =  more
        }*/
        mBinding.desc.text = more
        mBinding.name.text = mName
        mBinding.status.text = getString(R.string.BookComicStatus, more)
        mBinding.author.text = getString(R.string.book_author, more)
        mBinding.hot.text = getString(R.string.book_hot, more)
        mBinding.update.text = getString(R.string.book_update, more)
        mBinding.chapter.text = getString(R.string.book_new_chapter, more)
    }

    /** ⦁ 处理章节结果
     *
     * @param T 章节类型？ 漫画 轻小说
     * @param chapterResp 成功返回章节的数据
     * @param invalidResp 失败的结果
     **/
    abstract fun<T> showChapterPage(chapterResp: T?, invalidResp: String?)

    /** ⦁ 下拉刷新 */
    abstract fun onRefresh()

    /** ⦁ 初始化数据 */
    abstract fun onInitData()

    /** ⦁ 父类初始化数据 */
    override fun initData(savedInstanceState: Bundle?) {

        onInitData()
    }

    /** ⦁ 初始化监听器 */
    override fun initListener() {

        // 返回事件
        mBinding.bookInfoBack.doOnClickInterval { navigateUp() }

        // 章节选择器 Tab 点击事件 0-100话 101-200话
        mBinding.bookInfoRvChapterSelector.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                mBaseEvent.doOnInterval {
                    // 当选项卡添加完成后就会触发该逻辑
                    if (!mIsTabAlreadyAdded) return@doOnInterval
                    mBinding.bookInfoRvChapterSelector.isEnabled = false
                    showLoadingAnim()
                    mVM.reCountPos(tab.position)
                    mVM.input(BookIntent.GetComicChapter(mPathword))
                }
            }
        })

        // 刷新
        mBinding.bookInfoRefresh.setOnRefreshListener { mBaseEvent.doOnInterval { onRefresh() } ?: mBinding.bookInfoRefresh.finishRefresh() }

        mBinding.bookInfoDownload.doOnClickInterval { toast("很抱歉暂未开发完成...") }
    }

    /** ⦁ Lifecycle onDestoryView */
    override fun onDestroyView() {
        super.onDestroyView()

        // 设置成false是因为 当View重新创建的时候 可以重新添加章节选择器
        mIsTabAlreadyAdded = false

        mProgressFactory?.remove()?.removeProgressListener()
        mProgressFactory = null

        mBaseEvent.remove(LOGIN_CHAPTER_HAS_BEEN_SETED)
    }

    /**
     * ⦁ 当隐藏时
     *
     * ⦁ 2023-07-03 01:29:16 周一 上午
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mBaseEvent.setBoolean(HIDDEN_CHANED, hidden)
    }
}