package com.crow.copymanga

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.logMsg
import com.crow.copymanga.databinding.AppActivityMainBinding
import com.crow.module_main.ui.fragment.ContainerFragment
import com.orhanobut.logger.Logger

class MainActivity : AppCompatActivity()  {

    private val mBinding by lazy { AppActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {

        // 启动动画
        installSplashScreen().apply {

            // 可以监听动画是否展示完毕来延长launch的展示
            setKeepOnScreenCondition { false }
            setOnExitAnimationListener { provider -> provider.view.animateFadeOut(500L).withEndAction { provider.remove() } }
        }

        super.onCreate(savedInstanceState)
        "(MainActivity) onCreate".logMsg(Logger.WARN)
        setContentView(mBinding.root)

        // 设置屏幕方向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)
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