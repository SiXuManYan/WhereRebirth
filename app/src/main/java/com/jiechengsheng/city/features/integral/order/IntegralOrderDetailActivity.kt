package com.jiechengsheng.city.features.integral.order

import android.graphics.Color
import android.view.View
import com.blankj.utilcode.util.BarUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.integral.IntegralOrderDetail
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.databinding.ActivityIntegralOrderDetailBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/9/26 17:10.
 * 积分订单详情
 */
class IntegralOrderDetailActivity : BaseMvpActivity<IntegralOrderDetailPresenter>(), IntegralOrderDetailView {

    private lateinit var binding: ActivityIntegralOrderDetailBinding

    var orderId = 0

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_integral_order_detail

    override fun initView() {
        binding = ActivityIntegralOrderDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this,Color.WHITE)
        orderId = intent.getIntExtra(Constant.PARAM_ID , 0)
    }

    override fun initData() {
        presenter = IntegralOrderDetailPresenter(this)
        presenter.getData(orderId)
    }

    override fun bindListener() {

    }

    override fun bindData(response: IntegralOrderDetail) {


        when (response.order_status) {
            1 ->{
                binding.statusTv.text = getString(R.string.store_status_3)
                binding.recipientInfoLl.visibility = View.VISIBLE

                response.address?.let {
                    binding.addressNameTv.text = it.address
                    binding.recipientTv.text = getString(R.string.star_text_format, it.contact_name, it.contact_number)
                }

            }
            2 ->{
                binding.statusTv.text = getString(R.string.store_status_5)
                binding.recipientInfoLl.visibility = View.GONE
            }
        }
        binding.priceTv.text = getString(R.string.points_format, response.price)
        binding.orderNumberTv.text = response.trade_no
        binding.createdDateTv.text = response.created_at

        GlideUtil.load(this,response.image,binding.imageIv)
        binding.titleTv.text = response.title
        binding.pointsTv.text = getString(R.string.points_format , response.price)

    }


}