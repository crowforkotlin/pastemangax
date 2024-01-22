package com.crow.module_mine.ui.viewmodel

import android.os.Build
import com.crow.base.app.app
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_mine.R
import com.crow.module_mine.model.MineIntent
import com.crow.module_mine.model.resp.MineLoginResultsOkResp
import com.crow.module_mine.model.resp.user_info.Info
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.crow.mangax.R as mangaR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/viewmodel
 * @Time: 2023/3/22 14:56
 * @Author: CrowForKotlin
 * @Description: UserInfoViewModel
 * @formatter:on
 **************************/
class MineInfoViewModel : BaseMviViewModel<MineIntent>() {

    val mUserUpdateInfoData = arrayListOf<Pair<Int,String>>()

    private fun timestamp(time: String): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            OffsetDateTime
                .parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX"))
                .toLocalDate()
                .withDayOfMonth(1)
                .toString()
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.US).parse(time) ?: throw NullPointerException("parse time is null"))
        }
    }

    private fun String.getDate() = runCatching {timestamp(this) }.getOrElse { app.getString(mangaR.string.mangax_unknow_error) }

    fun setData(userInfo: MineLoginResultsOkResp) {
        mUserUpdateInfoData.add(R.drawable.mine_ic_time_24dp to app.getString(R.string.mine_datetime_created, userInfo.mDatetimeCreated.getDate()))
        mUserUpdateInfoData.add(mangaR.drawable.base_ic_download_24dp to app.getString(R.string.mine_download_count, userInfo.mDownloads.toString()))
        mUserUpdateInfoData.add(R.drawable.mine_ic_email_24dp to app.getString(R.string.mine_email, userInfo.mEmail.ifEmpty { "暂无" }))
    }

    fun setData(info: Info) {
        mUserUpdateInfoData.add(0, R.drawable.mine_ic_gender_24dp to app.getString(R.string.mine_gender, info.mGender.mDisplay))
        mUserUpdateInfoData.add(0, R.drawable.mine_ic_usr_24dp to app.getString(R.string.mine_username, info.mUsername))
        mUserUpdateInfoData.add(0, R.drawable.mine_ic_usr_24dp to app.getString(R.string.mine_nickname, info.mNickname))
    }

    fun doClearUserUpdateInfoData() { mUserUpdateInfoData.clear() }
}