package com.crow.module_anime.ui.activity

import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import com.crow.base.copymanga.BaseStrings
import com.crow.base.tools.extensions.logger
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.module_anime.databinding.AnimeActivityBinding
import com.crow.module_anime.model.resp.chapter.Line

class AnimeActivity : BaseMviActivity<AnimeActivityBinding>() {

    companion object { const val ANIME_CHAPTER_UUID = "UUID" }

    private val mPlayer by lazy { ExoPlayer.Builder(this).build() }

    override fun getViewBinding() = AnimeActivityBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

        val pathword = toTypeEntity<MutableList<Line>>(intent.getStringExtra(BaseStrings.PATH_WORD))
        val name = intent.getStringExtra(BaseStrings.NAME)
        val uuid = intent.getStringExtra(ANIME_CHAPTER_UUID)
        logger(pathword)
        logger(name)
        logger(uuid)
    }
}