package com.crow.module_home.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.crow.base.R
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.notNull
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_home.databinding.HomeFragmentTopicBinding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.Topices
import com.crow.module_home.ui.adapter.TopicListAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_home.ui.fragment
 * @Time: 2023/10/29 14:46
 * @Author: CrowForKotlin
 * @Description: TopicFragment
 * @formatter:on
 **************************/
class TopicFragment : BaseMviFragment<HomeFragmentTopicBinding>() {

    /**
     * ● StaticArea
     *
     * ● 2023-10-29 16:01:39 周日 下午
     * @author crowforkotlin
     */
    companion object { const val TOPIC = "TOPIC" }

    /**
     * ● 主题内容
     *
     * ● 2023-10-29 16:01:28 周日 下午
     * @author crowforkotlin
     */
    private lateinit var mTopic: Topices

    /**
     * ● 直接共享使用主页VM即可 数据量不多
     *
     * ● 2023-10-29 16:01:10 周日 下午
     * @author crowforkotlin
     */
    private val mVM by viewModel<HomeViewModel>()

    /**
     * ● Topic Rv Adapter
     *
     * ● 2023-11-01 00:09:12 周三 上午
     * @author crowforkotlin
     */
    private val mAdapter by lazy {
        TopicListAdapter { name, pathword ->
            onNavigate(Fragments.BookComicInfo.name, name, pathword)
        }
    }

    /**
     * ● 刷新任务
     *
     * ● 2023-11-01 01:31:45 周三 上午
     * @author crowforkotlin
     */
    private var mRefreshJob: Job? = null

    /**
     * ● 网络任务
     *
     * ● 2023-11-01 23:40:47 周三 下午
     * @author crowforkotlin
     */
    private var mNetworkJob: Job? = null

    /**
     * ● Error ViewStub
     *
     * ● 2023-11-01 00:23:39 周三 上午
     * @author crowforkotlin
     */
    private var mError by BaseNotNullVar<BaseErrorViewStub>(true)

    /**
     * ● 获取VB
     *
     * ● 2023-10-29 16:02:22 周日 下午
     * @author crowforkotlin
     */
    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentTopicBinding.inflate(layoutInflater)

    /**
     * ● Lifecycle onCreate Init mTopic
     *
     * ● 2023-10-29 15:51:14 周日 下午
     * @author crowforkotlin
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Init mTopic, otherwise exit fragment
        runCatching { mTopic = toTypeEntity<Topices>(arguments?.getString(TOPIC)) ?: error("topic is null!") }
            .onFailure {
                toast(getString(R.string.BaseUnknowError))
                navigateUp()
            }
            .onSuccess {
                mNetworkJob = lifecycleScope.launch {
                    delay(BASE_ANIM_200L shl 1)
                    mVM.input(HomeIntent.GetTopic(mTopic.mPathWord)) {
                        mVM.mTopicFlowPage?.onCollect(this@TopicFragment) { mAdapter.submitData(it) }
                    }
                }
            }
    }

    /**
     * ● Lifecycle DestroyView
     *
     * ● 2023-11-01 01:34:41 周三 上午
     * @author crowforkotlin
     */
    override fun onDestroyView() {
        super.onDestroyView()
        BaseEvent.getSIngleInstance().remove("TOPIC_FRAGMENT_REFRESH_ANIMATE_ONLY")
        mNetworkJob?.cancel()
        mNetworkJob = null
        mRefreshJob?.cancel()
        mRefreshJob = null
    }

    /**
     * ● Lifecycle onStart do back dispatcher
     *
     * ● 2023-10-29 16:04:04 周日 下午
     * @author crowforkotlin
     */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-10-29 16:01:56 周日 下午
     * @author crowforkotlin
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mVM.onOutput { intent ->
            when(intent) {
                is HomeIntent.GetTopic -> {
                    intent.mViewState
                        .doOnSuccess {
                            if (BaseEvent.getSIngleInstance().getBoolean("TOPIC_FRAGMENT_REFRESH_ANIMATE_ONLY") == true) {
                                BaseEvent.getSIngleInstance().remove("TOPIC_FRAGMENT_REFRESH_ANIMATE_ONLY")
                                mBinding.refresh.finishRefresh()
                            }
                            if (mBinding.refresh.isRefreshing) {
                                mRefreshJob?.cancel()
                                mRefreshJob = null
                                mBinding.refresh.finishRefresh()
                            }
                        }
                        .doOnError { _, _ ->
                            if (mError.isGone()) mError.loadLayout(visible = true, animation = true)
                            if (mBinding.list.isVisible)  mBinding.list.animateFadeOutWithEndInVisibility()
                        }
                        .doOnResult {
                            intent.topicResp.notNull {
                                if (mError.isVisible()) mError.loadLayout(visible = false, animation = true)
                                if (mBinding.list.isInvisible)  mBinding.list.animateFadeIn()
                            }
                        }
                }
            }
        }
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-10-29 16:02:07 周日 下午
     * @author crowforkotlin
     */
    override fun initView(savedInstanceState: Bundle?) {


        if (::mTopic.isInitialized) {

            mBinding.textView.text = mTopic.mBrief
            mBinding.topbar.title =  "${mTopic.mJournal} ${mTopic.mPeriod}"
            mBinding.topbar.subtitle = mTopic.mDatetimeCreated
        }

        // 刷新动画
        mBinding.refresh.autoRefreshAnimationOnly().also {
            BaseEvent.getSIngleInstance().setBoolean("TOPIC_FRAGMENT_REFRESH_ANIMATE_ONLY", true)
        }

        // 沉浸式
        immersionRoot()

        // 初始化ErrorViewStub
        mError = baseErrorViewStub(mBinding.error, lifecycle) { mBinding.refresh.autoRefresh() }

        // 设置刷新时不允许列表滚动
        mBinding.refresh.setDisableContentWhenRefresh(true)

        //  初始化Rv适配器
        mBinding.list.adapter = mAdapter

    }

    /**
     * ● 初始化事件
     *
     * ● 2023-11-01 01:23:07 周三 上午
     * @author crowforkotlin
     */
    override fun initListener() {

        // 返回
        mBinding.topbar.navigateIconClickGap { navigateUp() }

        // 刷新
        mBinding.refresh.setOnRefreshListener {  layout ->
            mRefreshJob?.cancel()
            mRefreshJob = lifecycleScope.launch {
                delay(4000L)
                layout.finishRefresh()
                toast(getString(baseR.string.BaseUnknowError))
            }
            mAdapter.retry()
        }
    }

    /**
     * ● 返回上一个界面
     *
     * ● 2023-10-29 15:40:05 周日 下午
     * @author crowforkotlin
     */
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Topic.name, Fragments.Topic.name)

    /**
     * ● 导航
     *
     * ● 2023-10-04 17:00:33 周三 下午
     */
    private fun onNavigate(tag: String, name: String, pathword: String) {
        val bundle = Bundle()
        bundle.putString(BaseStrings.PATH_WORD, pathword)
        bundle.putString(BaseStrings.NAME, name)
        parentFragmentManager.navigateToWithBackStack(
            id = R.id.app_main_fcv,
            hideTarget = this,
            addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
            tag = tag,
            backStackName = tag
        )
    }
}