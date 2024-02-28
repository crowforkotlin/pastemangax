package com.crow.module_anime.ui.activity

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.crow.base.tools.extensions.immersionFullScreen
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.immersionFullView
import com.crow.base.tools.extensions.immerureCutoutCompat
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.updatePadding
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.BaseStrings
import com.crow.module_anime.databinding.AnimeActivityBinding
import com.crow.module_anime.model.intent.AnimeIntent
import com.crow.module_anime.model.resp.video.AnimeVideoResp
import com.crow.module_anime.ui.helper.GestureHelper
import com.crow.module_anime.ui.viewmodel.AnimeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

class AnimeActivity : BaseMviActivity<AnimeActivityBinding>() {

    /**
     * ● Static Area
     *
     * ● 2023-10-15 14:51:01 周日 下午
     */
    companion object {
        const val ANIME_CHAPTER_UUIDS = "UUIDS"
        const val ANIME_CHAPTER_UUID_POSITION = "POSITION"
    }

    /**
     * ● 章节UUID列表
     *
     * ● 2023-10-15 13:07:31 周日 下午
     */
    private lateinit var mChapterUUIDS: List<String>

    /**
     * ● 番剧路径词
     *
     * ● 2023-10-15 13:07:40 周日 下午
     */
    private lateinit var mPathword: String

    /**
     * ● 番名
     *
     * ● 2023-10-15 13:07:47 周日 下午
     */
    private lateinit var mName: String

    /**
     * ● 番剧UUID 位置
     *
     * ● 2023-10-15 14:30:35 周日 下午
     */
    private var mPos: Int = 0

    /**
     * ● Activitiy GestureHelper （手势处理）
     *
     * ● 2023-10-15 14:47:15 周日 下午
     */
    private lateinit var mGestureHelper: GestureHelper

    /**
     * ● WindowInset For immersure or systembar
     *
     * ● 2023-10-15 14:52:05 周日 下午
     */
    private val mWindowInsetsCompat: WindowInsetsControllerCompat by lazy { WindowInsetsControllerCompat(window, mBinding.root) }

    /**
     * ● 播放器实例
     *
     * ● 2023-10-15 13:08:24 周日 下午
     */
    private val mPlayer by lazy { ExoPlayer.Builder(this).build() }

    /**
     * ● AnimeViewModel
     *
     * ● 2023-10-15 12:09:27 周日 下午
     */
    private val mVM by viewModel<AnimeViewModel>()
    
    /**
     * ● VB
     *
     * ● 2023-10-15 13:08:37 周日 下午
     */
    override fun getViewBinding() = AnimeActivityBinding.inflate(layoutInflater)

    /**
     * ● Lifecycle onCreate
     *
     * ● 2023-10-15 14:50:46 周日 下午
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val error = getString(baseR.string.base_loading_error)
        mChapterUUIDS = toTypeEntity<List<String>>(intent.getStringExtra(ANIME_CHAPTER_UUIDS)) ?: return run { finishActivity(error) }
        mPathword = intent.getStringExtra(BaseStrings.PATH_WORD) ?: return run { finishActivity(error) }
        mName = intent.getStringExtra(BaseStrings.NAME) ?: return run { finishActivity(error) }
        mPos = intent.getIntExtra(ANIME_CHAPTER_UUID_POSITION, 0)

        if (mPlayer.isPlaying) { mPlayer.stop() }

        super.onCreate(savedInstanceState)

        immersionFullView(window)
        immerureCutoutCompat(window)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.stop()
        mPlayer.release()
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-10-15 14:50:36 周日 下午
     */
    @androidx.media3.common.util.UnstableApi
    override fun initView(savedInstanceState: Bundle?) {

        // 横屏
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        } else {
            mBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
        }


        // 初始化Playerview
        mBinding.playerView.player = mPlayer

        // 全屏
        immersionFullScreen(mWindowInsetsCompat)

        // 沉浸式边距
        immersionPadding(mBinding.root) { view, insets, _ ->
//            mBinding.topbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            mBinding.playerView.updatePadding(top = insets.top, bottom = insets.bottom)
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
        }

        // 沉浸式状态栏和工具栏
        // immersionBarStyle()
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-10-15 14:50:27 周日 下午
     */
    @Suppress("DEPRECATION")
    @androidx.media3.common.util.UnstableApi
    override fun initListener() {

        // PlayerView可见性监听
        mBinding.playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { value ->
            /*if (mBinding.playerView.isControllerFullyVisible) {
                transitionBar(false)
            } else {
                transitionBar(true)
            }*/
        })

