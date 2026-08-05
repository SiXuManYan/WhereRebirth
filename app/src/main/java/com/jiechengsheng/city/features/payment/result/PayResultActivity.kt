package com.jiechengsheng.city.features.payment.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.ErrorResponse
import com.jiechengsheng.city.api.request.payment.PayStatus
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.bills.record.BillsRecordActivity
import com.jiechengsheng.city.features.main.MainActivity
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.LocalLanguageUtil
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityPayResultBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2023/3/14 17:11.
 * 支付结果页面
 */
class PayResultActivity : BaseMvpActivity<WebPayResultPresenter>(), WebPayResultView {

    private lateinit var binding: ActivityPayResultBinding


    /** 订单 Id */
    private var orderIds = java.util.ArrayList<Int>()

    /**
     *  支付场景
     *  @see PayUrlGet.HOTEL
     *  @see PayUrlGet.RESTAURANT
     *  @see PayUrlGet.TAKEAWAY
     *  @see PayUrlGet.BILL_PAY
     *  @see PayUrlGet.MALL
     */
    private var moduleType = ""

    /** 客户端计算好的待支付金额 */
    private var amountToPaid = ""


    companion object {

        fun navigation(context: Context, moduleType: String, orderIds: ArrayList<Int>, amountToPaid: String) {
            val bundle = Bundle().apply {
                putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
                putString(Constant.PARAM_MODULE_TYPE, moduleType)
                putString(Constant.PARAM_AMOUNT, amountToPaid)
            }

            val intent = if (User.isLogon()) {
                Intent(context, PayResultActivity::class.java).putExtras(bundle)
            } else {
                Intent(context, LoginActivity::class.java)
            }

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_pay_result

    override fun initView() {
        binding = ActivityPayResultBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
        initDefaultUI()
    }


    private fun initExtra() {
        intent.extras?.let {
            val ids = it.getIntegerArrayList(Constant.PARAM_ORDER_IDS)
            if (!ids.isNullOrEmpty()) {
                orderIds.addAll(ids)
            }
            moduleType = it.getString(Constant.PARAM_MODULE_TYPE, "")
            amountToPaid = it.getString(Constant.PARAM_AMOUNT, "0")
        }
    }

    private fun initDefaultUI() {

        val languageLocale = LocalLanguageUtil.getInstance().getSetLanguageLocale(this)

        when (moduleType) {
            PayUrlGet.BILL_PAY -> {
                if (languageLocale.language == "zh") {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill)
                } else {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill_en)
                }
                binding.paymentHint.visibility = View.GONE
            }
            else -> {
                if (languageLocale.language == "zh") {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_complete)
                } else {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_complete_en)
                }
            }
        }

    }


    override fun initData() {
        presenter = WebPayResultPresenter(this)
        showLoadingDialog(false)
        Handler(Looper.myLooper()!!).postDelayed({
            dismissLoadingDialog()
            presenter.getPayStatus(moduleType, orderIds)
        }, 3000)

    }


    override fun bindListener() {
        binding.finishTv.setOnClickListener {
            when (moduleType) {
                PayUrlGet.BILL_PAY -> {
                    // 跳转至水电列表
                    startActivity(BillsRecordActivity::class.java)
                }
                else -> {
                    startActivityClearTop(MainActivity::class.java, Bundle().apply {
                        putInt(Constant.PARAM_TAB, 2)
                    })
                }
            }
            finish()
        }
        binding.viewOrderTv.setOnClickListener {
            binding.finishTv.performClick()
        }
        binding.backIv.setOnClickListener {
            binding.finishTv.performClick()
        }
    }


    override fun onBackPressed() = Unit

    override fun bindPayStatus(response: PayStatus) {
        binding.containerLl.visibility = View.VISIBLE
        val payStatus = response.pay_status

        if (!payStatus) {

            // 支付失败
            binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_complete)
            binding.paymentHint.visibility = View.VISIBLE
            binding.payInfoIv.setImageResource(R.mipmap.ic_paying_loading)
            binding.viewOrderTv.visibility = View.VISIBLE
            return
        }
        binding.payStatusTitleIv.visibility = View.VISIBLE


        val languageLocale = LocalLanguageUtil.getInstance().getSetLanguageLocale(this)

        when (moduleType) {
            PayUrlGet.BILL_PAY -> {
                // 缴费中
                if (languageLocale.language == "zh") {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill)
                } else {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill_en)
                }
                binding.payInfoIv.setImageResource(R.mipmap.ic_paying_bills)
            }
            else -> {
                // 支付成功
                if (languageLocale.language == "zh") {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_success)
                } else {
                    binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_success_en)
                }
                binding.payInfoIv.setImageResource(R.mipmap.ic_pay_success_common)
            }

        }

        // 支付成功
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
    }

    override fun onError(errorResponse: ErrorResponse?) {
        super.onError(errorResponse)
        binding.containerLl.visibility = View.VISIBLE
    }

}