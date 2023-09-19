package com.crow.module_bookshelf.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.processTokenError
import com.crow.base.copymanga.ui.view.BaseTapScrollRecyclerView
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisible
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.findFisrtVisibleViewPosition
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.BaseViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnResultSuspend
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_bookshelf.R
import com.crow.module_bookshelf.databinding.BookshelfFragmentBinding
import com.crow.module_bookshelf.model.intent.BookshelfIntent
import com.crow.module_bookshelf.model.resp.BookshelfComicResp
import com.crow.module_bookshelf.model.resp.BookshelfNovelResp
import com.crow.module_bookshelf.ui.adapter.BookshelfComicRvAdapter
import com.crow.module_bookshelf.ui.adapter.BookshelfNovelRvAdapter
import com.crow.module_bookshelf.ui.viewmodel.BookshelfViewModel
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @RelativePath: com\crow\module_bookshelf\ui\fragment\BookShelfFragment.kt
 * @Path: D:\Programing\Android\2023\CopyManga\module_bookshelf\src\main\kotlin\com\crow\module_bookshelf\ui\fragment\BookShelfFragment.kt
 * @Author: CrowForKotlin
 * @Time: 2023/3/22 23:56 Wed PM
 * @Description: BookShelfFragment 因为书架模块的 轻小说和漫画的功能基本一致，所以两个Rv集成到一个Fragment下即可
 * @formatter:on
 *************************/
class BookshelfFragment : BaseMviFragment<BookshelfFragmentBinding>() {

    companion object {
        const val BOOKSHELF = "Bookshelf"
    }

    /**
     * ● 书架VM
     *
     * ● 2023-07-01 20:26:12 周六 下午
     */
    private val mBsVM by viewModel<BookshelfViewModel>()

    /**
     * ● Bookshelf Comic适配器
     *
     * ● 2023-07-07 21:49:53 周五 下午
     */
    private lateinit var mBookshelfComicRvAdapter: BookshelfComicRvAdapter

    /**
     * ● Bookshelf Novel适配器
     *
     * ● 2023-07-07 21:50:00 周五 下午
     */
    private lateinit var mBookshelfNovelRvAdapter: BookshelfNovelRvAdapter

    /**
     * ● 漫画计数
     *
     * ● 2023-07-07 21:50:05 周五 下午
     */
    private var mComicCount: Int? = null

    /**
     * ● 轻小说计数
     *
     * ● 2023-07-07 21:50:10 周五 下午
     */
    private var mNovelCount: Int? = null

    /**
     * ● 处理错误时 隐藏控件
     *
     * ● 2023-07-07 21:50:17 周五 下午
     */
    private fun processErrorHideView() {
        if (mBinding.bookshelfTipsEmpty.tag == null) return
        mBinding.bookshelfTipsEmpty.tag = null
        mBinding.bookshelfTipsEmpty.animateFadeIn()  // “空文本” 可见
        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOutWithEndInVisibility()          // 隐藏 计数
        if (mBinding.bookshelfRvComic.isVisible) mBinding.bookshelfRvComic.animateFadeOutWithEndInVisibility()  // 隐藏 漫画 Rv
        if (mBinding.bookshelfRvNovel.isVisible) mBinding.bookshelfRvNovel.animateFadeOutWithEndInVisibility()    // 隐藏 轻小说 Rv
        mBinding.bookshelfRefresh.finishRefresh()   // 完成刷新
    }

    /**
     * ● 处理错误
     *
     * ● 2023-07-07 21:49:45 周五 下午
     */
    private fun processError(code: Int, msg: String?) {

        // 解析地址失败 且 Resumed的状态才提示
        if (code == BaseViewState.Error.UNKNOW_HOST && isResumed) {
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) mBookshelfNovelRvAdapter.refresh()
            else mBookshelfComicRvAdapter.refresh()
        }

        // Token为空不处理 Token错误校验
        else if (BaseUser.CURRENT_USER_TOKEN.isEmpty()) {
            if (isResumed) toast(getString(R.string.bookshelf_identity_expired))
            return
        }

