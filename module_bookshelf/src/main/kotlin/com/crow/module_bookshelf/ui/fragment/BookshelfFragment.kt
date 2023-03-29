package com.crow.module_bookshelf.ui.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.current_project.entity.BookType
import com.crow.base.current_project.processTokenError
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.*
import com.crow.module_bookshelf.R
import com.crow.module_bookshelf.databinding.BookshelfFragmentBinding
import com.crow.module_bookshelf.model.intent.BookshelfIntent
import com.crow.module_bookshelf.model.resp.BookshelfComicResp
import com.crow.module_bookshelf.model.resp.BookshelfNovelResp
import com.crow.module_bookshelf.ui.adapter.BookshelfComicRvAdapter
import com.crow.module_bookshelf.ui.adapter.BookshelfNovelRvAdapter
import com.crow.module_bookshelf.ui.viewmodel.BookshelfViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @RelativePath: com\crow\module_bookshelf\ui\fragment\BookShelfFragment.kt
 * @Path: D:\Programing\Android\2023\CopyManga\module_bookshelf\src\main\kotlin\com\crow\module_bookshelf\ui\fragment\BookShelfFragment.kt
 * @Author: CrowForKotlin
 * @Time: 2023/3/22 23:56 Wed PM
 * @Description:BookShelfFragment
 * @formatter:on
 *************************/

class BookshelfFragment : BaseMviFragment<BookshelfFragmentBinding>() {

    // 共享书架VM
    private val mBsVM by sharedViewModel<BookshelfViewModel>()

    // Bookshelf Comic适配器
    private lateinit var mBookshelfComicRvAdapter: BookshelfComicRvAdapter

    // Bookshelf Novel适配器
    private lateinit var mBookshelfNovelRvAdapter: BookshelfNovelRvAdapter

    private var mComicCount: Int? = null

    private var mNovelCount: Int? = null

