package com.jiechengsheng.city.features.store.pay.result

import android.os.Bundle
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.features.main.MainActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.databinding.ActivityStorePayResultBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/6/24 16:20.
 *  支付成功
 */
class StorePayResultActivity : BaseActivity() {

    private lateinit var binding: ActivityStorePayResultBinding

    /**
     * 0 商城订单
     * 1 水电订单
     * 3 美食
     * 4 外卖
     */
    private var useType = 0


    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_store_pay_result

    override fun initView() {
        binding = ActivityStorePayResultBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)


        intent.extras?.let {

            useType = it.getInt(Constant.PARAM_TYPE)
        }

    }

    override fun initData() = Unit

    override fun onBackPressed() = Unit

    override fun bindListener() {

        mJcsTitle.setBackIvClickListener {

        }

        binding.checkOrderTv.setOnClickListener {

            if (useType == Constant.PAY_INFO_ESTORE ||
                useType == Constant.PAY_INFO_FOOD ||
                useType == Constant.PAY_INFO_TAKEAWAY ||
                useType == Constant.PAY_INFO_HOTEL ||
                useType == Constant.PAY_INFO_MALL
            ) {
                startActivityClearTop(MainActivity::class.java, Bundle().apply {
                    putInt(Constant.PARAM_TAB, 2)
                })
            }
            if (useType == Constant.PAY_INFO_BILLS) {
//                startActivityClearTop(PaymentRecordActivity::class.java, null)
            }
            finish()
        }
    }
}