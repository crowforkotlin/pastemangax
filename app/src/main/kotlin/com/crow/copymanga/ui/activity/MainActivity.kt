package com.crow.copymanga.ui.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.isInvisible
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.isLatestVersion
import com.crow.base.tools.extensions.navigateByAdd
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.copymanga.R
import com.crow.copymanga.databinding.AppActivityMainBinding
import com.crow.module_main.databinding.MainUpdateLayoutBinding
import com.crow.module_main.databinding.MainUpdateUrlLayoutBinding
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.model.resp.MainAppUpdateResp
import com.crow.module_main.ui.adapter.MainAppUpdateRv
import com.crow.module_main.ui.fragment.ContainerFragment
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.DisposableContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 *
 * 类名描述信息,测试文档生成类
 * @param T 泛型类型
 * @property value 属性value
 * @constructor 创建DokkaDocTest的构造器.
 * @Author haiyang
 *
 */
class MainActivity : BaseMviActivity<AppActivityMainBinding>()  {

    init {
        FlowBus.with<Unit>(BaseStrings.Key.CHECK_UPDATE).register(this) { mContainerVM.input(ContainerIntent.GetUpdateInfo()) }  // 查询更新
    }

    // 初始化更新是否完成
    private var mInitUpdate: Boolean = false

    private val mContainerVM by viewModel<ContainerViewModel>()

