package com.crow.copymanga.ui.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.isDarkMode
import com.crow.base.tools.extensions.navigateByAdd
import com.crow.base.tools.extensions.onCollect
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.copymanga.R
import com.crow.copymanga.databinding.AppActivityMainBinding
import com.crow.module_main.model.intent.MainIntent
import com.crow.module_main.ui.fragment.ContainerFragment
import com.crow.module_main.ui.viewmodel.MainViewModel
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseMviActivity<AppActivityMainBinding>()  {

    /** ● 容器VM */
    private val mContainerVM by viewModel<MainViewModel>()

    /** ● 获取ViewBinding */
    override fun getViewBinding() = AppActivityMainBinding.inflate(layoutInflater)

    /** ● Lifecycle onCreate */
    override fun onCreate(savedInstanceState: Bundle?) {

        // 启动动画
        val splash = installSplashScreen()

        // 可以监听动画是否展示完毕来延长launch的展示
        splash.setKeepOnScreenCondition { false }

        splash.setOnExitAnimationListener { provider ->  provider.view.animateFadeOut(BASE_ANIM_200L).withEndAction { provider.remove() } }

        // 配置KoinFragmentFactory
        setupKoinFragmentFactory()

        super.onCreate(savedInstanceState)
    }

    /** ● 初始化视图 */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun initView(savedInstanceState: Bundle?) {

        // 设置屏幕方向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 沉浸式Edge To Edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置布局
        setContentView(mBinding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.navigateByAdd<ContainerFragment>(R.id.app_main_fcv, null, Fragments.Container.name)
        }
    }

    /** ● 初始化观察者 */
    override fun initObserver(savedInstanceState: Bundle?) {

        mContainerVM.mAppConfig.onCollect(this) { appConfig ->

            if (appConfig != null) {

                // 设置站点和TOKEN
                BaseStrings.URL.COPYMANGA = appConfig.mSite
                BaseUser.CURRENT_ROUTE = appConfig.mRoute

                // 第一次初始化则获取动态站点
                if (appConfig.mAppFirstInit) mContainerVM.input(MainIntent.GetDynamicSite())

                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = (!isDarkMode())
            }
        }
    }
}

/*
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
*/
