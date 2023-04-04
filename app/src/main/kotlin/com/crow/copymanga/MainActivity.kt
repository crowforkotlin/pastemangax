package com.crow.copymanga

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.logMsg
import com.crow.copymanga.databinding.AppActivityMainBinding
import com.crow.module_main.ui.fragment.ContainerFragment
import com.orhanobut.logger.Logger
import java.util.ResourceBundle.getBundle

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
        // 设置屏幕方向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(mBinding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        "(MainActivity) onDestory".logMsg(Logger.WARN)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存导航状态
        outState.putBundle("navState", findNavController(R.id.app_main_fcv).saveState())
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