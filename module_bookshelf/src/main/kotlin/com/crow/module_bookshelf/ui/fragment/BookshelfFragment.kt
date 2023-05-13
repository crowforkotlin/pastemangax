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
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.processTokenError
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisible
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.getStatusBarHeight
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.doOnError
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

    companion object { fun newInstance() = BookshelfFragment() }

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
        mBinding.bookshelfTipsEmpty.animateFadeIn()  // “空文本” 可见
        if(mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOutWithEndInVisibility()          // 隐藏 计数
        if(mBinding.bookshelfRvComic.isVisible) mBinding.bookshelfRvComic.animateFadeOutWithEndInVisibility()  // 隐藏 漫画 Rv
        if(mBinding.bookshelfRvNovel.isVisible) mBinding.bookshelfRvNovel.animateFadeOutWithEndInVisibility()    // 隐藏 轻小说 Rv
        mBinding.bookshelfRefresh.finishRefresh()   // 完成刷新
    }

    // 处理错误
    private fun processError(code: Int, msg: String?) {

        // 解析地址失败 且 Resumed的状态才提示
        if (code == ViewState.Error.UNKNOW_HOST && isResumed) {
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
                FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).post(lifecycleScope, Unit)
            },
            doOnConfirm = {
                requireParentFragment().parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv, this, get(named(Fragments.Login)), Fragments.Login.toString(), Fragments.Login.toString())
                FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).post(lifecycleScope, Unit)
            }
        )
    }

    // 处理结果
    private fun processResult(bookshelfComicResp: BookshelfComicResp?, bookshelfNovelResp: BookshelfNovelResp?) {

        // “空提示” 文本不可见
        if (mBinding.bookshelfTipsEmpty.isVisible) {

            if (bookshelfComicResp == null) mBinding.bookshelfRvComic.visibility = View.INVISIBLE                  // 漫画 Rv 隐藏
            else {
                if (mBinding.bookshelfTipsEmpty.isVisible) mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()           // 让 “空提示”文本 消失
                mBinding.bookshelfRvComic.animateFadeIn()                                                                            // 漫画 Rv 淡入
            }

            if (bookshelfNovelResp == null) mBinding.bookshelfRvNovel.visibility = View.INVISIBLE                  // 轻小说 Rv 隐藏
            else {
                if (mBinding.bookshelfTipsEmpty.isVisible) mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()  // 让 “空提示”文本 消失
                mBinding.bookshelfRvNovel.animateFadeIn()                                                                             // 轻小说 Rv 淡入
            }

            // 计数 淡入
            mBinding.bookshelfCount.animateFadeIn()

            // 设置漫画总数
            mBinding.bookshelfCount.text = if (bookshelfComicResp != null) getString(R.string.bookshelf_comic_count, mComicCount ?: -1) else getString(R.string.bookshelf_novel_count, mNovelCount ?: -1)
        }

        // 正在刷新？
        if(mBinding.bookshelfRefresh.isRefreshing) {


            mHandler.postDelayed({
                mBinding.bookshelfRefresh.finishRefresh() // 取消刷新
                if (bookshelfComicResp != null) {
                    mBinding.bookshelfRvComic.smoothScrollToPosition(0)
                    mBinding.bookshelfCount.text = getString(R.string.bookshelf_comic_count, bookshelfComicResp.mTotal)
                    mBinding.bookshelfCount.animateFadeIn()
                } else {
                    mBinding.bookshelfRvNovel.smoothScrollToPosition(0)
                    mBinding.bookshelfCount.text = getString(R.string.bookshelf_novel_count, bookshelfNovelResp!!.mTotal)
                    mBinding.bookshelfCount.animateFadeIn()
                }
            }, BASE_ANIM_300L)
        }
    }

    private fun navigateBookComicInfo(pathword: String) = navigate(Fragments.BookComicInfo, pathword)
    private fun navigateBookNovelInfo(pathword: String) = navigate(Fragments.BookNovelInfo, pathword)
    private fun navigate(tag: Enum<Fragments>, pathword: String) {
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.toString())!!,
            get<Fragment>(named(tag)).also { it.arguments = bundle }, tag.toString(), tag.toString()
        )
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
            repeatOnLifecycle { mBsVM.mBookshelfComicFlowPager?.collect { data -> mBookshelfComicRvAdapter.submitData(data) } }
            repeatOnLifecycle { mBsVM.mBookshelfNovelFlowPager?.collect { data -> mBookshelfNovelRvAdapter.submitData(data) } }
        }
        mBinding.bookshelfTipsEmpty.animateFadeIn()
    }

    override fun getViewBinding(inflater: LayoutInflater) = BookshelfFragmentBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.bookshelfBar.setPadding(0, mContext.getStatusBarHeight(), 0, 0)

        // 设置刷新时不允许列表滚动
        mBinding.bookshelfRefresh.setDisableContentWhenRefresh(true)

        // 初始化适配器
        mBookshelfComicRvAdapter = BookshelfComicRvAdapter { navigateBookComicInfo(it.mComic.mPathWord) }
        mBookshelfNovelRvAdapter = BookshelfNovelRvAdapter { navigateBookNovelInfo(it.mNovel.mPathWord) }

        // 设置加载动画独占1行，卡片3行
        (mBinding.bookshelfRvComic.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() { override fun getSpanSize(position: Int)= if (position == mBookshelfComicRvAdapter.itemCount  && mBookshelfComicRvAdapter.itemCount > 0) 3 else 1 }
        (mBinding.bookshelfRvNovel.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() { override fun getSpanSize(position: Int) = if (position == mBookshelfNovelRvAdapter.itemCount  && mBookshelfNovelRvAdapter.itemCount > 0) 3 else 1 }

        // 设置适配器
        mBinding.bookshelfRvComic.adapter = mBookshelfComicRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mBookshelfComicRvAdapter.retry() })
        mBinding.bookshelfRvNovel.adapter = mBookshelfNovelRvAdapter.withLoadStateFooter(BaseLoadStateAdapter { mBookshelfComicRvAdapter.retry() })
    }

    override fun initListener() {

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

        // 移至顶部 点击事件
        mBinding.bookshelfMoveTop.doOnClickInterval {

            // 点击书架的当前类型为“漫画” 并且漫画适配器个数不为0 则滑动至顶部
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) if (mBookshelfComicRvAdapter.itemCount != 0) mBinding.bookshelfRvComic.smoothScrollToPosition(0)

            // 否则就是轻小说 并且 轻小说适配器个数不为0 则滑动至顶部
            else if(mBookshelfNovelRvAdapter.itemCount != 0) mBinding.bookshelfRvNovel.smoothScrollToPosition(0)
        }

        // 移至底部 点击事件
        mBinding.bookshelfMoveBottom.doOnClickInterval {

            // 点击漫画 并且漫画适配器个数不为0 则滑动至 适配器总数 - 1
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) if (mBookshelfComicRvAdapter.itemCount != 0) mBinding.bookshelfRvComic.smoothScrollToPosition(mBookshelfComicRvAdapter.itemCount - 1)

            // 否则就是轻小说，轻小说适配器个数不为0 则滑动至 适配器总数 - 1
            else if(mBookshelfNovelRvAdapter.itemCount != 0) mBinding.bookshelfRvNovel.smoothScrollToPosition(mBookshelfNovelRvAdapter.itemCount - 1)
        }

        // 按钮组 点击事件 （漫画、轻小说）
        mBinding.bookshelfButtonGropu.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when(checkedId) {
                R.id.bookshelf_comic -> {                                                              // 点击漫画
                    if (isChecked) {                                                                        // 选中
                        if (mBookshelfComicRvAdapter.itemCount == 0) {               // 漫画适配器个数为空
                            mBinding.bookshelfTipsEmpty.animateFadeIn()               // “空书架” 淡入
                            mBinding.bookshelfRvComic.visibility = View.INVISIBLE   // 漫画适配器隐藏
                            mBinding.bookshelfRvNovel.visibility = View.INVISIBLE    // 轻小说适配器隐藏
                            return@addOnButtonCheckedListener                            // 退出事件
                        }

                        // 漫画 适配器不为空 判断 “空书架文本” 是否可见 ，可见的话则 淡出并在动画结束时 设置消失
                        else if(mBinding.bookshelfTipsEmpty.isVisible) mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
                        mBinding.bookshelfRvNovel.animateFadeOutWithEndInVisibility()                                                                       // 轻小说适配器淡出 动画结束时隐藏
                        mBinding.bookshelfRvComic.animateFadeIn()                                                                                                    // 漫画适配器淡入 动画结束时显示

                        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOut()                                           // 漫画总数 可见则淡出
                        mBinding.bookshelfCount.text = getString(R.string.bookshelf_comic_count, mComicCount ?: 0)  // 设置漫画总数文本
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
                        } else if(mBinding.bookshelfTipsEmpty.isVisible) mBinding.bookshelfTipsEmpty.animateFadeOutWithEndInVisible()
                        mBinding.bookshelfRvComic.animateFadeOutWithEndInVisibility()
                        mBinding.bookshelfRvNovel.animateFadeIn()
                        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOut()
                        mBinding.bookshelfCount.text = getString(R.string.bookshelf_novel_count, mNovelCount ?: 0)
                        mBinding.bookshelfCount.animateFadeIn()
                    }
                }
            }
        }
    }

    override fun initObserver() {

        // 每隔观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
        repeatOnLifecycle { mBsVM.mBookshelfComicFlowPager?.collect { data -> mBookshelfComicRvAdapter.submitData(data) } }

        // 收集书架 轻小说Pager状态
        repeatOnLifecycle { mBsVM.mBookshelfNovelFlowPager?.collect { data -> mBookshelfNovelRvAdapter.submitData(data) } }

        // 接收意图
        mBsVM.onOutput { intent ->
            when(intent) {
                is BookshelfIntent.GetBookshelfComic -> {
                    intent.mViewState
                        .doOnSuccess { if (mBinding.bookshelfRefresh.isRefreshing) mBinding.bookshelfRefresh.finishRefresh() }
                        .doOnResultSuspend {

                            // 漫画数量为空 则设置总数
                            if (mComicCount == null) mComicCount = intent.bookshelfComicResp?.mTotal

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.bookshelfButtonGropu.checkedButtonId != R.id.bookshelf_comic) return@doOnResultSuspend

                            // 处理正确结果
                            processResult(intent.bookshelfComicResp, null)
                        }
                        .doOnError { code, msg ->

                            // 如果当前按钮组不为 漫画 则退出
                            if (mBinding.bookshelfButtonGropu.checkedButtonId != R.id.bookshelf_comic) return@doOnError

                            // 适配器数据 0 的逻辑
                            if (mBookshelfComicRvAdapter.itemCount == 0) processErrorHideView()

                            // 处理错误
                            processError(code, msg)
                        }
                }
                is BookshelfIntent.GetBookshelfNovel -> {
                    intent.mViewState
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
                            processResult(null, intent.bookshelfNovelResp)
                        }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBsVM.input(BookshelfIntent.GetBookshelfComic())    // 发送获取书架 “漫画” 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfNovel())    // 发送获取书架 “轻小说” 的意图 需要动态收集书架状态才可
    }
}
