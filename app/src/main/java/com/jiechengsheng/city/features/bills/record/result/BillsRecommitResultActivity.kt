package com.jiechengsheng.city.features.bills.record.result

import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.bills.BillStatus
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.LocalLanguageUtil
import com.jiechengsheng.city.databinding.ActivityBillsResultBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/6/11 15:00.
 * 缴费状态
 */
class BillsRecommitResultActivity : BaseMvpActivity<BillsRecommitResultPresenter>(), BillsRecommitResultView {

    private lateinit var binding: ActivityBillsResultBinding

    var orderId = 0

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_bills_result

    override fun initView() {
        binding = ActivityBillsResultBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initDefaultUI()
        intent.extras?.let {
            orderId = it.getInt(Constant.PARAM_ORDER_ID, 0)
        }
    }

    private fun initDefaultUI() {
        val languageLocale = LocalLanguageUtil.getInstance().getSetLanguageLocale(this)
        if (languageLocale.language == "zh") {
            binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill_fail)
        } else {
            binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill_fail_en)
        }

    }

    override fun initData() {
        presenter = BillsRecommitResultPresenter(this)
        presenter.getStatus(orderId)
    }

    override fun bindListener() {
        binding.finishTv.setOnClickListener {
            finish()
        }

        binding.viewOrderTv.setOnClickListener {
            finish()
        }
    }

    override fun bindStatus(response: BillStatus) {
        val status = response.transaction_status

        val languageLocale = LocalLanguageUtil.getInstance().getSetLanguageLocale(this)


        if (status) {
            if (languageLocale.language == "zh") {
                binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_success_bill)
            } else {
                binding.payStatusTitleIv.setImageResource(R.mipmap.ic_pay_success_bill_en)
            }
            binding.payInfoIv.setImageResource(R.mipmap.ic_pay_success_bill2)
        } else {
            if (languageLocale.language == "zh") {
                binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill_fail)
            } else {
                binding.payStatusTitleIv.setImageResource(R.mipmap.ic_paying_bill_fail_en)
            }
            binding.payInfoIv.setImageResource(R.mipmap.ic_pay_fail_bill)
        }

    }

}