    override fun getViewBinding() = AppActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {

        // 启动动画
        installSplashScreen().apply {

            // 可以监听动画是否展示完毕来延长launch的展示
            setKeepOnScreenCondition { false }
            setOnExitAnimationListener { provider -> provider.view.animateFadeOut(500L).withEndAction { provider.remove() } }
        }

        // 配置KoinFragmentFactory
        setupKoinFragmentFactory()

        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun initView(savedInstanceState: Bundle?) {

        // 设置屏幕方向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 设置布局
        setContentView(mBinding.root)



        // 内存重启后 避免再次添加布局
        if (savedInstanceState == null) supportFragmentManager.navigateByAdd<ContainerFragment>(R.id.app_main_fcv, null, Fragments.Container.toString())
    }

    override fun initObserver(savedInstanceState: Bundle?) {

        mContainerVM.appConfig.onCollect(this) { appConfig ->
            if (appConfig != null) {
                BaseStrings.URL.CopyManga = appConfig.mSite
                BaseUser.CURRENT_ROUTE = appConfig.mRoute
                if (appConfig.mAppFirstInit) { mContainerVM.input(ContainerIntent.GetDynamicSite()) }
            }
        }

        mContainerVM.onOutput { intent ->
            when (intent) {
                is ContainerIntent.GetUpdateInfo -> {
                    intent.mBaseViewState
                        .doOnError { _, _ -> toast(getString(com.crow.module_main.R.string.main_update_error)) }
                        .doOnResult { doUpdateChecker(intent.appUpdateResp!!) }
                }
            }
        }
    }

    // 检查更新
    private fun doUpdateChecker(appUpdateResp: MainAppUpdateResp) {
        val update = appUpdateResp.mUpdates.first()
        if (isLatestVersion(latest = update.mVersionCode.toLong())) return run {
            if (mInitUpdate) toast(getString(com.crow.module_main.R.string.main_update_tips))
            mInitUpdate = true
        }
        mInitUpdate = true
        val updateBinding = MainUpdateLayoutBinding.inflate(layoutInflater)
        val updateDialog = newMaterialDialog { dialog ->
            dialog.setCancelable(false)
            dialog.setView(updateBinding.root)
        }
        val screenHeight = resources.displayMetrics.heightPixels / 3
        (updateBinding.mainUpdateScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
        updateBinding.mainUpdateCancel.isInvisible = appUpdateResp.mForceUpdate
        updateBinding.mainUpdateTitle.text = update.mTitle
        updateBinding.mainUpdateText.text = update.mContent
        updateBinding.mainUpdateTime.text = getString(com.crow.module_main.R.string.main_update_time, update.mTime)
        if (!appUpdateResp.mForceUpdate) { updateBinding.mainUpdateCancel.doOnClickInterval { updateDialog.dismiss() } }
        updateBinding.mainUpdateGo.doOnClickInterval {
            updateDialog.dismiss()
            val updateUrlBinding = MainUpdateUrlLayoutBinding.inflate(layoutInflater)
            val updateUrlDialog = newMaterialDialog {
                it.setCancelable(false)
                it.setView(updateUrlBinding.root)
            }
            (updateUrlBinding.mainUpdateUrlScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
            updateUrlBinding.mainUpdateUrlCancel.isInvisible = appUpdateResp.mForceUpdate
            updateUrlBinding.mainUpdateUrlRv.adapter = MainAppUpdateRv(update.mUrl)
            if (!appUpdateResp.mForceUpdate) { updateUrlBinding.mainUpdateUrlCancel.doOnClickInterval { updateUrlDialog.dismiss() } }
        }
    }
}

fun main() = runBlocking<Unit> {

    launch {
        delay(1000L)
        println("------------------------------------")
        flow { emit(1) }
            .onStart { println("start") }
            .onEach { println("onEach1") }
            .onEach {throw Exception("onEach2") }
            .catch { println("catch : $it") }
            .collect { println(it) }
    }

    println("-----------")


    fun getResult() : String {
        return "Result"
    }

    Flowable.just(getResult())
        .doOnSubscribe {
            println("[Flowable] : doOnSubscribe")
        }
        .doOnComplete {
            println("[Flowable] : doOnComplete")
        }
        .doOnEach {
            println("[Flowable] : doOnEach : $it")
        }
        .doOnNext {
            println("[Flowable] : doOnNext : $it")
        }
        .doAfterNext {
            println("[Flowable] : doAfterNext : $it")
        }
        .doOnError {
            println("[Flowable] : doOnError : $it")
        }
        .doAfterTerminate {
            println("[Flowable] : doAfterTerminate")
        }
        .doFinally {
            println("[Flowable] : doFinally")
        }
        .doOnLifecycle({
            println("doOnLifecycle : Disposable $it")
        }, {
            println("[Flowable] : doOnLifecycle : Run Action")
        }, {
            println("[Flowable] : doOnLifecycle : Run Action")
        })
        .subscribe({
            println("[Flowable] [subscribe] : value is $it")
        }, {
            println("[Flowable] [subscribe] : Throwable : $it")
        }, {
            println("[Flowable] [subscribe] : Run Action")
        }, object : DisposableContainer {
            override fun add(d: Disposable?): Boolean {
                println("[Flowable] [subscribe] : Add $d")
                return true
            }

            override fun remove(d: Disposable?): Boolean {
                println("[Flowable] [subscribe] : Remove")
                return true
            }

            override fun delete(d: Disposable?): Boolean {
                println("[Flowable] [subscribe] : delete $d")
                return true
            }
        })

    println("------------------------")
    Observable.create { it.onNext("onNext") }.subscribe {
        println("123123132123123132")
    }
    Observable.just(getResult())
        .doOnSubscribe {
            println("doOnSubscribe")
        }
        .doOnComplete {
            println("doOnComplete")
        }
        .doOnEach {
            println("doOnEach : $it")
        }
        .doOnNext {
            println("doOnNext : $it")
        }
        .doAfterNext {
            println("doAfterNext : $it")
        }
        .doOnError {
            println("doOnError : $it")
        }
        .doAfterTerminate {
            println("doAfterTerminate")
        }
        .doFinally {
            println("doFinally")
        }
        .doOnLifecycle({
            println("doOnLifecycle : Disposable $it")
        }, {
            println("doOnLifecycle : Run Action")
        })
        .subscribe({
            println("[subscribe] : value is $it")
        }, {
            println("[subscribe] : Throwable : $it")
        }, {
            println("[subscribe] : Run Action")
        }, object : DisposableContainer {
            override fun add(d: Disposable?): Boolean {
                println("[subscribe] : Add $d")
                return true
            }

            override fun remove(d: Disposable?): Boolean {
                println("[subscribe] : Remove")
                return true
            }

            override fun delete(d: Disposable?): Boolean {
                println("[subscribe] : delete $d")
                return true
            }
        })
}