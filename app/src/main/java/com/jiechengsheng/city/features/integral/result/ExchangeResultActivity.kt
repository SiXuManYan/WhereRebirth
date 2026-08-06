package com.jiechengsheng.city.features.integral.result

import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.features.coupon.user.MyCouponActivity
import com.jiechengsheng.city.databinding.ActivityExchangeResultBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2023/2/10 16:03.
 *
 */
class ExchangeResultActivity : BaseActivity() {

    private lateinit var binding: ActivityExchangeResultBinding

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_exchange_result

    override fun initView() {
        binding = ActivityExchangeResultBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

    }

    override fun initData() = Unit

    override fun bindListener() {
        binding.viewCouponTv.setOnClickListener {
            startActivityAfterLogin(MyCouponActivity::class.java)
        }
    }
}