    private fun processErrorHideView() {
        mBinding.bookshelfText.animateFadeIn()  // “空文本” 可见
        if(mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOut().withEndAction { mBinding.bookshelfCount.visibility = View.INVISIBLE }      // 隐藏 计数
        if(mBinding.bookshelfRvComic.isVisible) mBinding.bookshelfRvComic.animateFadeOut().withEndAction { mBinding.bookshelfRvComic.visibility = View.INVISIBLE }  // 隐藏 漫画 Rv
        if(mBinding.bookshelfRvNovel.isVisible) mBinding.bookshelfRvNovel.animateFadeOut().withEndAction { mBinding.bookshelfRvNovel.visibility = View.INVISIBLE }  // 隐藏 轻小说 Rv
        mBinding.bookshelfRefresh.finishRefresh()   // 完成刷新
    }

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
                mBookshelfComicRvAdapter.refresh()
                FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).post(lifecycleScope, Unit)
            },
            doOnConfirm = {
                navigate(baseR.id.mainUserloginfragment)
                FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).post(lifecycleScope, Unit)
            }
        )
    }

    private fun processResult(bookshelfComicResp: BookshelfComicResp?, bookshelfNovelResp: BookshelfNovelResp?) {

        // “空提示” 文本不可见
        if (mBinding.bookshelfText.isVisible) {


            mBinding.bookshelfText.visibility = View.GONE               // 让 “空提示”文本 消失
            if (bookshelfComicResp == null)
                mBinding.bookshelfRvComic.visibility = View.INVISIBLE   // 漫画 Rv 隐藏
            else
                mBinding.bookshelfRvComic.animateFadeIn()               // 漫画 Rv 淡入
            if (bookshelfNovelResp == null)
                mBinding.bookshelfRvNovel.visibility = View.INVISIBLE   // 轻小说 Rv 隐藏
            else
                mBinding.bookshelfRvNovel.animateFadeIn()               // 轻小说 Rv 淡入

            mBinding.bookshelfCount.animateFadeIn()                     // 计数 淡入

            // 设置漫画总数
            mBinding.bookshelfCount.text = if (bookshelfComicResp != null) getString(R.string.bookshelf_comic_count, mComicCount ?: -1) else getString(R.string.bookshelf_novel_count, mNovelCount ?: -1)
        }

        // 正在刷新？
        if(mBinding.bookshelfRefresh.isRefreshing) {

            // 取消刷新
            mBinding.bookshelfRefresh.finishRefresh()

            // Toast Tips
            toast(getString(baseR.string.BaseRefreshScucess))
        }
    }

    fun doRefresh() {

        // 开启刷新动画
        mBinding.bookshelfRefresh.autoRefresh()

        // 刷新
        mBookshelfComicRvAdapter.refresh()
        mBookshelfNovelRvAdapter.refresh()
    }

    fun doExitUser() {
        mBinding.bookshelfRefresh.autoRefresh()
        mBookshelfComicRvAdapter.refresh()
        mBookshelfNovelRvAdapter.refresh()
    }

    override fun getViewBinding(inflater: LayoutInflater) = BookshelfFragmentBinding.inflate(inflater)

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.bookshelfBar.setPadding(0, mContext.getStatusBarHeight(), 0, 0)

        // 设置刷新时不允许列表滚动
        mBinding.bookshelfRefresh.setDisableContentWhenRefresh(true)

        // 初始化适配器
        mBookshelfComicRvAdapter = BookshelfComicRvAdapter {
            FlowBus.with<BookTapEntity>(BaseStrings.Key.OPEN_COMIC_INFO).post(lifecycleScope, BookTapEntity(BookType.Comic, it.mComic.mPathWord))
        }
        mBookshelfNovelRvAdapter = BookshelfNovelRvAdapter {
            FlowBus.with<BookTapEntity>(BaseStrings.Key.OPEN_COMIC_INFO).post(lifecycleScope, BookTapEntity(BookType.Novel, it.mNovel.mPathWord))
        }

        // 设置适配器
        mBinding.bookshelfRvComic.adapter = mBookshelfComicRvAdapter
        mBinding.bookshelfRvNovel.adapter = mBookshelfNovelRvAdapter

    }

    override fun initListener() {

        // 根据当前页面类型（漫画 、 轻小说）执行对应适配器刷新
        mBinding.bookshelfRefresh.setOnRefreshListener {
            if (mBinding.bookshelfText.isVisible) {
                mBookshelfComicRvAdapter.refresh()
                mBookshelfNovelRvAdapter.refresh()
                return@setOnRefreshListener
            }
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) mBookshelfComicRvAdapter.refresh()
            else mBookshelfNovelRvAdapter.refresh()
        }

        // 移至顶部 点击事件
        mBinding.bookshelfMoveTop.clickGap { _, _ ->

            // 点击漫画 并且漫画适配器个数不为0
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) {
                if (mBookshelfComicRvAdapter.itemCount != 0) mBinding.bookshelfRvComic.smoothScrollToPosition(0)
            }

            // 否则就是轻小说，轻小说适配器个数不为0
            else if(mBookshelfNovelRvAdapter.itemCount != 0) mBinding.bookshelfRvNovel.smoothScrollToPosition(0)
        }

        // 移至底部 点击事件
        mBinding.bookshelfMoveBottom.clickGap { _, _ ->

            // 点击漫画 并且漫画适配器个数不为0
            if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) {
                if (mBookshelfComicRvAdapter.itemCount != 0) mBinding.bookshelfRvComic.smoothScrollToPosition(mBookshelfComicRvAdapter.itemCount - 1)
            }

            // 否则就是轻小说，轻小说适配器个数不为0
            else if(mBookshelfNovelRvAdapter.itemCount != 0) mBinding.bookshelfRvNovel.smoothScrollToPosition(mBookshelfNovelRvAdapter.itemCount - 1)
        }

        // 按钮组 点击事件 （漫画、轻小说）
        mBinding.bookshelfButtonGropu.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when(checkedId) {
                R.id.bookshelf_comic -> {                                           // 点击漫画
                    if (isChecked) {                                                // 选中
                        if (mBookshelfComicRvAdapter.itemCount == 0) {              // 漫画适配器个数为空
                            mBinding.bookshelfText.animateFadeIn()                  // “空空如也” 淡入
                            mBinding.bookshelfRvComic.visibility = View.INVISIBLE   // 漫画适配器隐藏
                            mBinding.bookshelfRvNovel.visibility = View.INVISIBLE   // 轻小说适配器隐藏
                            return@addOnButtonCheckedListener                       // 退出Lambda
                        }

                        // 漫画 适配器不为空 判断“空空如也” 是否可见 ，可见的话则 淡出并在动画结束时 设置消失
                        else if(mBinding.bookshelfText.isVisible) mBinding.bookshelfText.animateFadeOut().withEndAction { mBinding.bookshelfText.visibility = View.GONE }
                        mBinding.bookshelfRvNovel.animateFadeOut().withEndAction { mBinding.bookshelfRvNovel.visibility = View.INVISIBLE }  // 轻小说适配器淡出 动画结束时隐藏
                        mBinding.bookshelfRvComic.animateFadeIn()   // 漫画适配器淡入 动画结束时显示
                        if (mBinding.bookshelfCount.isVisible) mBinding.bookshelfCount.animateFadeOut()
                        mBinding.bookshelfCount.text = getString(R.string.bookshelf_comic_count, mComicCount ?: 0)
                        mBinding.bookshelfCount.animateFadeIn()
                    }
                }
                R.id.bookshelf_novel -> {  // 逻辑如上 反着来
                    if (isChecked) {
                        if (mBookshelfNovelRvAdapter.itemCount == 0) {
                            mBinding.bookshelfText.animateFadeIn()
                            mBinding.bookshelfRvComic.visibility = View.INVISIBLE
                            mBinding.bookshelfRvNovel.visibility = View.INVISIBLE
                            return@addOnButtonCheckedListener
                        } else if(mBinding.bookshelfText.isVisible) mBinding.bookshelfText.animateFadeOut().withEndAction { mBinding.bookshelfText.visibility = View.GONE }
                        mBinding.bookshelfRvComic.animateFadeOut().withEndAction { mBinding.bookshelfRvComic.visibility = View.INVISIBLE }
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

        // 发送获取书架 漫画 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfComic())

        // 发送获取书架 轻小说 的意图 需要动态收集书架状态才可
        mBsVM.input(BookshelfIntent.GetBookshelfNovel())

        // 每隔观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
        repeatOnLifecycle { mBsVM.mBookshelfComicFlowPager?.collect { data -> mBookshelfComicRvAdapter.submitData(data) } }

        // 收集书架 轻小说Pager状态
        repeatOnLifecycle { mBsVM.mBookshelfNovelFlowPager?.collect { data -> mBookshelfNovelRvAdapter.submitData(data) } }

        // 接收意图
        mBsVM.onOutput { intent ->
            when(intent) {
                is BookshelfIntent.GetBookshelfComic -> {
                    "GetBoookshelfComic : ${intent.mViewState}".logMsg()
                    intent.mViewState
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
                    "GetBoookshelfNovel : ${intent.mViewState}".logMsg()
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
}
