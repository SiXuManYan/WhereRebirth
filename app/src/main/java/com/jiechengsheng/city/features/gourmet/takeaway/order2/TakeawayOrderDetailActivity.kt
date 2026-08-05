package com.jiechengsheng.city.features.gourmet.takeaway.order2

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.*
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.hotel.ComplaintRequest
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.gourmet.order.TakeawayOrderDetail
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.gourmet.comment.post.FoodCommentPostActivity
import com.jiechengsheng.city.features.gourmet.refund.ComplexRefundActivity
import com.jiechengsheng.city.features.gourmet.refund.ComplexRefundPresenter
import com.jiechengsheng.city.features.gourmet.refund.detail.FoodRefundInfoActivity
import com.jiechengsheng.city.features.gourmet.refund.detail.FoodRefundInfoPresenter
import com.jiechengsheng.city.features.gourmet.takeaway.order.TakeawayGoodDataAdapter
import com.jiechengsheng.city.features.gourmet.takeaway.order.TakeawayOrderDetailPresenter
import com.jiechengsheng.city.features.gourmet.takeaway.order.TakeawayOrderDetailView
import com.jiechengsheng.city.features.mall.refund.complaint.ComplaintActivity
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.widget.list.DividerDecoration
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityTakeawayOrderDetail2Binding
import android.view.ViewGroup


/**
 * Created by Wangsw  2021/7/28 16:12.
 * 美食外卖模块订单详情
 */
class TakeawayOrderDetailActivity : BaseMvpActivity<TakeawayOrderDetailPresenter>(), TakeawayOrderDetailView {

    private lateinit var binding: ActivityTakeawayOrderDetail2Binding

    private var orderId = 0;
    private var contactNumber = "";
    private var merUuid = "";
    private var restaurantName = "";
    private lateinit var mAdapter: TakeawayGoodDataAdapter
    private var tel = ""
    private var alreadyComplaint = false

    override fun getLayoutId() = R.layout.activity_takeaway_order_detail_2

    /** 处理申诉 */
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            alreadyComplaint = true
            ToastUtils.showShort(R.string.complained_success)
        }
    }

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityTakeawayOrderDetail2Binding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        BarUtils.setStatusBarColor(this, Color.WHITE)
        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }
        orderId = bundle.getInt(Constant.PARAM_ORDER_ID, 0)

        mAdapter = TakeawayGoodDataAdapter()
        binding.goodRv.apply {
            adapter = mAdapter
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.white), SizeUtils.dp2px(10f), 0, 0).apply {
                setDrawHeaderFooter(false)
            })
            layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun canScrollVertically(): Boolean = false
            }
            isNestedScrollingEnabled = true
        }


    }

    override fun initData() {
        presenter = TakeawayOrderDetailPresenter(this)
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
                    .putExtra(Constant.PARAM_TYPE, ComplaintRequest.TYPE_FOOD_TAKEAWAY)
                searchLauncher.launch(intent)
            }
        }

    }

    override fun bindDetail(it: TakeawayOrderDetail) {
        val goodData = it.good_data
        val restaurantData = it.restaurant_data
        val orderData = it.order_data
        val paymentChannel = it.payment_data
        merUuid = restaurantData.mer_uuid
        restaurantName = restaurantData.name
        tel = restaurantData.tel

        alreadyComplaint = orderData.complaint_status == 1

        /** 1-待支付，2-支付审核中，3-交易取消，4-交易关闭，5-待接单，6-已接单，7-待收货，8-交易成功，9-退款中，10-退款成功 11-商家审核中 12-拒绝售后 13-退款失败 */
        val status = orderData.status
        binding.statusTv.text = BusinessUtils.getTakeawayStatusText(status)
        binding.statusDescTv.text = BusinessUtils.getTakeawayOrderStatusDesc(status)

        val orderPrice = orderData.price
        binding.priceTv.text = getString(R.string.price_unit_format, orderPrice.toPlainString())
        binding.orderNumberTv.text = orderData.trade_no
        binding.createdDateTv.text = orderData.created_at

        binding.deliveryTimeTv.text = if (orderData.delivery_time_type == 1) {
            getString(R.string.delivery_now)
        } else {
            orderData.delivery_time
        }

        val address = orderData.address
        contactNumber = address.contact_number
        val contactName = address.contact_name
        binding.addressNameTv.text = address.address
        binding.contactNameTv.text = getString(R.string.address_name_format, contactName, contactNumber)


        if (status != 1 && status != 3) {
            binding.paymentContainerLl.visibility = View.VISIBLE
            binding.payWayTv.text = paymentChannel.payment_channel
        } else {
            binding.paymentContainerLl.visibility = View.GONE

        }

        binding.businessNameTv.text = restaurantName
        binding.packagingFeeTv.text = getString(R.string.price_unit_format, orderData.packing_charges.toPlainString())
        binding.deliveryFeeTv.text = getString(R.string.price_unit_format, orderData.delivery_cost.toPlainString())
        binding.remarksTv.text = orderData.remark

        mAdapter.setNewInstance(goodData)


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
                    orderIds.add(orderId)
                    /*      startActivityAfterLogin(StorePayActivity::class.java, Bundle().apply {
                              putDouble(Constant.PARAM_TOTAL_PRICE, price.toDouble())
                              putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
                              putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_TAKEAWAY)
                          })*/

//                    WebPayActivity.navigation(this, Constant.PAY_INFO_TAKEAWAY, orderIds)
                    PayCounterActivity.navigation(this, PayUrlGet.TAKEAWAY, orderIds, orderPrice.toPlainString())

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
            8 -> {
                if (orderData.comment_status == 1) {
                    binding.bottomContainerRl.visibility = View.VISIBLE
                    binding.leftTv.apply {

                        visibility = View.GONE
                    }
                    binding.rightTv.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.evaluation)
                    }
                    binding.rightTv.setOnClickListener {
                        startActivity(FoodCommentPostActivity::class.java, Bundle().apply {
                            putInt(Constant.PARAM_ORDER_ID, orderId)
                            putInt(Constant.PARAM_RESTAURANT_ID, restaurantData.id)
                            putInt(Constant.PARAM_TYPE, 2)
                        })
                    }
                } else {
                    binding.bottomContainerRl.visibility = View.GONE
                }
            }
            9, 11, 12 -> {
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
            13 -> {
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


        // 投诉
        binding.complaintTv.visibility = if (status == 12) View.VISIBLE else View.GONE

        // 退款失败
        if (status == 13) {
            binding.failReasonRl.visibility = View.VISIBLE
            binding.reasonSplitV.visibility = View.VISIBLE
            binding.failReasonTv.text = orderData.error_reason
        } else {
            binding.failReasonRl.visibility = View.GONE
            binding.reasonSplitV.visibility = View.GONE
        }

    }

    override fun cancelSuccess() {
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
        presenter.getDetail(orderId)
    }


    private fun doRefund(price: BigDecimal) {
        ComplexRefundActivity.navigation(this,
            orderId,
            price.toPlainString(),
            price.toPlainString(),
            ComplexRefundPresenter.TYPE_TAKEAWAY)
    }

    private fun viewRefundInfo() = FoodRefundInfoActivity.navigation(this, orderId, FoodRefundInfoPresenter.TYPE_TAKEAWAY)


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
