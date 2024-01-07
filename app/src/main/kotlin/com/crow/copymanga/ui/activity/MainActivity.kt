package com.crow.copymanga.ui.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentContainerView
import androidx.viewbinding.ViewBinding
import com.crow.mangax.R.id.app_main_fcv
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.BaseUserConfig
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mDarkMode
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.onCollect
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.module_main.model.intent.AppIntent
import com.crow.module_main.ui.viewmodel.MainViewModel
import org.koin.android.ext.android.get
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class MainActivity : BaseMviActivity<ViewBinding>()  {

    /** ● 容器VM */
    private val mContainerVM by viewModel<MainViewModel>()

    /** ● 获取ViewBinding */
    override fun getViewBinding() = ViewBinding { FragmentContainerView(this).also { view -> view.id = app_main_fcv } }

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
            val container = Fragments.Container.name
            supportFragmentManager
                .beginTransaction()
                .replace(app_main_fcv, get(named(container)), container)
                .commit()
        }
    }

    /** ● 初始化观察者 */
    override fun initObserver(savedInstanceState: Bundle?) {

        mContainerVM.mAppConfig.onCollect(this) { appConfig ->

            if (appConfig != null) {

                // 设置站点和TOKEN
                BaseStrings.URL.COPYMANGA = appConfig.mCopyMangaSite
                BaseStrings.URL.HotManga = appConfig.mHotMangaSite
                BaseUserConfig.CURRENT_ROUTE = appConfig.mRoute
                BaseUserConfig.RESOLUTION = appConfig.mResolution

                // 第一次初始化则获取动态站点
                if (appConfig.mAppFirstInit) mContainerVM.input(AppIntent.GetDynamicSite())

                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = (!mDarkMode)
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
