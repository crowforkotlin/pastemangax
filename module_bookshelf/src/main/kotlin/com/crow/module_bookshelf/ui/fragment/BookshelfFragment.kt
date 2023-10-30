package com.crow.module_bookshelf.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUserConfig
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.processTokenError
import com.crow.base.copymanga.ui.view.BaseTapScrollRecyclerView
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.findFisrtVisibleViewPosition
import com.crow.base.tools.extensions.ifNull
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.BaseViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_bookshelf.R
import com.crow.module_bookshelf.databinding.BookshelfFragmentBinding
import com.crow.module_bookshelf.model.intent.BookshelfIntent
import com.crow.module_bookshelf.model.resp.BookshelfComicResp
import com.crow.module_bookshelf.model.resp.BookshelfNovelResp
import com.crow.module_bookshelf.ui.adapter.BSComicRvAdapter
import com.crow.module_bookshelf.ui.adapter.BSNovelRvAdapter
import com.crow.module_bookshelf.ui.view.BookshelfViewStub
import com.crow.module_bookshelf.ui.view.bookshelfViewStub
import com.crow.module_bookshelf.ui.viewmodel.BookshelfViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    /**
     * ● Static Area
     *
     * ● 2023-10-22 01:30:51 周日 上午
     * @author crowforkotlin
     */
    companion object { const val BOOKSHELF = "BOOKSHELF" }

    /**
     * ● 书架VM
     *
     * ● 2023-07-01 20:26:12 周六 下午
     */
    private val mVM by viewModel<BookshelfViewModel>()

    /**
     * ● Bookshelf Comic适配器
     *
     * ● 2023-07-07 21:49:53 周五 下午
     */
    private val mComicRvAdapter by lazy { BSComicRvAdapter { navigate(Fragments.BookComicInfo.name, it.mComic.mName, it.mComic.mPathWord) } }

    /**
     * ● Bookshelf Novel适配器
     *
     * ● 2023-07-07 21:50:00 周五 下午
     */
    private val mNovelRvAdapter by lazy { BSNovelRvAdapter { navigate(Fragments.BookNovelInfo.name, it.mNovel.mName, it.mNovel.mPathWord) } }

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
     * ● ViewStub
     *
     * ● 2023-10-30 23:21:20 周一 下午
     * @author crowforkotlin
     */
    private var mViewStub by BaseNotNullVar<BookshelfViewStub>(true)

    /**
     * ● 处理错误时 隐藏控件
     *
     * ● 2023-07-07 21:50:17 周五 下午
     */
    private fun processErrorHideView() {
        if (mBinding.tips.tag == null) return
        mBinding.tips.tag = null
        mViewStub.loadLayout(visible = true, animation = true)
//        mBinding.tips.animateFadeIn()  // “空文本” 可见
        if (mBinding.count.isVisible) mBinding.count.animateFadeOutWithEndInVisibility()          // 隐藏 计数
        if (mBinding.comicList.isVisible) mBinding.comicList.animateFadeOutWithEndInVisibility()  // 隐藏 漫画 Rv
        if (mBinding.novelList.isVisible) mBinding.novelList.animateFadeOutWithEndInVisibility()    // 隐藏 轻小说 Rv
        mBinding.refresh.finishRefresh()   // 完成刷新
    }

    /**
     * ● 处理错误
     *
     * ● 2023-07-07 21:49:45 周五 下午
     */
    private fun processError(code: Int, msg: String?) {

        // 解析地址失败 且 Resumed的状态才提示
        if (code == BaseViewState.Error.UNKNOW_HOST && isResumed) {
            if (mBinding.buttonGroup.checkedButtonId == R.id.comic) mNovelRvAdapter.refresh()
            else mComicRvAdapter.refresh()
        }

        // Token为空不处理 Token错误校验
        else if (BaseUserConfig.CURRENT_USER_TOKEN.isEmpty()) {
            if (isResumed) {
                toast(getString(R.string.bookshelf_identity_expired))
                lifecycleScope.launch {
                    if (mComicRvAdapter.itemCount != 0 || mNovelRvAdapter.itemCount != 0) {
                        async {
                            mComicRvAdapter.submitData(PagingData.empty())
                            mNovelRvAdapter.submitData(PagingData.empty())
                        }.await()
                    }
                    mViewStub.loadLayout(visible = true, animation = true)
                }
            }
            return
        }

        // 处理Token错误校验
        else mBinding.root.processTokenError(code, msg,
            doOnCancel = {
                mComicRvAdapter.retry()
                mNovelRvAdapter.retry()
                parentFragmentManager.setFragmentResult(BaseEventEnum.ClearUserInfo.name, arguments ?: Bundle())
            },
            doOnConfirm = {
                val tag = Fragments.Login.name
                requireParentFragment().parentFragmentManager.navigateToWithBackStack(
                    id = baseR.id.app_main_fcv,
                    hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
                    addedTarget = get(named(tag)),
                    tag = tag,
                    backStackName = tag
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
//        if (mBinding.tips.isGone || mBinding.tips.tag != null) return
        mViewStub.isVisible().log()
        mViewStub.isGone().log()
        if (mViewStub.isGone() || mBinding.tips.tag != null) return
        mBinding.tips.tag = Unit
//        mBinding.tips.animateFadeOutWithEndInVisible()
        mViewStub.loadLayout(visible = false,  animation = true)
        mBinding.count.animateFadeIn()
        bookshelfRv.animateFadeIn()
    }

    /**
     * ● 处理结果
     *
     * ● 2023-07-07 21:50:30 周五 下午
     */
    private fun processResult(comicResp: BookshelfComicResp?, novelResp: BookshelfNovelResp?) {
        when(mBinding.buttonGroup.checkedButtonId) {
            mBinding.comic.id -> {
                if (comicResp?.mList?.isEmpty() == true) {
                    mViewStub.loadLayout(visible = true, animation = true)
                    toast(getString(R.string.bookshelf_empty_comic))
                    return
                }
                processResultView(mBinding.comicList)
                comicResp?.let { mBinding.count.text =  getString(R.string.bookshelf_comic_count, mComicCount ?: -1) }
            }
            mBinding.novel.id -> {
                if (novelResp?.mList?.isEmpty() == true) {
                    mViewStub.loadLayout(visible = true, animation = true)
                    toast(getString(R.string.bookshelf_empty_novel))
                    return
                }
                processResultView(mBinding.novelList)
                novelResp?.let { mBinding.count.text = getString(R.string.bookshelf_novel_count, mNovelCount ?: -1) }
            }
        }
    }
    
    /**
     * ● 导航
     *
     * ● 2023-07-07 21:51:20 周五 下午
     */
    private fun navigate(tag: String, name: String, pathword: String) {
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        bundle.putSerializable(BaseStrings.NAME, name)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            id = baseR.id.app_main_fcv,
            hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            addedTarget= get<Fragment>(named(tag)).also { it.arguments = bundle },
            tag = tag,
            backStackName = tag
        )
    }

    /**
     * ● Mvi Intent Output
     *
     * ● 2023-07-07 21:55:12 周五 下午
     */
    private fun onCollectState() {

        if (mVM.mBookshelfComicFlowPager == null && mVM.mBookshelfNovelFlowPager == null) {

            // 发送获取书架 “漫画” 的意图 需要动态收集书架状态才可
            mVM.input(BookshelfIntent.GetBookshelfComic())

            // 发送获取书架 “轻小说” 的意图 需要动态收集书架状态才可
            mVM.input(BookshelfIntent.GetBookshelfNovel())
        }


        // 每个观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
        repeatOnLifecycle {
            mVM.mBookshelfComicFlowPager?.collect { data ->
                mComicRvAdapter.submitData(data)
            }
        }

        // 收集书架 轻小说Pager状态
        repeatOnLifecycle {
            mVM.mBookshelfNovelFlowPager?.collect { data ->
                mNovelRvAdapter.submitData(data)
            }
       }
    }


    private fun initNovelRvAdapter() {

        // 初始化适配器
        mBinding.novelList.adapter = mNovelRvAdapter

        // 设置加载动画独占1行，卡片3行
        (mBinding.novelList.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = if (position == mNovelRvAdapter.itemCount && mNovelRvAdapter.itemCount > 0) 3 else 1
        }

        // 添加Footer
        mBinding.novelList.adapter = mNovelRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mNovelRvAdapter.retry() })
    }


    private fun initComicRvAdapter() {

        // 初始化适配器
        mBinding.comicList.adapter = mComicRvAdapter

        // 设置加载动画独占1行，卡片3行
        (mBinding.comicList.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = if (position == mComicRvAdapter.itemCount && mComicRvAdapter.itemCount > 0) 3 else 1
        }

        // 添加Footer
        mBinding.comicList.adapter = mComicRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mComicRvAdapter.retry() })
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
     * ● 初始化视图
     *
     * ● 2023-07-07 21:54:34 周五 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        mViewStub = bookshelfViewStub(mBinding.tips, lifecycle) { }

        // 设置刷新时不允许列表滚动
        mBinding.refresh.setDisableContentWhenRefresh(true)

        // Init Rv Adapter
        when(mBinding.buttonGroup.checkedButtonId) {
            R.id.comic ->initComicRvAdapter()
            R.id.novel -> initNovelRvAdapter()
        }
    }

    /**
     * ● 初始化事件
     *
     * ● 2023-07-07 21:54:43 周五 下午
     */
    override fun initListener() {

        // 处理双击事件
        parentFragmentManager.setFragmentResultListener("onDoubleTap_Bookshelf", this) { _, _ ->
            val recyclerView: BaseTapScrollRecyclerView = if (mBinding.comicList.isVisible) mBinding.comicList else mBinding.novelList
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
                mBinding.refresh.autoRefreshAnimationOnly()
                mBinding.refresh.finishRefresh((BASE_ANIM_300L.toInt() shl 1) or 0xFF)
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
                mViewStub.loadLayout(visible = false, animation = true)
            } else {
                mViewStub.loadLayout(visible = false, animation = false)
            }
            when(mBinding.buttonGroup.checkedButtonId) {
                mBinding.comic.id -> mBinding.comicList.isVisible = true
                mBinding.novel.id -> mBinding.novelList.isVisible = true
            }
            mBinding.refresh.autoRefresh()
            mComicRvAdapter.refresh()
            mNovelRvAdapter.refresh()
        }

        // 刷新监听
        mBinding.refresh.setOnRefreshListener {

            // 如果 空书架文本可见 刷新两个适配器 并退出逻辑
            if (mViewStub.isVisible()) {
                when(mBinding.buttonGroup.checkedButtonId) {
                    mBinding.comic.id -> mBinding.comicList.isVisible = true
                    mBinding.novel.id -> mBinding.novelList.isVisible = true
                }
                mComicRvAdapter.refresh()
                mNovelRvAdapter.refresh()
                return@setOnRefreshListener
            }

            // 根据当前页面类型（漫画 、 轻小说）执行对应适配器刷新
            if (mBinding.buttonGroup.checkedButtonId == R.id.comic) mComicRvAdapter.refresh() else mNovelRvAdapter.refresh()
        }

        // 按钮组 点击事件 （漫画、轻小说）
        mBinding.buttonGroup.addOnButtonCheckedListener { _, checkedId, buttonChecked ->
            if (BaseUserConfig.CURRENT_USER_TOKEN.isEmpty()) toast(getString(R.string.bookshelf_identity_expired))
            when (checkedId) {

                R.id.comic -> {
                    if (buttonChecked) {

                        // 适配器为空则初始化漫画适配器
                        mBinding.comicList.adapter.ifNull(::initComicRvAdapter)

                        // 漫画适配器个数为空 - Lottie淡入 - 隐藏漫画轻小说列表
                        if (mComicRvAdapter.itemCount == 0) {
                            mViewStub.loadLayout(visible = true, animation = true)
                            mBinding.comicList.isInvisible = true
                            mBinding.novelList.isInvisible = true
                            return@addOnButtonCheckedListener
                        }

                        // 漫画 适配器不为空 判断 “ViewStub” 是否可见 ，可见的话则 淡出并在动画结束时 设置消失
                        else if (mViewStub.isVisible()) { mViewStub.loadLayout(visible = false, animation = true) }

                        // 轻小说适配器淡出 动画结束时隐藏
                        mBinding.novelList.animateFadeOutWithEndInVisibility()

                        // 漫画适配器淡入 动画结束时显示
                        mBinding.comicList.animateFadeIn()

                        // 漫画总数 可见则淡出
                        if (mBinding.count.isVisible) { mBinding.count.animateFadeOut() }

                        // 设置漫画总数文本
                        mBinding.count.text = getString(R.string.bookshelf_comic_count, mComicCount ?: 0)

                        // 漫画总数 淡入， 这里淡出淡入给予一个过渡效果
                        mBinding.count.animateFadeIn()
                    }
                }

                R.id.novel -> {
                    if (buttonChecked) {

                        mBinding.novelList.adapter.ifNull(::initNovelRvAdapter)

                        if (mNovelRvAdapter.itemCount == 0) {
                            mViewStub.loadLayout(visible = true, animation = true)
                            mBinding.comicList.visibility = View.INVISIBLE
                            mBinding.novelList.visibility = View.INVISIBLE
                            return@addOnButtonCheckedListener
                        }

                        else if (mViewStub.isVisible()) { mViewStub.loadLayout(visible = false, animation = true) }

                        mBinding.comicList.animateFadeOutWithEndInVisibility()
                        mBinding.novelList.animateFadeIn()

                        if (mBinding.count.isVisible) { mBinding.count.animateFadeOut() }
                        mBinding.count.text = getString(R.string.bookshelf_novel_count, mNovelCount ?: 0)
                        mBinding.count.animateFadeIn()
                    }
                }
            }
        }

        // Toolbar Menu
        mBinding.topbar.setOnMenuItemClickListener { menuItem ->
            BaseEvent.getSIngleInstance().doOnInterval {
                mVM.sendGetBookshelfInent(
                    when(menuItem.itemId) {
                        R.id.bookshelf_menu_sort_add -> "-datetime_modifier"
                        R.id.bookshelf_menu_sort_update ->"-datetime_updated"
                        R.id.bookshelf_menu_sort_readed -> "-datetime_browse"
                        else -> return@doOnInterval
                    }
                )
                launchDelay(BASE_ANIM_100L) {
                    mComicRvAdapter.submitData(PagingData.empty())
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
        mVM.onOutput { intent ->
            when (intent) {
                is BookshelfIntent.GetBookshelfComic -> {
                    intent.mViewState
                        .doOnSuccess { if (mBinding.refresh.isRefreshing) mBinding.refresh.finishRefresh() }
                        .doOnError { code, msg ->

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.buttonGroup.checkedButtonId != R.id.comic) return@doOnError

                            // 适配器数据为0 则处理错误->隐藏视图
                            if (mComicRvAdapter.itemCount == 0) processErrorHideView()

                            // 处理错误
                            processError(code, msg)
                        }
                        .doOnResult {

                            // 漫画数量为空 则设置总数
                            if (mComicCount == null) mComicCount = intent.bookshelfComicResp?.mTotal

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.buttonGroup.checkedButtonId != R.id.comic) return@doOnResult

                            // 处理正确结果
                            processResult(intent.bookshelfComicResp ?: return@doOnResult, null)
                        }
                }

                is BookshelfIntent.GetBookshelfNovel -> {
                    intent.mViewState
                        .doOnSuccess { if (mBinding.refresh.isRefreshing) mBinding.refresh.finishRefresh() }
                        .doOnError { code, msg ->

                            // 如果当前按钮组为 轻小说
                            if (mBinding.buttonGroup.checkedButtonId == R.id.novel) {

                                // 适配器数据 0 的逻辑
                                if (mNovelRvAdapter.itemCount == 0) processErrorHideView()

                                // 处理错误
                                processError(code, msg)
                            }
                        }
                        .doOnResult {

                            // 轻小说数量为空 则设置总数
                            if (mNovelCount == null) mNovelCount = intent.bookshelfNovelResp?.mTotal

                            // 如果当前按钮组不为 轻小说 则退出
                            if (mBinding.buttonGroup.checkedButtonId != R.id.novel) return@doOnResult

                            // 处理正确结果
                             processResult(null, intent.bookshelfNovelResp ?: return@doOnResult)
                        }
                }
            }
        }
    }
}
