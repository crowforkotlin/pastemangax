@file:Suppress("IMPLICIT_CAST_TO_ANY", "CAST_NEVER_SUCCEEDS")

package com.crow.module_comic.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.crow.base.current_project.*
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.current_project.entity.BookType
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_book.R
import com.crow.module_book.databinding.BookComicFragmentInfoBinding
import com.crow.module_comic.model.intent.BookIntent
import com.crow.module_comic.model.resp.ComicChapterResp
import com.crow.module_comic.model.resp.NovelChapterResp
import com.crow.module_comic.model.resp.comic_chapter.ComicChapterResult
import com.crow.module_comic.model.resp.comic_info.ComicInfoResult
import com.crow.module_comic.model.resp.comic_info.Status
import com.crow.module_comic.model.resp.novel_info.NovelInfoResult
import com.crow.module_comic.ui.adapter.ComicChapterRvAdapter
import com.crow.module_comic.ui.adapter.NovelChapterRvAdapter
import com.crow.module_comic.ui.viewmodel.BookInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic
 * @Time: 2023/3/14 0:04
 * @Author: CrowForKotlin
 * @Description: ComicInfoFragment
 * @formatter:on
 **************************/
class BookInfoFragment : BaseMviFragment<BookComicFragmentInfoBinding>() {

    companion object { val TAG = this::class.java.simpleName }

    // 书架VM
    private val mBookVM by viewModel<BookInfoViewModel>()

