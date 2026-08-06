package com.jiechengsheng.city.features.refund.add.form.third

import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.features.refund.add.verify.RefundBindVerifyActivity
import com.jiechengsheng.city.utils.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.jiechengsheng.city.databinding.ActivityRefundChannelThirdFormBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2022/4/26 9:37.
 * 绑定第三方退款渠道
 */
class ThirdChannelFormActivity : BaseActivity() {

    private lateinit var binding: ActivityRefundChannelThirdFormBinding

    /** 渠道名 */
    private var channelName = ""
    private var channelCode = ""
    private var channelCategory = ""

    /**  用户名 */
    private var userName = ""

    /** 账号 */
    private var accountNumber = ""

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_refund_channel_third_form

    override fun initView() {
        binding = ActivityRefundChannelThirdFormBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        initExtra()
        binding.userNameEt.addTextChangedListener(
            afterTextChanged = {
                userName = it.toString().trim()
                handleAlpha()
            }
        )

        binding.bankAccountEt.addTextChangedListener(
            afterTextChanged = {
                accountNumber = it.toString().trim()
                handleAlpha()
            }
        )

    }

    private fun initExtra() {
        val eventBus = EventBus.getDefault()
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this)
        }

        intent.extras?.let {
            channelName = it.getString(Constant.PARAM_CHANNEL_NAME, "")
            channelCode = it.getString(Constant.PARAM_REFUND_CHANNEL_CODE, "")
            channelCategory = it.getString(Constant.PARAM_REFUND_CHANNEL_CATEGORY, "")
        }
    }

    private fun handleAlpha() {
        if (userName.isNotBlank() && accountNumber.isNotBlank()) {
            binding.nextTv.alpha = 1.0f
        } else {
            binding.nextTv.alpha = 0.5f
        }

    }

    override fun initData() = Unit

    override fun bindListener() {


        binding.nextTv.setOnClickListener {

            if (userName.isBlank()) {
                ToastUtils.showShort(R.string.edit_account_name_hint)
                return@setOnClickListener
            }
            if (accountNumber.isBlank()) {
                ToastUtils.showShort(R.string.enter_account_number_hint)
                return@setOnClickListener
            }
            // 跳转至短信验证
            RefundBindVerifyActivity.navigation(this, channelName, userName, accountNumber,channelCode,channelCategory)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val eventBus = EventBus.getDefault()
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventReceived(baseEvent: BaseEvent<*>) {
        when (baseEvent.code) {
            EventCode.EVENT_REFUND_METHOD_ADD_SUCCESS -> {
                finish()
            }
        }
    }

}