        // 处理Token错误校验
        else mBinding.root.processTokenError(code, msg,
            doOnCancel = {
                mBookshelfComicRvAdapter.retry()
                mBookshelfNovelRvAdapter.retry()
                parentFragmentManager.setFragmentResult(BaseEventEnum.ClearUserInfo.name, arguments ?: Bundle())
            },
            doOnConfirm = {
                requireParentFragment().parentFragmentManager.navigateToWithBackStack(
                    baseR.id.app_main_fcv,
                    requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
                    get(named(Fragments.Login.name)),
                    Fragments.Login.name,
                    Fragments.Login.name
                )
                parentFragmentManager.setFragmentResult(BaseEventEnum.ClearUserInfo.name, arguments ?: Bundle())
            }
        )
    }

    /**
     * ● 处理结果视图
     *
     * ● 2023-07-07 22:03:47 周五 下午
     */
    private fun processResultView(bookshelfRv: RecyclerView) {
        if (mBinding.bookshelfTipsEmpty.isGone || mBinding.bookshelfTipsEmpty.tag != null) return
        mBinding.bookshelfTipsEmpty.tag = Unit
        mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
        mBinding.bookshelfCount.animateFadeIn()
        bookshelfRv.animateFadeIn()
    }

    /**
     * ● 处理结果
     *
     * ● 2023-07-07 21:50:30 周五 下午
     */
    private fun processResult(bookshelfComicResp: BookshelfComicResp?, bookshelfNovelResp: BookshelfNovelResp?) {
        when(mBinding.bookshelfButtonGropu.checkedButtonId) {
            mBinding.bookshelfComic.id -> {
                processResultView(mBinding.bookshelfRvComic)
                bookshelfComicResp?.let { mBinding.bookshelfCount.text =  getString(R.string.bookshelf_comic_count, mComicCount ?: -1) }
            }
            mBinding.bookshelfNovel.id -> {
                processResultView(mBinding.bookshelfRvNovel)
                bookshelfNovelResp?.let { mBinding.bookshelfCount.text = getString(R.string.bookshelf_novel_count, mNovelCount ?: -1) }
            }
        }
    }
    
    /**
     * ● 导航
     *
     * ● 2023-07-07 21:51:20 周五 下午
     */
    private fun navigate(tag: String, pathword: String) {
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            get<Fragment>(named(tag)).also { it.arguments = bundle }, tag, tag
        )
    }

    /**
     * ● Mvi Intent Output
     *
     * ● 2023-07-07 21:55:12 周五 下午
     */
    private fun onCollectState() {

        // 每个观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
        repeatOnLifecycle {
            mBsVM.mBookshelfComicFlowPager?.collect { data ->
                mBookshelfComicRvAdapter.submitData(data)
            }
        }

        // 收集书架 轻小说Pager状态
        repeatOnLifecycle {
            mBsVM.mBookshelfNovelFlowPager?.collect { data ->
                mBookshelfNovelRvAdapter.submitData(data)
            }
       }
    }

    /**
     * ● 获取ViewBinding
     *
     * ● 2023-07-07 21:55:05 周五 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) =
        BookshelfFragmentBinding.inflate(inflater)

    /**
     * ● Lifecycle onDestroyView
     *
     * ● 2023-07-07 21:52:59 周五 下午
     */
    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.clearFragmentResultListener(BOOKSHELF)
    }

    /**
     * ● Lifecycle onCreate
     *
     * ● 2023-07-07 21:54:21 周五 下午
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 发送获取书架 “漫画” 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfComic())

        // 发送获取书架 “轻小说” 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfNovel())
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-07-07 21:54:34 周五 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.bookshelfToolbar, paddingNaviateBar = false)

        // 设置刷新时不允许列表滚动
        mBinding.bookshelfRefresh.setDisableContentWhenRefresh(true)

        // 初始化适配器
        mBookshelfComicRvAdapter = BookshelfComicRvAdapter { navigate(Fragments.BookComicInfo.name, it.mComic.mPathWord) }
        mBookshelfNovelRvAdapter = BookshelfNovelRvAdapter { navigate(Fragments.BookNovelInfo.name, it.mNovel.mPathWord) }

        // 设置加载动画独占1行，卡片3行
        (mBinding.bookshelfRvComic.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    if (position == mBookshelfComicRvAdapter.itemCount && mBookshelfComicRvAdapter.itemCount > 0) 3 else 1
            }
        (mBinding.bookshelfRvNovel.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    if (position == mBookshelfNovelRvAdapter.itemCount && mBookshelfNovelRvAdapter.itemCount > 0) 3 else 1
            }

        // 设置适配器
        mBinding.bookshelfRvComic.adapter = mBookshelfComicRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mBookshelfComicRvAdapter.retry() })
        mBinding.bookshelfRvNovel.adapter = mBookshelfNovelRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mBookshelfComicRvAdapter.retry() })
    }


    /**
     * ● 初始化事件
     *
     * ● 2023-07-07 21:54:43 周五 下午
     */
    override fun initListener() {

        // 处理双击事件
        parentFragmentManager.setFragmentResultListener("onDoubleTap_Bookshelf", this) { _, _ ->
            val recyclerView: BaseTapScrollRecyclerView = if (mBinding.bookshelfRvComic.isVisible) mBinding.bookshelfRvComic else mBinding.bookshelfRvNovel
            val first = recyclerView.findFisrtVisibleViewPosition()
            if (first > 0) {
                recyclerView.onInterceptScrollRv(toPosition = 0, precisePosition = first)
            } else {
                recyclerView.onInterceptScrollRv(precisePosition = first)
            }
        }

        // 设置容器Fragment的共享结果回调
        parentFragmentManager.setFragmentResultListener(BOOKSHELF, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 2) {
                if (bundle.getBoolean(BaseStrings.ENABLE_DELAY)) {
                    launchDelay(BASE_ANIM_200L) { onCollectState() }
                } else {
                    onCollectState()
                }
            }
        }

        // 登录成功
        parentFragmentManager.setFragmentResultListener(BaseEventEnum.LoginCategories.name, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 2) {
                mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
            } else {
                mBinding.bookshelfTipsEmpty.isGone = true
            }
            when(mBinding.bookshelfButtonGropu.checkedButtonId) {
                mBinding.bookshelfComic.id -> mBinding.bookshelfRvComic.isVisible = true
                mBinding.bookshelfNovel.id -> mBinding.bookshelfRvNovel.isVisible = true
            }
            mBinding.bookshelfRefresh.autoRefresh()
            mBookshelfComicRvAdapter.refresh()
            mBookshelfNovelRvAdapter.refresh()
        }

        // 刷新监听
        mBinding.bookshelfRefresh.setOnRefreshListener {

            // 如果 空书架文本可见 刷新两个适配器 并退出逻辑
            if (mBinding.bookshelfTipsEmpty.isVisible) {
                mBinding.bookshelfTipsEmpty.animateFadeIn()
                when(mBinding.bookshelfButtonGropu.checkedButtonId) {
                    mBinding.bookshelfComic.id -> mBinding.bookshelfRvComic.isVisible = true
                    mBinding.bookshelfNovel.id -> mBinding.bookshelfRvNovel.isVisible = true
                }
                mBookshelfComicRvAdapter.refresh()
                mBookshelfNovelRvAdapter.refresh()
                return@setOnRefreshListener
            }

            // 根据当前页面类型（漫画 、 轻小说）执行对应适配器刷新
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) mBookshelfComicRvAdapter.refresh() else mBookshelfNovelRvAdapter.refresh()
        }

        // 按钮组 点击事件 （漫画、轻小说）
        mBinding.bookshelfButtonGropu.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (BaseUser.CURRENT_USER_TOKEN.isEmpty()) toast(getString(R.string.bookshelf_identity_expired))
            when (checkedId) {
                R.id.bookshelf_comic -> {                                                              // 点击漫画
                    if (isChecked) {                                                                        // 选中
                        if (mBookshelfComicRvAdapter.itemCount == 0) {               // 漫画适配器个数为空
                            mBinding.bookshelfTipsEmpty.animateFadeIn()               // “空书架” 淡入
                            mBinding.bookshelfRvComic.visibility = View.INVISIBLE   // 漫画适配器隐藏
                            mBinding.bookshelfRvNovel.visibility = View.INVISIBLE    // 轻小说适配器隐藏
                            return@addOnButtonCheckedListener                            // 退出事件

                        }

                        // 漫画 适配器不为空 判断 “空书架文本” 是否可见 ，可见的话则 淡出并在动画结束时 设置消失
                        else if (mBinding.bookshelfTipsEmpty.isVisible) mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
                        mBinding.bookshelfRvNovel.animateFadeOutWithEndInVisibility()                                                                       // 轻小说适配器淡出 动画结束时隐藏
                        mBinding.bookshelfRvComic.animateFadeIn()                                                                                                    // 漫画适配器淡入 动画结束时显示

                        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOut()                                           // 漫画总数 可见则淡出
                        mBinding.bookshelfCount.text =
                            getString(R.string.bookshelf_comic_count, mComicCount ?: 0)  // 设置漫画总数文本
                        mBinding.bookshelfCount.animateFadeIn()                                                                                                       // 漫画总数 淡入， 这里淡出淡入数位了一个过渡效果
                    }
                }

                R.id.bookshelf_novel -> {  // 逻辑如上 反着来
                    if (isChecked) {
                        if (mBookshelfNovelRvAdapter.itemCount == 0) {
                            mBinding.bookshelfTipsEmpty.animateFadeIn()
                            mBinding.bookshelfRvComic.visibility = View.INVISIBLE
                            mBinding.bookshelfRvNovel.visibility = View.INVISIBLE
                            return@addOnButtonCheckedListener
                        } else if (mBinding.bookshelfTipsEmpty.isVisible) mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
                        mBinding.bookshelfRvComic.animateFadeOutWithEndInVisibility()
                        mBinding.bookshelfRvNovel.animateFadeIn()
                        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOut()
                        mBinding.bookshelfCount.text =
                            getString(R.string.bookshelf_novel_count, mNovelCount ?: 0)
                        mBinding.bookshelfCount.animateFadeIn()
                    }
                }
            }
        }

        // Toolbar Menu
        mBinding.bookshelfToolbar.setOnMenuItemClickListener { menuItem ->
            BaseEvent.getSIngleInstance().doOnInterval {
                mBsVM.sendGetBookshelfInent(
                    when(menuItem.itemId) {
                        R.id.bookshelf_menu_sort_add -> "-datetime_modifier"
                        R.id.bookshelf_menu_sort_update ->"-datetime_updated"
                        R.id.bookshelf_menu_sort_readed -> "-datetime_browse"
                        else -> return@doOnInterval
                    }
                )
                launchDelay(BASE_ANIM_100L) {
                    mBookshelfComicRvAdapter.submitData(PagingData.empty())
                    onCollectState()
                }
            }
            true
        }
    }


    /**
     * ● 初始化观察者
     *
     * ● 2023-07-07 21:53:56 周五 下午
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        // 接收意图
        mBsVM.onOutput { intent ->
            when (intent) {
                is BookshelfIntent.GetBookshelfComic -> {
                    intent.mBaseViewState
                        .doOnSuccess { if (mBinding.bookshelfRefresh.isRefreshing) mBinding.bookshelfRefresh.finishRefresh() }
                        .doOnError { code, msg ->

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.bookshelfButtonGropu.checkedButtonId != R.id.bookshelf_comic) return@doOnError

                            // 适配器数据为0 则处理错误->隐藏视图
                            if (mBookshelfComicRvAdapter.itemCount == 0) processErrorHideView()

                            // 处理错误
                            processError(code, msg)
                        }
                        .doOnResult {

                            // 漫画数量为空 则设置总数
                            if (mComicCount == null) mComicCount = intent.bookshelfComicResp?.mTotal

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.bookshelfButtonGropu.checkedButtonId != R.id.bookshelf_comic) return@doOnResult

                            // 处理正确结果
                            processResult(intent.bookshelfComicResp ?: return@doOnResult, null)
                        }
                }

                is BookshelfIntent.GetBookshelfNovel -> {
                    intent.mBaseViewState
                        .doOnSuccess { if (mBinding.bookshelfRefresh.isRefreshing) mBinding.bookshelfRefresh.finishRefresh() }
                        .doOnError { code, msg ->

                            // 如果当前按钮组为 轻小说
                            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_novel) {

                                // 适配器数据 0 的逻辑
                                if (mBookshelfNovelRvAdapter.itemCount == 0) processErrorHideView()

                                // 处理错误
                                processError(code, msg)
                            }
                        }
                        .doOnResultSuspend {

                            // 轻小说数量为空 则设置总数
                            if (mNovelCount == null) mNovelCount = intent.bookshelfNovelResp?.mTotal

                            // 如果当前按钮组不为 轻小说 则退出
                            if (mBinding.bookshelfButtonGropu.checkedButtonId != R.id.bookshelf_novel) return@doOnResultSuspend

                            // 处理正确结果
                             processResult(null, intent.bookshelfNovelResp ?: return@doOnResultSuspend)
                        }
                }
            }
        }
    }
}