        // 初始化GestureHelper
//        mGestureHelper =  GestureHelper(this, this)

        // 设置全屏的按钮
        mBinding.playerView.setControllerOnFullScreenModeChangedListener { isProtranit ->

            requestedOrientation = if(isProtranit) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

/*            when(resources.configuration.orientation) {

                // 横屏 -> 竖屏
                Configuration.ORIENTATION_LANDSCAPE -> resources.configuration.orientation = Configuration.ORIENTATION_PORTRAIT

                // 竖屏 -> 横屏
                Configuration.ORIENTATION_PORTRAIT -> resources.configuration.orientation = Configuration.ORIENTATION_LANDSCAPE
            }*/
        }
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-10-15 14:50:19 周日 下午
     */
    override fun initData(savedInstanceState: Bundle?) {

        mVM.input(AnimeIntent.AnimeVideoIntent(mPathword, mChapterUUIDS[mPos]))
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-10-15 14:50:10 周日 下午
     */
    override fun initObserver(savedInstanceState: Bundle?) {
        mVM.onOutput { intent ->
            when(intent) {
                is AnimeIntent.AnimeVideoIntent -> {
                    intent.mViewState
                        .doOnLoading {

                        }
                        .doOnError { _, _ ->

                        }
                        .doOnResult {
                            loadVideo(intent.video ?: return@doOnResult)
                        }
                }
            }
        }
    }

    /**
     * ● Activity Event onTouch
     *
     * ● 2023-07-07 23:56:56 周五 下午
     */
/*    override fun onTouch(area: Int, ev: MotionEvent) {
        transitionBar(mBinding.topbar.isVisible)
    }*/

    /**
     * ● Activity Event dispatchTouchEvent
     *
     * ● 2023-07-07 23:57:39 周五 下午
     */
/*    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val hasTopbar = hasGlobalPoint(mBinding.topbar, ev.rawX.toInt(), ev.rawY.toInt())
        val hasControllerView = hasGlobalPoint(mBinding.playerView.findViewById<PlayerControlView>(androidx.media3.ui.R.id.exo_controller), ev.rawX.toInt(), ev.rawY.toInt())
        mGestureHelper.dispatchTouchEvent(ev, hasTopbar && hasControllerView)
        return super.dispatchTouchEvent(ev)
    }*/

    /**
     * ● TransitionBar With Animation
     *
     * ● 2023-10-15 14:49:27 周日 下午
     */
/*    private fun transitionBar(isHide: Boolean) {
        val transition = TransitionSet()
            .setDuration(BASE_ANIM_300L)
            .addTransition(Slide(Gravity.TOP))
            .addTarget(mBinding.topbar)
        TransitionManager.beginDelayedTransition(mBinding.root, transition)
        mBinding.topbar.isGone = isHide
        mWindowInsetsCompat.isAppearanceLightStatusBars = !mDarkMode
        mWindowInsetsCompat.isAppearanceLightNavigationBars = !mDarkMode
        if (isHide) {
            immersionFullScreen(mWindowInsetsCompat)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            mWindowInsetsCompat.show(WindowInsetsCompat.Type.systemBars())
        }
    }*/

    /**
     * ● 加载视频
     *
     * ● 2023-10-15 14:49:33 周日 下午
     */
    private fun loadVideo(video: AnimeVideoResp) {
        mPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(video.mChapter.mVideo)))
        mPlayer.prepare()
        mPlayer.play()
    }

    /**
     * ● 退出Activity
     *
     * ● 2023-10-15 14:49:44 周日 下午
     */
    private fun finishActivity(msg: String? = null) {
        msg?.let(::toast)
        finishActivity()
    }

    /**
     * ● 沉浸式工具栏、导航栏、状态栏样式
     *
     * ● 2023-10-15 15:00:57 周日 下午
     */
/*    private fun immersionBarStyle() {
        (mBinding.topbar.background as MaterialShapeDrawable).apply {
            elevation = resources.getDimension(baseR.dimen.base_dp3)
            alpha = 242
            val color = ColorUtils.setAlphaComponent(resolvedTintColor, alpha)
            window.statusBarColor = color
            window.navigationBarColor = color
        }
    }*/
}