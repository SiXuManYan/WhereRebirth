package com.jiechengsheng.city.features.daily.sign

import android.widget.CheckedTextView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatCheckBox
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.SignListResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.utils.LocalLanguageUtil
import com.jiechengsheng.city.utils.ShareUtil
import com.jiechengsheng.city.utils.SPKey
import com.jiechengsheng.city.databinding.SignInBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/11/24 16:32.
 * 每日签到
 */
class SignInActivity : BaseMvpActivity<SignInPresenter>(), SignInView {

    private lateinit var binding: SignInBinding

    private var inviteLink = ""

    override fun getLayoutId() = R.layout.sign_in

    override fun initView() {
        binding = SignInBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initLanguage()
        inviteLink = SPUtils.getInstance().getString(SPKey.K_INVITE_LINK, "")
        if (inviteLink.isBlank()) {
            binding.inviteTv.isChecked = true
            binding.inviteTv.isClickable = false
        } else {
            binding.inviteTv.isChecked = false
            binding.inviteTv.isClickable = true
        }

    }

    private fun initLanguage() {
        val languageLocale = LocalLanguageUtil.getInstance().getSetLanguageLocale(this)
        if (languageLocale.language == "zh") {
            binding.adIv.setImageResource(R.mipmap.ic_sign_in_ad_chn)
        } else {
            binding.adIv.setImageResource(R.mipmap.ic_sign_in_ad_en)
        }
    }

    override fun initData() {
        presenter = SignInPresenter(this)
        presenter.getUserInfo()
        presenter.getSignInList()
    }

    override fun bindListener() {
        binding.ruleTv.setOnClickListener {
            startActivity(SignInRuleActivity::class.java)
        }
        binding.signInNowTv.setOnClickListener {
            presenter.signIn()
        }
        binding.signInTv.setOnClickListener {
            binding.signInNowTv.performClick()
        }
        binding.inviteTv.setOnClickListener {
            ShareUtil.shareLink(inviteLink, this)
        }
    }

    override fun bindUserIntegral(isSigned: Boolean) {
        if (isSigned) {
            binding.signInNowTv.apply {
                setText(R.string.already_sign_in)
                isClickable = false
                setTextColor(ColorUtils.getColor(R.color.black_33333360))
            }
            binding.signInTv.apply {
                setText(R.string.finished)
                isClickable = false
            }
        } else {
            binding.signInNowTv.setText(R.string.sign_in_now)
            binding.signInTv.setText(R.string.to_finish)
        }
        binding.signInTv.isChecked = isSigned
    }


    override fun bindSignInList(response: SignListResponse) {

        val data = response.data

        data.forEachIndexed { index, dataBean ->

            if (index <= 3) {
                bindCommonItem(index, dataBean, binding.firstDateLl)
            }
            if (index == 4 || index == 5) {
                bindCommonItem(index - 4, dataBean, binding.secondDateLl)
            }
            if (index == 6) {
                val signStatus = dataBean.signStatus
                val integral = dataBean.integral
                binding.finalDateTv.isChecked = signStatus == 1
                binding.finalScoreTv.isChecked = signStatus == 1
                binding.finalScoreTv.text = StringUtils.getString(R.string.add_format, integral)
                if (signStatus == 1) {
                    binding.finalContainerRl.setBackgroundResource(R.drawable.shape_sign_in_radius_4_selected)
                } else {
                    binding.finalContainerRl.setBackgroundResource(R.drawable.shape_sign_in_radius_4_normal)
                }

            }
        }

    }

    private fun bindCommonItem(index: Int, dataBean: SignListResponse.DataBean, container: LinearLayout) {

        val signStatus = dataBean.signStatus
        val integral = dataBean.integral

        val firstItem = container.getChildAt(index) as LinearLayout
        val date = firstItem.getChildAt(0) as CheckedTextView
        val tagIv = firstItem.getChildAt(1) as AppCompatCheckBox
        val score = firstItem.getChildAt(2) as CheckedTextView
        date.isChecked = signStatus == 1
        tagIv.isChecked = signStatus == 1
        score.isChecked = signStatus == 1
        score.text = StringUtils.getString(R.string.add_format, integral)

        if (signStatus == 1) {
            firstItem.setBackgroundResource(R.drawable.shape_sign_in_radius_4_selected)
        } else {
            firstItem.setBackgroundResource(R.drawable.shape_sign_in_radius_4_normal)
        }
    }


    override fun signInSuccess() {
        bindUserIntegral(true)
        presenter.getSignInList()
    }

}