package com.crow.module_mine.model.resp

import com.squareup.moshi.Json



data class MineLoginResultsOkResp (

    @Json(name =  "ads_vip_end")
    val mAdsVipEnd: Any?,

    @Json(name =  "avatar")
    val mIconUrl: String?,

    @Json(name =  "b_sstv")
    val mBSstv: Boolean,

    @Json(name =  "b_verify_email")
    val mBVerifyEmail: Boolean,

    @Json(name =  "cartoon_vip")
    val mCartoonVip: Int,

    @Json(name =  "cartoon_vip_end")
    val mCartoonVipEnd: Any?,

    @Json(name =  "cartoon_vip_start")
    val mCartoonVipStart: Any?,

    @Json(name =  "close_report")
    val mCloseReport: Boolean,

    @Json(name =  "comic_vip")
    val mComicVip: Int,

    @Json(name =  "comic_vip_end")
    val mComicVipEnd: Any?,

    @Json(name =  "comic_vip_start")
    val mComicVipStart: Any?,

    @Json(name =  "datetime_created")
    val mDatetimeCreated: String,

    @Json(name =  "downloads")
    val mDownloads: Int,

    @Json(name =  "email")
    val mEmail: String,

    @Json(name =  "invite_code")
    val mInviteCode: Any?,

    @Json(name =  "invited")
    val mInvited: Any?,

    @Json(name =  "is_authenticated")
    val mIsAuthenticated: Boolean,

    @Json(name =  "mobile")
    val mMobile: Any?,

    @Json(name =  "mobile_region")
    val mMobileRegion: Any?,

    @Json(name =  "nickname")
    val mNickname: String,

    @Json(name =  "point")
    val mPoint: Int,

    @Json(name =  "reward_downloads")
    val mRewardDownloads: Int,

    @Json(name =  "scy_answer")
    val mScyAnswer: Boolean,

    @Json(name =  "token")
    val mToken: String,

    @Json(name =  "user_id")
    val mUserId: String,

    @Json(name =  "username")
    val mUsername: String,

    @Json(name =  "vip_downloads")
    val mVipDownloads: Int,

    /* @Json(name =  "vip_downloads")
     val mDayDownloadsRefresh: String? = null,

     @Json(name =  "day_downloads")
     val mDayDownloads: Int? = null,*/

    var mPassword: String? = null,
)