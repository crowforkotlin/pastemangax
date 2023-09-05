package com.crow.module_user.model.resp

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LoginResultsOkResp (

    @SerialName(value = "ads_vip_end")
    val mAdsVipEnd: @Polymorphic Any?,

    @SerialName(value = "avatar")
    val mIconUrl: String?,

    @SerialName(value = "b_sstv")
    val mBSstv: Boolean,

    @SerialName(value = "b_verify_email")
    val mBVerifyEmail: Boolean,

    @SerialName(value = "cartoon_vip")
    val mCartoonVip: Int,

    @SerialName(value = "cartoon_vip_end")
    val mCartoonVipEnd: @Polymorphic Any?,

    @SerialName(value = "cartoon_vip_start")
    val mCartoonVipStart: @Polymorphic Any?,

    @SerialName(value = "close_report")
    val mCloseReport: Boolean,

    @SerialName(value = "comic_vip")
    val mComicVip: Int,

    @SerialName(value = "comic_vip_end")
    val mComicVipEnd: @Polymorphic Any?,

    @SerialName(value = "comic_vip_start")
    val mComicVipStart: @Polymorphic Any?,

    @SerialName(value = "datetime_created")
    val mDatetimeCreated: String,

    @SerialName(value = "downloads")
    val mDownloads: Int,

    @SerialName(value = "email")
    val mEmail: String,

    @SerialName(value = "invite_code")
    val mInviteCode: @Polymorphic Any?,

    @SerialName(value = "invited")
    val mInvited: @Polymorphic Any?,

    @SerialName(value = "is_authenticated")
    val mIsAuthenticated: Boolean,

    @SerialName(value = "mobile")
    val mMobile: @Polymorphic Any?,

    @SerialName(value = "mobile_region")
    val mMobileRegion: @Polymorphic Any?,

    @SerialName(value = "nickname")
    val mNickname: String,

    @SerialName(value = "point")
    val mPoint: Int,

    @SerialName(value = "reward_downloads")
    val mRewardDownloads: Int,

    @SerialName(value = "scy_answer")
    val mScyAnswer: Boolean,

    @SerialName(value = "token")
    val mToken: String,

    @SerialName(value = "user_id")
    val mUserId: String,

    @SerialName(value = "username")
    val mUsername: String,

    @SerialName(value = "vip_downloads")
    val mVipDownloads: Int,

    /* @SerialName(value = "vip_downloads")
     val mDayDownloadsRefresh: String? = null,

     @SerialName(value = "day_downloads")
     val mDayDownloads: Int? = null,*/

    var mPassword: String? = null,
)