package com.crow.copymanga

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.crow.base.current_project.entity.Fragments
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.logMsg
import com.crow.base.tools.extensions.navigateByAddWithBackStack
import com.crow.copymanga.databinding.AppActivityMainBinding
import com.crow.module_main.ui.fragment.ContainerFragment
import com.orhanobut.logger.Logger
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.ScopeActivity

class MainActivity : ScopeActivity()  {

    private val mBinding by lazy { AppActivityMainBinding.inflate(layoutInflater) }

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

        // 设置屏幕方向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 设置布局
        setContentView(mBinding.root)

        // 内存重启后 避免再次添加布局
        if (savedInstanceState == null) supportFragmentManager.navigateByAddWithBackStack<ContainerFragment>(R.id.app_main_fcv, null, Fragments.Container.toString(), Fragments.Container.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        "(MainActivity) onDestory".logMsg(Logger.WARN)
    }

    override fun onStop() {
        super.onStop()
        "(MainActivity) onStop".logMsg(Logger.WARN)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        "(MainActivity) onLowMemory".logMsg(Logger.ERROR)
    }
}