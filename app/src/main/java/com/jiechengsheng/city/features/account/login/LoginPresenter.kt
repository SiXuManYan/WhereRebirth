package com.jiechengsheng.city.features.account.login

import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.jiechengsheng.city.BaseApplication
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.network.BaseMvpPresenter
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.CacheUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.SPKey
import com.jiechengsheng.city.utils.ThreePartyAccount
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Wangsw  2021/1/28 16:43.
 *
 * 离线演示模式下登录不再请求服务器，任意密码/验证码均可通过。
 * 登录成功后在 Room 中写入本地演示用户，Mine 等页面可立即读取。
 */
class LoginPresenter(private val mView: LoginView) : BaseMvpPresenter(mView) {

    companion object {
        const val PLATFORM_FACEBOOK = "Facebook"
        const val PLATFORM_GOOGLE = "Google"

        private const val DEMO_TOKEN = "where-rebirth-local-token"
        private const val DEMO_AVATAR = "android.resource://com.jiechengsheng.city/drawable/mock_avatar_1"
    }

    lateinit var context: Context

    /**
     * 处理登录。离线演示：不校验密码和验证码内容。
     */
    fun handleLogin(isVerifyMode: Boolean, prefix: String?, account: String?, verifyCode: String?, password: String?) {
        if (TextUtils.isEmpty(account)) {
            ToastUtils.showShort(R.string.login_phone_input)
            return
        }
        if (isVerifyMode && TextUtils.isEmpty(verifyCode)) {
            ToastUtils.showShort(StringUtils.getString(R.string.verify_code_hint))
            return
        }
        if (!isVerifyMode && TextUtils.isEmpty(password)) {
            ToastUtils.showShort(StringUtils.getString(R.string.input_password_hint))
            return
        }
        loginLocally(account!!)
    }

    private fun loginLocally(account: String) {
        CacheUtil.saveToken(DEMO_TOKEN)
        saveDemoUser(account)
        ToastUtils.showShort(R.string.login_local_mode_hint)
        mView.LoginSuccess()
    }

    /**
     * 获取验证码。离线演示直接进入倒计时，不请求服务器。
     */
    fun getVerifyCode(prefix: String?, account: String?, getVerifyTv: TextView) {
        if (TextUtils.isEmpty(account)) {
            ToastUtils.showShort(R.string.login_phone_input)
            return
        }
        getVerifyTv.isClickable = false
        countdown(getVerifyTv, StringUtils.getString(R.string.get_again))
        ToastUtils.showShort(R.string.verify_code_send_success)
    }

    /**
     * 倒计时
     */
    private fun countdown(countdownView: TextView, defaultStr: String) {
        Flowable.intervalRange(0, Constant.WAIT_DELAYS.toLong(), 0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { aLong: Long ->
                val string = StringUtils.getString(R.string.count_down_format, Constant.WAIT_DELAYS - aLong)
                countdownView.text = string
            }
            .doOnComplete {
                countdownView.text = defaultStr
                countdownView.isClickable = true
            }.subscribe()
    }

    /**
     * 第三方授权。离线演示直接返回模拟账号，不再调起 ShareSDK。
     */
    fun threePartyAuthorize(platformName: String) {
        val isFacebook = platformName == PLATFORM_FACEBOOK
        val account = ThreePartyAccount(
            platformName = platformName,
            userName = if (isFacebook) "Facebook Demo" else "Google Demo",
            userId = if (isFacebook) "facebook-demo-001" else "google-demo-001",
            userIcon = DEMO_AVATAR,
        )
        CacheUtil.saveToken(DEMO_TOKEN)
        saveDemoUser(account.userName, account.userName, account.userIcon)
        ToastUtils.showShort(R.string.login_local_mode_hint)
        mView.LoginSuccess()
    }

    private fun saveDemoUser(account: String, nickname: String = "Where Demo", avatar: String = DEMO_AVATAR) {
        val user = User.Builder.anUser()
            .id(1L)
            .nickName(nickname)
            .phone(account)
            .email("demo@whererebirth.local")
            .avatar(avatar)
            .balance("1000.00")
            .createdAt("2026-01-01 00:00:00")
            .name(nickname)
            .type(1)
            .countryCode("63")
            .merchantApplyStatus(2)
            .faceBookBindStatus(2)
            .googleBindStatus(2)
            .twitterBindStatus(2)
            .signStatus(2)
            .integral("100")
            .build()

        val whereApp = Utils.getApp() as BaseApplication
        whereApp.database.userDao().addUser(user)
        User.update()
        SPUtils.getInstance().put(SPKey.K_INVITE_LINK, "https://example.com/local/invite")
    }
}
