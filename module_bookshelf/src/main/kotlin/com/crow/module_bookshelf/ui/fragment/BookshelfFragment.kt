package com.crow.module_bookshelf.ui.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.current_project.processTokenError
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_bookshelf.R
import com.crow.module_bookshelf.databinding.BookshelfFragmentBinding
import com.crow.module_bookshelf.model.intent.BookShelfIntent
import com.crow.module_bookshelf.ui.adapter.BookshelfRvAdapter
import com.crow.module_bookshelf.ui.viewmodel.BookshelfViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
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
    private val mBookshelfVM by viewModel<BookshelfViewModel>()

    // Bookshelf 适配器
    private lateinit var mBookshelfRvAdapter: BookshelfRvAdapter

    override fun getViewBinding(inflater: LayoutInflater) = BookshelfFragmentBinding.inflate(inflater)

    override fun initView() {

        // 初始化适配器
        mBookshelfRvAdapter = BookshelfRvAdapter {  }

        // 设置刷新时不允许列表滚动
        mBinding.bookshelfRefresh.setDisableContentWhenRefresh(true)

        // 设置适配器
        mBinding.bookshelfRv.adapter = mBookshelfRvAdapter

        // mBinding.bookshelfBar.setPadding(0, 0, 0, mContext.getNavigationBarHeight() + mContext.getNavigationBarHeight() / 4)
    }

    override fun initListener() {

        // 刷新
        mBinding.bookshelfRefresh.setOnRefreshListener { mBookshelfRvAdapter.refresh() }

        mBinding.bookshelfMoveTop.clickGap { _, _ ->
            if (mBookshelfRvAdapter.itemCount != 0) {
                mBinding.bookshelfRv.smoothScrollToPosition(0)
            }
        }
        mBinding.bookshelfMoveBottom.clickGap { _, _ ->
            if (mBookshelfRvAdapter.itemCount > 0) {
                mBinding.bookshelfRv.smoothScrollToPosition(mBookshelfRvAdapter.itemCount - 1)
            }
        }


    }

    override fun initObserver() {

        repeatOnLifecycle(Lifecycle.State.CREATED) {

            // 发送获取书架的意图
            mBookshelfVM.input(BookShelfIntent.GetBookShelf())

            // 收集书架Pager状态
            mBookshelfVM.mBookshelfFlowPager?.collect { data -> mBookshelfRvAdapter.submitData(data) }
        }

        // 接收意图
        mBookshelfVM.onOutput { intent ->
            when(intent) {
                is BookShelfIntent.GetBookShelf -> {
                    intent.mViewState
                        .doOnResult {

                            // 文本不可见 代表成功获取到数据
                            if (mBinding.bookshelfText.isVisible) {

                                // “空空如也“ 不可见
                                mBinding.bookshelfText.visibility = View.GONE

                                // 刷新布局 可见
                                mBinding.bookshelfRefresh.visibility = View.VISIBLE

                                // 书架栏 可见
                                mBinding.bookshelfBarFirst.visibility = View.VISIBLE
                                mBinding.bookshelfCount.visibility = View.VISIBLE
                            }

                            // 设置漫画总数
                            if (mBinding.bookshelfCount.text.isNullOrEmpty()) {
                                mBinding.bookshelfCount.text = getString(R.string.bookshelf_count, intent.bookshelfResp!!.mTotal.toString())
                                mBinding.bookshelfBarFirst.animateFadeIn()
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
                            if (mBookshelfRvAdapter.itemCount == 0) {

                                // “空空如也” 可见
                                mBinding.bookshelfText.visibility = View.VISIBLE

                                // 隐藏 书架栏
                                mBinding.bookshelfBarFirst.visibility = View.INVISIBLE
                                mBinding.bookshelfCount.visibility = View.INVISIBLE

                                // 隐藏 刷新布局
                                mBinding.bookshelfRefresh.visibility = View.GONE

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
                                    mBookshelfRvAdapter.refresh()
                                    FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).post(lifecycleScope, Unit)
                                },
                                doOnConfirm = {
                                    navigate(baseR.id.mainUserloginfragment)
                                    FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).post(lifecycleScope, Unit)
                                }
                            )
                        }
                }
            }
        }
    }

    fun doRefresh() {
        mBinding.bookshelfRefresh.autoRefresh()
        mBookshelfRvAdapter.refresh()
    }
}