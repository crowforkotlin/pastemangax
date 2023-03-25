package com.crow.module_bookshelf.ui.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.current_project.entity.ComicTapEntity
import com.crow.base.current_project.entity.ComicType
import com.crow.base.current_project.processTokenError
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResultSuspend
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_bookshelf.R
import com.crow.module_bookshelf.databinding.BookshelfFragmentBinding
import com.crow.module_bookshelf.model.intent.BookshelfIntent
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

    // 书架VM
    private val mBsVM by sharedViewModel<BookshelfViewModel>()

    // Bookshelf Comic适配器
    private lateinit var mBookshelfComicRvAdapter: BookshelfComicRvAdapter

    // Bookshelf Novel适配器
    private lateinit var mBookshelfNovelRvAdapter: BookshelfNovelRvAdapter

    override fun getViewBinding(inflater: LayoutInflater) = BookshelfFragmentBinding.inflate(inflater)

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.bookshelfBar.setPadding(0, mContext.getStatusBarHeight(), 0, 0)

        // 设置刷新时不允许列表滚动
        mBinding.bookshelfRefresh.setDisableContentWhenRefresh(true)

        // 初始化适配器
        mBookshelfComicRvAdapter = BookshelfComicRvAdapter {
            FlowBus.with<ComicTapEntity>(BaseStrings.Key.OPEN_COMIC_BOTTOM).post(lifecycleScope, ComicTapEntity(ComicType.Comic, it.mComic.mPathWord))
        }
        mBookshelfNovelRvAdapter = BookshelfNovelRvAdapter {
            FlowBus.with<ComicTapEntity>(BaseStrings.Key.OPEN_COMIC_BOTTOM).post(lifecycleScope, ComicTapEntity(ComicType.Novel, it.mNovel.mPathWord))
        }

        // 设置适配器
        mBinding.bookshelfRvComic.adapter = mBookshelfComicRvAdapter
        mBinding.bookshelfRvNovel.adapter = mBookshelfNovelRvAdapter

    }

    override fun initListener() {

        // 根据当前页面类型（漫画 、 轻小说）执行对应适配器刷新
        mBinding.bookshelfRefresh.setOnRefreshListener {
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
                        mBinding.bookshelfRvComic.animateFadeIn().withEndAction { mBinding.bookshelfRvComic.visibility = View.VISIBLE }     // 漫画适配器淡入 动画结束时显示
                        mBinding.bookshelfRvNovel.animateFadeOut().withEndAction { mBinding.bookshelfRvNovel.visibility = View.INVISIBLE }  // 轻小说适配器淡出 动画结束时隐藏
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
                        mBinding.bookshelfRvNovel.animateFadeIn().withEndAction { mBinding.bookshelfRvNovel.visibility = View.VISIBLE }
                    }
                }
            }
        }
    }

    override fun initObserver() {

        // 每隔观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效
        repeatOnLifecycle {

            // 发送获取书架 漫画 的意图 需要动态收集书架状态才可
            mBsVM.input(BookshelfIntent.GetBookshelfComic())

            // 收集书架 漫画Pager状态
            mBsVM.mBookshelfComicFlowPager?.collect { data -> mBookshelfComicRvAdapter.submitData(data) }

        }

        repeatOnLifecycle {

            // 发送获取书架 轻小说 的意图 需要动态收集书架状态才可
            mBsVM.input(BookshelfIntent.GetBookshelfNovel())

            // 收集书架 轻小说Pager状态
            mBsVM.mBookshelfNovelFlowPager?.collect { data -> mBookshelfNovelRvAdapter.submitData(data) }
        }

        // 接收意图
        mBsVM.onOutput { intent ->
            when(intent) {
                is BookshelfIntent.GetBookshelfComic -> {
                    intent.mViewState
                        .doOnResultSuspend {

                            // 文本不可见 代表成功获取到数据
                            if (mBinding.bookshelfText.isVisible) {

                                // “空空如也“ 不可见
                                mBinding.bookshelfText.visibility = View.GONE

                                // 刷新布局 可见
                                mBinding.bookshelfRefresh.visibility = View.VISIBLE

                                if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) {
                                    mBinding.bookshelfRvComic.visibility = View.VISIBLE
                                } else {
                                    mBinding.bookshelfRvNovel.visibility = View.VISIBLE
                                }

                                // 书架栏 可见
                                mBinding.bookshelfBar.visibility = View.VISIBLE
                                mBinding.bookshelfCount.visibility = View.VISIBLE
                            }

                            // 设置漫画总数
                            if (mBinding.bookshelfCount.text.isNullOrEmpty()) {
                                mBinding.bookshelfCount.text = getString(R.string.bookshelf_count, intent.bookshelfComicfResp!!.mTotal.toString())
                                mBinding.bookshelfBar.animateFadeIn()
                                mBinding.bookshelfCount.animateFadeIn()
                            }

                            // 正在刷新？
                            if(mBinding.bookshelfRefresh.isRefreshing) {

                                // 取消刷新
                                mBinding.bookshelfRefresh.finishRefresh()

                                // Toast Tips
                                toast(getString(baseR.string.BaseRefreshScucess))
                            }

                        }
                        .doOnError { code, msg ->

                            // 适配器数据 0 的逻辑
                            if (mBookshelfComicRvAdapter.itemCount == 0) {

                                // “空空如也” 可见
                                mBinding.bookshelfText.visibility = View.VISIBLE

                                // 隐藏 书架栏
                                mBinding.bookshelfBar.visibility = View.INVISIBLE
                                mBinding.bookshelfCount.visibility = View.INVISIBLE

                                // 隐藏 刷新布局
                                mBinding.bookshelfRefresh.visibility = View.GONE
                                mBinding.bookshelfRvComic.visibility = View.INVISIBLE

                                // 置空漫画总数文本 为什么？因为 当下次请求成功时就可根据对应的逻辑设置 文本， 仅设置一次文本即可
                                mBinding.bookshelfCount.text = null

                                // 完成刷新
                                mBinding.bookshelfRefresh.finishRefresh()
                            }

                            // 解析地址失败 且 Resumed的状态才提示
                            if (code == ViewState.Error.UNKNOW_HOST && this.isResumed) mBinding.root.showSnackBar(msg ?: getString(baseR.string.BaseLoadingError))

                            // Token为空不处理 Token错误校验
                            else if (BaseUser.CURRENT_USER_TOKEN.isEmpty()) return@doOnError

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
                }
                is BookshelfIntent.GetBookshelfNovel -> {
                    intent.mViewState
                        .doOnSuccess {
                            if (mBinding.bookshelfRefresh.isRefreshing) mBinding.bookshelfRefresh.finishRefresh()
                        }
                }
            }
        }
    }

    fun doRefresh() {

        // 开启刷新动画
        mBinding.bookshelfRefresh.autoRefresh()

        // 根据当前页面类型（漫画 、 轻小说）执行对应适配器刷新
        if (mBinding.bookshelfButtonGropu.checkedButtonId == R.id.bookshelf_comic) mBookshelfComicRvAdapter.refresh() else mBookshelfNovelRvAdapter.refresh()
    }
}