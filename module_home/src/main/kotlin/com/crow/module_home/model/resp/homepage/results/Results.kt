package com.crow.module_home.model.resp.homepage.results

import com.crow.module_home.model.resp.homepage.*
import com.squareup.moshi.Json

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
data class Results(

    @Json(name = "banners")
    val mBanners: List<Banner>,

    @Json(name = "finishComics")
    val mFinishComicDatas: FinishComicDatas,

    @Json(name = "hotComics")
    val mHotComics: List<HotComic>,

    @Json(name = "newComics")
    val mNewComics: List<NewComic>,

    @Json(name = "rankDayComics")
    val mRankDayComics: ComicDatas<RankComics>,

    @Json(name = "rankWeekComics")
    val mRankWeekComics: ComicDatas<RankComics>,

    @Json(name = "rankMonthComics")
    val mRankMonthComics: ComicDatas<RankComics>,

    @Json(name = "recComics")
    val mRecComicsResult: ComicDatas<RecComicsResult>,

    @Json(name = "topics")
    val mTopics: ComicDatas<Topices>,
)