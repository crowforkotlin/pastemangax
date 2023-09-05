package com.crow.module_home.model.resp.homepage.results

import com.crow.module_home.model.resp.homepage.Banner
import com.crow.module_home.model.resp.homepage.ComicDatas
import com.crow.module_home.model.resp.homepage.FinishComicDatas
import com.crow.module_home.model.resp.homepage.HotComic
import com.crow.module_home.model.resp.homepage.NewComic
import com.crow.module_home.model.resp.homepage.RankComics
import com.crow.module_home.model.resp.homepage.Topices

/**
 * Results
 *
 * @property mBanners 轮播图
 * @property mFinishComicDatas 已完结漫画
 * @property mHotComics 热门漫画
 * @property mNewComics 新漫画
 * @property mRankDayComics 日排名漫画
 * @property mRankWeekComics 周排名漫画
 * @property mRankMonthComics 月排名漫画
 * @property mRecComicsResult 推荐的漫画
 * @property mTopics 专题系列
 * @constructor Create empty Results
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Results(

    @SerialName(value = "banners")
    val mBanners: List<Banner>,

    @SerialName(value = "finishComics")
    val mFinishComicDatas: FinishComicDatas,

    @SerialName(value = "hotComics")
    val mHotComics: List<HotComic>,

    @SerialName(value = "newComics")
    val mNewComics: List<NewComic>,

    @SerialName(value = "rankDayComics")
    val mRankDayComics: ComicDatas<RankComics>,

    @SerialName(value = "rankWeekComics")
    val mRankWeekComics: ComicDatas<RankComics>,

    @SerialName(value = "rankMonthComics")
    val mRankMonthComics: ComicDatas<RankComics>,

    @SerialName(value = "recComics")
    val mRecComicsResult: ComicDatas<RecComicsResult>,

    @SerialName(value = "topics")
    val mTopics: ComicDatas<Topices>,
)