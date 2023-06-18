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
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.processTokenError
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisible
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
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

    companion object {
        fun newInstance() = BookshelfFragment()
    }

    // 书架VM
    private val mBsVM by viewModel<BookshelfViewModel>()

    // Bookshelf Comic适配器
    private lateinit var mBookshelfComicRvAdapter: BookshelfComicRvAdapter

    // Bookshelf Novel适配器
    private lateinit var mBookshelfNovelRvAdapter: BookshelfNovelRvAdapter

    // 漫画计数
    private var mComicCount: Int? = null

    // 轻小说计数
    private var mNovelCount: Int? = null

    // 处理错误时 隐藏控件
    private fun processErrorHideView() {
        if (mBinding.bookshelfTipsEmpty.tag == null) return
        mBinding.bookshelfTipsEmpty.tag = null
        mBinding.bookshelfTipsEmpty.animateFadeIn()  // “空文本” 可见
        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOutWithEndInVisibility()          // 隐藏 计数
        if (mBinding.bookshelfRvComic.isVisible) mBinding.bookshelfRvComic.animateFadeOutWithEndInVisibility()  // 隐藏 漫画 Rv
        if (mBinding.bookshelfRvNovel.isVisible) mBinding.bookshelfRvNovel.animateFadeOutWithEndInVisibility()    // 隐藏 轻小说 Rv
        mBinding.bookshelfRefresh.finishRefresh()   // 完成刷新
    }

    // 处理错误
    private fun processError(code: Int, msg: String?) {

        // 解析地址失败 且 Resumed的状态才提示
        if (code == BaseViewState.Error.UNKNOW_HOST && isResumed) {
            mBinding.bookshelfFrameRv.showSnackBar(msg ?: getString(baseR.string.BaseLoadingError))
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
                FlowBus.with<Unit>(BaseEventEnum.ClearUserInfo.name).post(lifecycleScope, Unit)
            },
            doOnConfirm = {
                requireParentFragment().parentFragmentManager.navigateToWithBackStack(
                    baseR.id.app_main_fcv,
                    requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
                    get(named(Fragments.Login.name)),
                    Fragments.Login.name,
                    Fragments.Login.name
                )
                FlowBus.with<Unit>(BaseEventEnum.ClearUserInfo.name).post(lifecycleScope, Unit)
            }
        )
    }

    // 处理结果
    private fun processResult(bookshelfComicResp: BookshelfComicResp?, bookshelfNovelResp: BookshelfNovelResp?) {
        if (mBinding.bookshelfButtonGropu.checkedButtonId == mBinding.bookshelfComic.id) {
            if (bookshelfComicResp == null) return
        } else {
            if (bookshelfNovelResp == null) return
        }
        if (!mBinding.bookshelfTipsEmpty.isVisible || mBinding.bookshelfTipsEmpty.tag != null) return
        mBinding.bookshelfTipsEmpty.tag = Unit
        mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
        mBinding.bookshelfCount.animateFadeIn()
        if (bookshelfComicResp != null) {
            mBinding.bookshelfCount.text =  getString(R.string.bookshelf_comic_count, mComicCount ?: -1)
            mBinding.bookshelfRvComic.animateFadeIn()
        } else {
            mBinding.bookshelfCount.text = getString(R.string.bookshelf_novel_count, mNovelCount ?: -1)
            mBinding.bookshelfRvNovel.animateFadeIn()
        }
    }

    private fun processBookshelfResponse(response: Any?, view: View) {
        when {
            response == null -> {
                view.isInvisible = true
            }
            else -> {

            }
        }
        mBsVM.mInitOk = true
    }

    private fun navigateBookComicInfo(pathword: String) =
        navigate(Fragments.BookComicInfo.name, pathword)

    private fun navigateBookNovelInfo(pathword: String) =
        navigate(Fragments.BookNovelInfo.name, pathword)

    private fun navigate(tag: String, pathword: String) {
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            get<Fragment>(named(tag)).also { it.arguments = bundle }, tag, tag
        )
    }

    private fun onOutput() {
        // 每个观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
        repeatOnLifecycle { mBsVM.mBookshelfComicFlowPager?.collect { data -> mBookshelfComicRvAdapter.submitData(data) } }

        // 收集书架 轻小说Pager状态
        repeatOnLifecycle { mBsVM.mBookshelfNovelFlowPager?.collect { data -> mBookshelfNovelRvAdapter.submitData(data) } }
    }

    // 暴露的函数 当登录成功、退出登录时 ContainerFragment可调用该函数
    fun doRefresh() {
        mBinding.bookshelfRefresh.autoRefresh()
        mBookshelfComicRvAdapter.refresh()
        mBookshelfNovelRvAdapter.refresh()
    }

    fun doExitFromUser() {
        mBinding.bookshelfCount.isInvisible = true
        mBinding.bookshelfRvComic.isInvisible = true
        mBinding.bookshelfRvNovel.isInvisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            mBookshelfComicRvAdapter.submitData(PagingData.empty())
            mBookshelfNovelRvAdapter.submitData(PagingData.empty())

            // 每个FlowPager观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
            repeatOnLifecycle {
                mBsVM.mBookshelfComicFlowPager?.collect { data ->
                    mBookshelfComicRvAdapter.submitData(
                        data
                    )
                }
            }
            repeatOnLifecycle {
                mBsVM.mBookshelfNovelFlowPager?.collect { data ->
                    mBookshelfNovelRvAdapter.submitData(
                        data
                    )
                }
            }
        }
        mBinding.bookshelfTipsEmpty.animateFadeIn()
    }

    override fun getViewBinding(inflater: LayoutInflater) =
        BookshelfFragmentBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 发送获取书架 “漫画” 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfComic())

        // 发送获取书架 “轻小说” 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfNovel())
    }

    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.bookshelfToolbar.immersionPadding(hideNaviateBar = false)

        // 设置刷新时不允许列表滚动
        mBinding.bookshelfRefresh.setDisableContentWhenRefresh(true)

        // 初始化适配器
        mBookshelfComicRvAdapter = BookshelfComicRvAdapter { navigateBookComicInfo(it.mComic.mPathWord) }
        mBookshelfNovelRvAdapter = BookshelfNovelRvAdapter { navigateBookNovelInfo(it.mNovel.mPathWord) }

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

    override fun initListener() {

        // 设置容器Fragment的共享结果回调
        parentFragmentManager.setFragmentResultListener("Bookshelf", this) { _, bundle ->
            if (bundle.getInt("id") == 2) {
                if (bundle.getBoolean("delay")) {
                    launchDelay(BASE_ANIM_200L) { onOutput() }
                } else {
                    onOutput()
                }
            }
        }

        // 刷新监听
        mBinding.bookshelfRefresh.setOnRefreshListener {

            // 如果 空书架文本可见 刷新两个适配器 并退出逻辑
            if (mBinding.bookshelfTipsEmpty.isVisible) {
                mBookshelfComicRvAdapter.refresh()
                mBookshelfNovelRvAdapter.refresh()
                return@setOnRefreshListener
            }

            // 根据当前页面类型（漫画 、 轻小说）执行对应适配器刷新
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) mBookshelfComicRvAdapter.refresh() else mBookshelfNovelRvAdapter.refresh()
        }

        // 按钮组 点击事件 （漫画、轻小说）
        mBinding.bookshelfButtonGropu.addOnButtonCheckedListener { _, checkedId, isChecked ->
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
    }

    override fun initObserver(savedInstanceState: Bundle?) {

        // 接收意图
        mBsVM.onOutput { intent ->
            when (intent) {
                is BookshelfIntent.GetBookshelfComic -> {
                    intent.mBaseViewState
                        .doOnSuccess { if (mBinding.bookshelfRefresh.isRefreshing) mBinding.bookshelfRefresh.finishRefresh() }
                        .doOnError { code, msg ->

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.bookshelfButtonGropu.checkedButtonId != R.id.bookshelf_comic) return@doOnError

                            // 适配器数据 0 的逻辑
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

    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.clearFragmentResultListener("Bookshelkf")
    }
}
