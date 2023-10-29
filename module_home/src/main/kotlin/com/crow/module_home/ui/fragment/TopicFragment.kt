package com.crow.module_home.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import com.crow.base.R
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_home.databinding.HomeFragmentTopicBinding
import com.crow.module_home.model.resp.homepage.Topices
import com.crow.module_home.ui.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    private var mTopic by BaseNotNullVar<Topices>()

    /**
     * ● 直接共享使用主页VM即可 数据量不多
     *
     * ● 2023-10-29 16:01:10 周日 下午
     * @author crowforkotlin
     */
    private val mVM by viewModel<HomeViewModel>()

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
    override fun initObserver(saveInstanceState: Bundle?) {}

    /**
     * ● 初始化视图
     *
     * ● 2023-10-29 16:02:07 周日 下午
     * @author crowforkotlin
     */
    override fun initView(savedInstanceState: Bundle?) {

        immersionRoot()

    }


    /**
     * ● 返回上一个界面
     *
     * ● 2023-10-29 15:40:05 周日 下午
     * @author crowforkotlin
     */
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Topic.name, Fragments.Topic.name)
}