    // 漫画点击实体
    private val mBookTapEntity: BookTapEntity by lazy {
        // 获取TapEntity 根据Android版本获取 未获取则返回上个界面
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("tapEntity", BookTapEntity::class.java)
        } else {
            arguments?.getSerializable("tapEntity") as BookTapEntity
        } ?: run {
            toast(getString(baseR.string.BaseUnknow))
            navigateUp()
        } as BookTapEntity
    }

    // 是否添加选项卡
    private var mIsTabAlreadyAdded = false

    // 默认的Appbar状态
    private var mAppbarState = STATE_EXPANDED

    // 漫画章节Rv
    private var mComicChapterRvAdapter: ComicChapterRvAdapter? = null

    // 轻小说章节Rv
    private var mNovelChapterRvAdapter: NovelChapterRvAdapter? = null

    // 显示书页信息
    private fun showBookInfoPage() {

        // 根据VM 数据得到单独的一个完整数据（未确定类型）
        val bookResult = if (mBookVM.mComicInfoPage != null) mBookVM.mComicInfoPage!!.mComic else mBookVM.mNovelInfoPage!!.mNovel

        // 类型有两个 漫画 和 小说
        if (bookResult is ComicInfoResult) {
            Glide.with(this).load(bookResult.mCover).into(mBinding.comicInfoImage)
            mBinding.comicInfoAuthor.text = getString(R.string.BookComicAuthor, bookResult.mAuthor.joinToString { it.mName })
            mBinding.comicInfoHot.text = getString(R.string.BookComicHot, formatValue(bookResult.mPopular))
            mBinding.comicInfoUpdate.text = getString(R.string.BookComicUpdate, bookResult.mDatetimeUpdated)
            mBinding.comicInfoNewChapter.text = getString(R.string.BookComicNewChapter, bookResult.mLastChapter.mName)
            mBinding.comicInfoStatus.text = when (bookResult.mStatus.mValue) {
                Status.LOADING -> getString(R.string.BookComicStatus, bookResult.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.comic_green), 3)
                Status.FINISH -> getString(R.string.BookComicStatus, bookResult.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.comic_red), 3)
                else -> null
            }
            mBinding.comicInfoName.text = bookResult.mName
            mBinding.comicInfoDesc.text = bookResult.mBrief
            bookResult.mTheme.forEach { theme ->
                mBinding.comicInfoThemeChip.addView(Chip(mContext).also {
                    it.text = theme.mName
                    it.isClickable = false
                })
            }
        } else if (bookResult is NovelInfoResult) {
            Glide.with(this).load(bookResult.mCover).into(mBinding.comicInfoImage)
            mBinding.comicInfoAuthor.text = getString(R.string.BookComicAuthor, bookResult.mAuthor.joinToString { it.mName })
            mBinding.comicInfoHot.text = getString(R.string.BookComicHot, formatValue(bookResult.mPopular))
            mBinding.comicInfoUpdate.text = getString(R.string.BookComicUpdate, bookResult.mDatetimeUpdated)
            mBinding.comicInfoNewChapter.text = getString(R.string.BookComicNewChapter, bookResult.mLastChapter.mName)
            mBinding.comicInfoStatus.text = when (bookResult.mStatus.mValue) {
                Status.LOADING -> getString(R.string.BookComicStatus, bookResult.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.comic_green), 3)
                Status.FINISH -> getString(R.string.BookComicStatus, bookResult.mStatus.mDisplay).getSpannableString(ContextCompat.getColor(mContext, R.color.comic_red), 3)
                else -> null
            }
            mBinding.comicInfoName.text = bookResult.mName
            mBinding.comicInfoDesc.text = bookResult.mBrief
            bookResult.mTheme.forEach { theme ->
                mBinding.comicInfoThemeChip.addView(Chip(mContext).also {
                    it.text = theme.mName
                    it.isClickable = false
                })
            }
        }

        // 整体布局淡入
        mBinding.root.animateFadeIn(BASE_ANIM_300L)
    }

    // 显示漫画章节页
    private fun showBookChapterPage(comicChapterResp: ComicChapterResp?, novelChapterResp: NovelChapterResp?) {

        // 添加章节选择器
        addBookChapterSlector(comicChapterResp, novelChapterResp)

        // 开启协程 数据交给适配器去做出调整
        viewLifecycleOwner.lifecycleScope.launch {
            mComicChapterRvAdapter?.doNotify(comicChapterResp?.mList?.toMutableList() ?: return@launch)
            mNovelChapterRvAdapter?.doNotify(novelChapterResp?.mList?.toMutableList() ?: return@launch)
        }
    }

    // 添加章节选择器
    private fun addBookChapterSlector(comicChapterResp: ComicChapterResp?, novelChapterResp: NovelChapterResp?) {

        novelChapterResp?.mTotal.logMsg()

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

            // 章节 Rv 淡入
            mBinding.bookInfoRvChapter.animateFadeIn(BASE_ANIM_300L)

            // 设置已经添加选项卡为true
            mIsTabAlreadyAdded = true
        }
    }

    // 恢复视图
    private fun doRecoverView() {

        // 添加漫画章节选择器
        addBookChapterSlector(mBookVM.mComicChapterPage, mBookVM.mNovelChapterPage)

        // 显示漫画页
        showBookInfoPage()

        // 章节选择器 可见
        mBinding.bookInfoRvChapterSelector.visibility = View.VISIBLE

        // Rv可见
        mBinding.bookInfoRvChapter.visibility = View.VISIBLE
    }

    // 书页内容意图处理
    private fun doBookInfoIntent(intent: BookIntent) {
        intent.mViewState
            // 执行加载动画
            .doOnLoading { showLoadingAnim() }

            // 发生错误 取消动画 退出界面 提示
            .doOnError { _, _ ->
                dismissLoadingAnim()
                navigateUp()
                toast(getString(baseR.string.BaseLoadingError))
            }

            // 显示书页内容 根据意图类型 再次发送获取章节意图的请求
            .doOnResult {
                showBookInfoPage()
                if (intent is BookIntent.GetComicInfo) mBookVM.input(BookIntent.GetComicChapter(intent.pathword))
                else if (intent is BookIntent.GetNovelInfo) mBookVM.input(BookIntent.GetNovelChapter(intent.pathword))
            }
    }

    // 书页章节意图处理
    private fun doBookInfoChapterIntent(intent: BookIntent) {
        intent.mViewState
            .doOnError { _, _ -> mBinding.comicInfoErrorTips.visibility = View.VISIBLE }
            .doOnSuccess {
                mBinding.bookInfoRvChapterSelector.isEnabled = true
                if (mBinding.bookComicInfoRefresh.isRefreshing) mBinding.bookComicInfoRefresh.finishRefresh()
            }
            .doOnResult {
                when(intent) {
                    is BookIntent.GetComicChapter -> {
                        if (intent.comicChapter != null) {
                            if (mBinding.bookComicInfoRefresh.isRefreshing) showBookChapterPage(intent.comicChapter, null)
                            else dismissLoadingAnim { showBookChapterPage(intent.comicChapter, null) }
                            return@doOnResult
                        }
                        dismissLoadingAnim {
                            navigateUp()
                            mBinding.root.showSnackBar(intent.invalidResp ?: getString(baseR.string.BaseUnknow))
                        }
                    }
                    is BookIntent.GetNovelChapter -> {
                        if (intent.novelChapter != null) {
                            if (mBinding.bookComicInfoRefresh.isRefreshing) showBookChapterPage(null, intent.novelChapter)
                            else dismissLoadingAnim { showBookChapterPage(null, intent.novelChapter) }
                            return@doOnResult
                        }
                        dismissLoadingAnim {
                            navigateUp()
                            mBinding.root.showSnackBar(intent.invalidResp ?: getString(baseR.string.BaseUnknow))
                        }
                    }
                    else -> {}
                }
            }
    }

    override fun getViewBinding(inflater: LayoutInflater) = BookComicFragmentInfoBinding.inflate(inflater)

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(),0 , mContext.getNavigationBarHeight())

        // 设置 漫画图的卡片 宽高
        mBinding.comicInfoCard.layoutParams.height = getComicCardHeight()
        mBinding.comicInfoCard.layoutParams.width = getComicCardWidth()

        // 设置刷新时不允许列表滚动
        mBinding.bookComicInfoRefresh.setDisableContentWhenRefresh(true)

        // 重新创建View之后 appBarLayout会展开折叠，记录一个状态进行初始化
        if (mAppbarState == STATE_COLLAPSED) mBinding.bookComicInfoAppbar.setExpanded(false, false)
        else mBinding.bookComicInfoAppbar.setExpanded(true, false)

        // 漫画
        if (mBookTapEntity.type == BookType.Comic) {

            // 定义点击章节
            val doOnTapChapter = { comic: ComicChapterResult -> navigate(baseR.id.mainComicfragment, Bundle().also {
                    it.putString(BaseStrings.PATH_WORD, comic.comicPathWord)
                    it.putString("uuid", comic.uuid)
            }) }

            // 初始化漫画章节适配器 这里判断数据是否为空？ 不为空直接恢复View的状态
            mComicChapterRvAdapter = if (mBookVM.mComicChapterPage != null && mBookVM.mComicInfoPage != null) {

                // 恢复视图
                doRecoverView()

                // 返回 适配器
                ComicChapterRvAdapter(mBookVM.mComicChapterPage!!.mList.toMutableList(),doOnTapChapter)

            } else ComicChapterRvAdapter(mDoOnTapChapter = doOnTapChapter)
        }

        // 轻小说
        if(mBookTapEntity.type == BookType.Novel) {

            // 初始化轻小说章节适配器 这里判断数据是否为空？不为空直接恢复View的状态
            mNovelChapterRvAdapter = if (mBookVM.mNovelChapterPage != null && mBookVM.mNovelInfoPage != null) {

                // 恢复视图
                doRecoverView()

                NovelChapterRvAdapter(mBookVM.mNovelChapterPage!!.mList.toMutableList()) { }
            } else NovelChapterRvAdapter { }
        }

        // 设置适配器
        mBinding.bookInfoRvChapter.adapter = mComicChapterRvAdapter ?: mNovelChapterRvAdapter ?:return
    }

    override fun initListener() {

        // 章节选择器 Tab 点击事件 0-100话 101-200话
        mBinding.bookInfoRvChapterSelector.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 当选项卡添加完成后就会触发该逻辑
                if (!mIsTabAlreadyAdded) return
                mBinding.bookInfoRvChapterSelector.isEnabled = false
                showLoadingAnim()
                mBookVM.reCountPos(tab.position)
                mBookVM.input(BookIntent.GetComicChapter(mBookTapEntity.pathword))
            }
        })

        // 记录AppBar的状态 （展开、折叠）偏移监听
        mBinding.bookComicInfoAppbar.addOnOffsetChangedListener { appBar, offset ->
            mAppbarState = if (offset == 0) STATE_EXPANDED else if (abs(offset) >= appBar.totalScrollRange) STATE_COLLAPSED else STATE_COLLAPSED
        }

        // 设置刷新事件
        mBinding.bookComicInfoRefresh.setOnRefreshListener { mBookVM.input(BookIntent.GetComicChapter(mBookTapEntity.pathword)) }
    }

    override fun initData() {

        // 数据不为空 则退出
        if (mBookVM.doNovelDatasIsNotNull() || mBookVM.doComicDatasIsNotNull()) return

        // Token 不为空 则获取浏览记录
        if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) {

            // 判断类型
            when(mBookTapEntity.type) {

                // 漫画 获取当前漫画浏览记录
                BookType.Comic -> mBookVM.input(BookIntent.GetComicBrowserHistory(mBookTapEntity.pathword))

                // 轻小说 获取当前轻小说浏览记录
                BookType.Novel -> mBookVM.input(BookIntent.GetNovelBrowserHistory(mBookTapEntity.pathword))

                else -> {}
            }
        }

        // 判断类型 获取书页信息
        when(mBookTapEntity.type) {

            // 漫画 获取单个漫画信息
            BookType.Comic -> mBookVM.input(BookIntent.GetComicInfo(mBookTapEntity.pathword))

            // 轻小说 获取单个轻小说信息
            BookType.Novel -> mBookVM.input(BookIntent.GetNovelInfo(mBookTapEntity.pathword))

            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 漫画适配器置空
        mComicChapterRvAdapter = null

        // 轻小说适配器置空
        mNovelChapterRvAdapter = null

        // 设置成false是因为 当View重新创建的时候 可以重新添加章节选择器
        mIsTabAlreadyAdded = false
    }

    override fun initObserver() {
        mBookVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicInfo -> doBookInfoIntent(intent)
                is BookIntent.GetNovelInfo -> doBookInfoIntent(intent)

                is BookIntent.GetComicChapter -> doBookInfoChapterIntent(intent)
                is BookIntent.GetNovelChapter -> doBookInfoChapterIntent(intent)

                is BookIntent.GetComicBrowserHistory -> {
                    intent.mViewState.doOnResult {
                        mComicChapterRvAdapter?.mChapterName = intent.comicBrowser?.browse?.chapterName ?: return@doOnResult
                        toast(getString(R.string.BookComicReadedPage, mComicChapterRvAdapter?.mChapterName))
                    }
                }
                is BookIntent.GetNovelBrowserHistory -> {
                    intent.mViewState.doOnResult {
                        mNovelChapterRvAdapter?.mChapterName = intent.novelBrowser?.browse?.chapterName ?: return@doOnResult
                        toast(getString(R.string.BookComicReadedPage, mNovelChapterRvAdapter?.mChapterName))
                    }
                }
                else -> { }
            }
        }
    }
}