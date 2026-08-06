package com.jiechengsheng.city.features.bills.order

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.order.bill.BillOrderDetails
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.bills.record.result.BillsRecommitResultActivity
import com.jiechengsheng.city.features.gourmet.refund.ComplexRefundActivity
import com.jiechengsheng.city.features.gourmet.refund.ComplexRefundPresenter
import com.jiechengsheng.city.features.gourmet.refund.detail.FoodRefundInfoActivity
import com.jiechengsheng.city.features.gourmet.refund.detail.FoodRefundInfoPresenter
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.widget.list.DividerDecoration
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityBillsDetailBinding
import android.view.ViewGroup


/**
 * Created by Wangsw  2021/7/20 10:49.
 * 水电订单详情
 */
class BillsOrderDetailActivity : BaseMvpActivity<BillsDetailPresenter>(), BillsDetailView {

    private lateinit var binding: ActivityBillsDetailBinding

    private var orderId = 0

    private lateinit var mAdapter: BillOrderDetailAdapter

    override fun getLayoutId() = R.layout.activity_bills_detail

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityBillsDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        intent.extras?.let {
            orderId = it.getInt(Constant.PARAM_ORDER_ID, 0)
        }

        mAdapter = BillOrderDetailAdapter()
        binding.contentRv.apply {
            adapter = mAdapter
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.colorPrimary),
                1,
                SizeUtils.dp2px(15f),
                0))

        }
    }

    override fun initData() {

        presenter = BillsDetailPresenter(this)
        presenter.getDetail(orderId)
    }

    override fun bindListener() {
        binding.backIv.setOnClickListener {
            finish()
        }
    }

    override fun bindDetail(data: BillOrderDetails) {


        /**
         * 订单状态（0-待支付，1-缴费中，2-缴费成功，3-缴费失败，4-订单关闭，5-退款审核中，6-拒绝退款，7-退款中，8-退款成功，9-退款失败）
         */
        val orderStatus = data.order_status
        binding.statusTv.text = BusinessUtils.getBillsStatusText(orderStatus)
        binding.statusDescTv.text = BusinessUtils.getBillsStatusDesc(orderStatus)


        // 退款失败
        if (orderStatus == 9) {
            binding.failReasonRl.visibility = View.VISIBLE
            binding.failReasonTv.text = data.refund_refuse_reason
        } else {
            binding.failReasonRl.visibility = View.GONE
        }

        binding.orderNumberTv.text = data.trade_no
        binding.createdDateTv.text = data.created_at

        // 支付方式
        if (orderStatus == 0) {
            binding.paymentContainerLl.visibility = View.GONE
        } else {
            binding.paymentContainerLl.visibility = View.VISIBLE
            binding.payWayTv.text = data.payment_method
        }

        // 字段
        val billsParams = data.bills_params
        mAdapter.setNewInstance(billsParams)

        when (orderStatus) {
            3 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.to_cancel_order)
                    setOnClickListener {
                        ComplexRefundActivity.navigation(this@BillsOrderDetailActivity,
                            orderId,
                            data.refund_price.toPlainString(),
                            data.total_price.toPlainString(),
                            ComplexRefundPresenter.TYPE_BILL)
                    }
                }
                binding.rightTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.resubmit)
                    setOnClickListener {
                        presenter.recommit(orderId, data.order_type)
                    }
                }
            }
            5, 6, 7, 8 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.refund_information)
                    setOnClickListener {
                        FoodRefundInfoActivity.navigation(this@BillsOrderDetailActivity,
                            orderId, FoodRefundInfoPresenter.TYPE_BILL)
                    }
                }
                binding.rightTv.visibility = View.GONE
            }
            9 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.refund_information)
                    setOnClickListener {
                        FoodRefundInfoActivity.navigation(this@BillsOrderDetailActivity,
                            orderId, FoodRefundInfoPresenter.TYPE_BILL)
                    }
                }
                binding.rightTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.apply_again)
                    setOnClickListener {
                        ComplexRefundActivity.navigation(this@BillsOrderDetailActivity,
                            orderId,
                            data.refund_price.toPlainString(),
                            data.total_price.toPlainString(),
                            ComplexRefundPresenter.TYPE_BILL)
                    }
                }
            }
            else -> {
                binding.bottomContainerRl.visibility = View.GONE
            }
        }


        // 顶部价格
        binding.priceTv.text = getString(R.string.price_unit_format, data.total_price.toPlainString())
        // 支付明细
        binding.paymentAmountBillTv.text = getString(R.string.price_unit_format, data.bills_price.toPlainString())
        binding.serviceChargeBillTv.text = getString(R.string.price_unit_format, data.service_price)
        // 满减价格
        val discounts = data.discounts


        when (data.type) {
            1 -> {
                binding.discountTitleTv.text = getString(R.string.sale_price_bill)
                setDiscount(discounts)
            }
            2 -> {
                binding.discountTitleTv.text = getString(R.string.rebate_price_bill)
                setDiscount(discounts)
            }
            else -> {
                binding.discountRl.visibility = View.GONE
            }
        }

        binding.actualPaymentBillTv.text = getString(R.string.price_unit_format, data.total_price.toPlainString())

        binding.couponMoneyTv.text =getString(R.string.price_unit_format, data.coupon_price)

    }

    private fun setDiscount(discounts: String) {
        if (BusinessUtils.getSafeBigDecimal(discounts).compareTo(BigDecimal.ZERO) == 1) {
            binding.discountRl.visibility = View.VISIBLE
            binding.discountV.visibility = View.VISIBLE
            binding.discountValueTv.text = getString(R.string.price_unit_discount_format, discounts)
        } else {
            binding.discountRl.visibility = View.GONE
            binding.discountV.visibility = View.GONE
        }
    }

    override fun recommitSuccess(orderId: Int) {
        startActivity(BillsRecommitResultActivity::class.java, Bundle().apply {
            putInt(Constant.PARAM_ORDER_ID, orderId)
        })
    }

}
