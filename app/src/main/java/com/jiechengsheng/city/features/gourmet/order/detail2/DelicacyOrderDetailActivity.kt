package com.jiechengsheng.city.features.gourmet.order.detail2

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.hotel.ComplaintRequest
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.gourmet.order.FoodOrderDetail
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.gourmet.comment.post.FoodCommentPostActivity
import com.jiechengsheng.city.features.gourmet.refund.ComplexRefundActivity
import com.jiechengsheng.city.features.gourmet.refund.detail.FoodRefundInfoActivity
import com.jiechengsheng.city.features.gourmet.refund.detail.FoodRefundInfoPresenter
import com.jiechengsheng.city.features.mall.refund.complaint.ComplaintActivity
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityDelicacyOrderDetailBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/7/23 15:59.
 * 美食订单详情
 */
class DelicacyOrderDetailActivity : BaseMvpActivity<DelicacyOrderDetailPresenter>(), DelicacyOrderDetailView {

    private lateinit var binding: ActivityDelicacyOrderDetailBinding

    private var orderId = 0
    private var merUuid = ""
    private var restaurantName = ""
    private var tel = ""
    private var alreadyComplaint = false

    override fun getLayoutId() = R.layout.activity_delicacy_order_detail


    /** 处理申诉 */
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            alreadyComplaint = true
            ToastUtils.showShort(R.string.complained_success)
        }
    }

    override fun initView() {
        binding = ActivityDelicacyOrderDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }
        orderId = bundle.getInt(Constant.PARAM_ORDER_ID, 0)
    }

    override fun isStatusDark() = true

    override fun initData() {
        presenter = DelicacyOrderDetailPresenter(this)
        presenter.getDetail(orderId)
    }

    override fun bindListener() {
        binding.backIv.setOnClickListener {
            finish()
        }
        binding.phoneLl.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$tel")
            }
            startActivity(intent)
        }
        binding.serviceLl.setOnClickListener {
            if (merUuid.isBlank()) {
                return@setOnClickListener
            }
            BusinessUtils.showImUnavailableDialog(this)
        }
        binding.complaintTv.setOnClickListener {
            // 投诉
            if (alreadyComplaint) {
                ToastUtils.showShort(R.string.complained_success)
            } else {
                val intent = Intent(this, ComplaintActivity::class.java)
                    .putExtra(Constant.PARAM_ORDER_ID, orderId)
                    .putExtra(Constant.PARAM_TYPE, ComplaintRequest.TYPE_FOOD)
                searchLauncher.launch(intent)
            }
        }

    }

    override fun bindDetail(it: FoodOrderDetail) {

        val goodData = it.good_data
        val restaurantData = it.restaurant_data
        val orderData = it.order_data
        val paymentChannel = it.payment_data

        merUuid = restaurantData.mer_uuid
        restaurantName = restaurantData.name
        tel = restaurantData.tel

        alreadyComplaint = orderData.complaint_status == 1

        /**
         * 订单状态（1-待支付，2-支付审核中，3-交易取消，4-交易关闭，5-待使用，6-交易成功，7-退款中，8-退款成功 9-商家审核中 10-拒绝售后 11-退款失败）
         */
        val status = orderData.status
        binding.statusTv.text = BusinessUtils.getDelicacyOrderStatusText(status)

        val orderPrice = orderData.price
        val goodPrice = goodData.price

        binding.priceTv.text = getString(R.string.price_unit_format, orderPrice.toPlainString())
        binding.orderNumberTv.text = orderData.trade_no
        binding.createdDateTv.text = orderData.created_at
        binding.phoneTv.text = orderData.phone
        binding.validPeriodTv.text = getString(R.string.valid_period_format2, orderData.start_date, orderData.end_date)
        binding.validPeriodRl.visibility = if (status == 3) {
            View.VISIBLE
        } else {
            View.GONE
        }

        if (status != 1 && status != 3) {
            binding.payContainerLl.visibility = View.VISIBLE
            binding.payWayTv.text = paymentChannel.payment_channel
        } else {
            binding.payContainerLl.visibility = View.GONE
        }


        binding.businessNameTv.text = restaurantData.name

        GlideUtil.load(this, goodData.good_image, binding.imageIv, 4)
        binding.goodNameTv.text = goodData.name
        binding.goodCountTv.text = getString(R.string.quantity_format, goodData.good_num)
        binding.goodPriceTv.text = getString(R.string.price_unit_format, goodPrice.toPlainString())

        if (status == 5) {
            binding.couponInfoLl.visibility = View.VISIBLE
            binding.orderCodeInformation.text = orderData.coupon_no
        } else {
            binding.couponInfoLl.visibility = View.GONE
        }



        binding.complaintTv.visibility = if (status == 10) View.VISIBLE else View.GONE
        // 退款失败
        if (status == 11) {
            binding.failReasonRl.visibility = View.VISIBLE
            binding.reasonSplitV.visibility = View.VISIBLE
            binding.failReasonTv.text = orderData.error_reason
        } else {
            binding.failReasonRl.visibility = View.GONE
            binding.reasonSplitV.visibility = View.GONE
        }

        // 处理状态描述文案
        binding.statusDescTv.text = BusinessUtils.getDelicacyOrderStatusDesc(status)


        // 处理底部
        when (status) {
            1 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    text = getString(R.string.to_cancel_order)
                    visibility = View.VISIBLE
                }
                binding.rightTv.apply {
                    text = getString(R.string.to_pay_2)
                    visibility = View.VISIBLE
                }

                binding.leftTv.setOnClickListener {

                    AlertDialog.Builder(this)
                        .setTitle(R.string.prompt)
                        .setMessage(R.string.cancel_order_confirm)
                        .setPositiveButton(R.string.ensure) { dialogInterface, i ->
                            presenter.cancelOrder(orderId)
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
                        .create().show()

                }

                binding.rightTv.setOnClickListener {

                    val orderIds = ArrayList<Int>()
                    orderIds.add(orderData.id)
/*
                    startActivityAfterLogin(StorePayActivity::class.java, Bundle().apply {
                        putDouble(Constant.PARAM_TOTAL_PRICE, price.toDouble())
                        putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
                        putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_FOOD)
                    })
*/
//                    WebPayActivity.navigation(this, Constant.PAY_INFO_FOOD, orderIds)
                    PayCounterActivity.navigation(this, PayUrlGet.RESTAURANT, orderIds, orderPrice.toPlainString())

                }
            }
            5 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    text = getString(R.string.to_refund)
                    visibility = View.VISIBLE
                }
                binding.rightTv.apply {
                    visibility = View.GONE
                }
                binding.leftTv.setOnClickListener {
                    doRefund(orderPrice)
                }
            }
            6 -> {

                if (orderData.comment_status == 1) {
                    binding.bottomContainerRl.visibility = View.VISIBLE
                    binding.leftTv.apply {

                        visibility = View.GONE
                    }
                    binding.rightTv.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.evaluation_go)
                    }
                    binding.rightTv.setOnClickListener {
                        startActivity(FoodCommentPostActivity::class.java, Bundle().apply {
                            putInt(Constant.PARAM_ORDER_ID, orderId)
                            putInt(Constant.PARAM_RESTAURANT_ID, restaurantData.id)
                            putInt(Constant.PARAM_TYPE, 1)
                        })
                    }
                } else {
                    binding.bottomContainerRl.visibility = View.GONE
                }

            }
            7, 8, 9, 10 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.refund_information)
                    setOnClickListener {
                        // 查看退款信息
                        viewRefundInfo()
                    }
                }
                binding.rightTv.visibility = View.GONE
            }
            11 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.refund_information)
                    setOnClickListener {
                        // 查看退款信息
                        viewRefundInfo()
                    }
                }
                binding.rightTv.apply {
                    visibility = View.VISIBLE
                    text = StringUtils.getString(R.string.apply_again)
                    setOnClickListener {
                        // 再次申请
                        doRefund(orderPrice)
                    }
                }
            }
            else -> {
                binding.bottomContainerRl.visibility = View.GONE
            }
        }
        binding.bottomV.visibility = binding.bottomContainerRl.visibility


    }

    private fun doRefund(price: BigDecimal) {
        ComplexRefundActivity.navigation(this, orderId, price.toPlainString(), price.toPlainString())
    }

    private fun viewRefundInfo() = FoodRefundInfoActivity.navigation(this, orderId, FoodRefundInfoPresenter.TYPE_FOOD)

    override fun cancelSuccess() {
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
        presenter.getDetail(orderId)

    }

    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        when (baseEvent.code) {
            EventCode.EVENT_CANCEL_PAY -> finish()
            EventCode.EVENT_REFRESH_ORDER_LIST -> {
                // 退款成功
                presenter.getDetail(orderId)
            }
            else -> {}
        }


    }

}
