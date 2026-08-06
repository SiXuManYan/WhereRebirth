package com.jiechengsheng.city.features.store.order.detail

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.order.store.StoreOrderDetail
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.EventCode.EVENT_REFRESH_ORDER_LIST
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.features.store.comment.detail.StoreCommentDetailActivity
import com.jiechengsheng.city.features.store.comment.post.StoreCommentPostActivity
import com.jiechengsheng.city.features.store.refund.StoreRefundActivity
import com.jiechengsheng.city.features.store.refund.detail.StoreRefundDetailActivity
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.widget.list.DividerDecoration
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityStoreOrderDetailBinding
import android.view.ViewGroup


/**
 * Created by Wangsw  2021/6/26 11:15.
 *  商城订单详情
 */
class StoreOrderDetailActivity : BaseMvpActivity<StoreOrderDetailPresenter>(), StoreOrderDetailView {

    private lateinit var binding: ActivityStoreOrderDetailBinding


    private var orderId = 0

    private var totalPrice = 0.0

    private lateinit var mAdapter: StoreOrderDetailAdapter

    private var tel = ""

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_store_order_detail

    override fun initView() {
        binding = ActivityStoreOrderDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        intent.extras?.let {
            orderId = it.getInt(Constant.PARAM_ORDER_ID, 0)
        }

        mAdapter = StoreOrderDetailAdapter()
        binding.goodRv.apply {
            isNestedScrollingEnabled = true
            layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun canScrollVertically() = false
            }
            adapter = mAdapter
            addItemDecoration(
                DividerDecoration(ColorUtils.getColor(R.color.white), SizeUtils.dp2px(10f), 0, 0)
            )
        }


    }

    override fun initData() {
        presenter = StoreOrderDetailPresenter(this)
        presenter.getOrderDetail(orderId)

    }

    override fun bindListener() {
        binding.phoneLl.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$tel")
            }
            startActivity(intent)
        }

    }


    override fun bindData(data: StoreOrderDetail) {
        tel = data.phone
        binding.statusTv.text = presenter.getStatusText(data.delivery_type, data.status)
        presenter.getStatusDescText(binding.statusDescTv, data.status)
        binding.orderNumberTv.text = data.trade_no
        binding.createdDateTv.text = data.created_at
        val price = data.price
        totalPrice = price.toDouble()
        binding.priceTv.text = getString(R.string.price_unit_format, price.toPlainString())


        // 商家服务
        if (data.delivery_type == 1) {
            binding.deliveryValueTv.text = getString(R.string.self_extraction)
            binding.serviceSecondTitleTv.text = getString(R.string.phone_number)
            binding.serviceSecondValueTv.text = data.phone
            binding.recipientInfoLl.visibility = View.GONE

            // 商品备注
            binding.deliveryRl.visibility = View.GONE
            binding.remarkRl.visibility = View.GONE
        } else {
            binding.deliveryValueTv.text = getString(R.string.express)
            binding.serviceSecondTitleTv.text = getString(R.string.delivery_time)
            binding.serviceSecondValueTv.text = data.delivery_times
            binding.recipientInfoLl.visibility = View.VISIBLE
            data.address?.let {
                binding.addressNameTv.text = it.address
                binding.recipientTv.text = getString(R.string.star_text_format, it.contact_name, it.contact_number)
            }

            // 商品备注
            binding.deliveryRl.visibility = View.VISIBLE
            binding.remarkRl.visibility = View.VISIBLE
            binding.deliveryPriceTv.text = getString(R.string.price_unit_format, data.delivery_fee.toPlainString())
            binding.remarksValueTv.text = data.remark

        }

        // 支付信息
        if (data.status != 1 && data.status != 6) {
            binding.payWayTv.text = data.pay_channel
            binding.paymentNameTv.text = getString(R.string.payment_name_format, data.bank_card_account)
            binding.paymentAccountTv.text = getString(R.string.payment_account_format, data.bank_card_number)
            binding.payContainerLl.visibility = View.VISIBLE
        } else {
            binding.payContainerLl.visibility = View.GONE
        }

        var shopName = ""
        var shopImage = ""

        // 商品信息
        data.shop?.let {
            mAdapter.setNewInstance(it.goods)
            shopName = it.title
            binding.businessNameTv.text = it.title

            if (it.im_status == 1 && !TextUtils.isEmpty(it.mer_uuid)) {
                binding.imLl.visibility = View.VISIBLE
                binding.imLl.setOnClickListener { _ ->
                    BusinessUtils.showImUnavailableDialog(this)
                }
            } else {
                binding.imLl.visibility = View.GONE
            }

            if (it.images.isNotEmpty()) {
                shopImage = it.images[0]
            }

        }

        // 底部
        when (data.status) {
            1 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.visibility = View.VISIBLE
                binding.leftTv.text = getString(R.string.to_cancel_order)
                binding.leftTv.setOnClickListener {
                    // 取消订单
                    cancelOrder(data.id)
                }

                binding.rightTv.visibility = View.VISIBLE
                binding.rightTv.text = getString(R.string.to_pay_2)
                binding.rightTv.setOnClickListener {
                    // 去付款
                    handlePay(data.id, data.price)
                }
            }
            3 -> {
                if (data.delivery_type == 2) {
                    binding.bottomContainerRl.visibility = View.VISIBLE
                    binding.leftTv.visibility = View.VISIBLE
                    binding.leftTv.text = getString(R.string.to_refund)
                    binding.leftTv.setOnClickListener {
                        // 申请退款
                        doRefund()
                    }
                    binding.rightTv.visibility = View.GONE
                } else {
                    binding.bottomContainerRl.visibility = View.GONE
                }
            }
            4 -> {
                if (data.delivery_type == 1) {
                    binding.bottomContainerRl.visibility = View.VISIBLE
                    binding.leftTv.visibility = View.VISIBLE
                    binding.leftTv.text = getString(R.string.to_refund)
                    binding.leftTv.setOnClickListener {
                        // 申请退款
                        doRefund()
                    }
                    binding.rightTv.visibility = View.GONE
                } else {
                    binding.bottomContainerRl.visibility = View.GONE
                }
            }
            5 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE

                binding.leftTv.visibility = View.VISIBLE
                binding.leftTv.text = getString(R.string.apply_return)
                binding.leftTv.setOnClickListener {
                    // 申请退货
                    doRefund()
                }
                val commentStatus = data.comment_status
                if (commentStatus == 3) {
                    binding.rightTv.visibility = View.GONE
                } else {

                    binding.rightTv.visibility = View.VISIBLE

                    if (commentStatus == 1) {
                        binding.rightTv.text = getString(R.string.evaluation)
                        binding.rightTv.setOnClickListener {
                            // 去评价
                            startActivity(StoreCommentPostActivity::class.java, Bundle().apply {
                                putInt(Constant.PARAM_ORDER_ID, orderId)
                                putString(Constant.PARAM_SHOP_NAME, shopName)
                                putString(Constant.PARAM_SHOP_IMAGE, shopImage)
                            })
                        }
                    }

                    if (commentStatus == 2) {
                        binding.rightTv.text = getString(R.string.view_evaluation)
                        binding.rightTv.setOnClickListener {
                            startActivity(StoreCommentDetailActivity::class.java, Bundle().apply {
                                putInt(Constant.PARAM_ORDER_ID, orderId)
                            })
                        }
                    }
                }

            }
            8, 9, 10, 11, 12 -> {
                //  查看售后详情
                if (data.aftersale_status == 2) {
                    binding.bottomContainerRl.visibility = View.VISIBLE
                    binding.leftTv.visibility = View.VISIBLE
                    binding.leftTv.text = getString(R.string.after_sale_details)
                    binding.leftTv.setOnClickListener {
                        // 查看售后详情
                        viewRefundDetail()
                    }
                    binding.rightTv.visibility = View.GONE
                } else {
                    binding.bottomContainerRl.visibility = View.GONE
                }

            }
            else -> {
                binding.bottomContainerRl.visibility = View.GONE
            }
        }
    }

    private fun cancelOrder(orderId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.prompt)
            .setMessage(R.string.cancel_order_confirm)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm) { dialogInterface, i ->
                presenter.cancelStoreOrder(orderId)
                dialogInterface.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .create().show()

    }

    private fun handlePay(orderId: Int, price: BigDecimal) {
        val orderIds = ArrayList<Int>()
        orderIds.add(orderId)
//        startActivityAfterLogin(StorePayActivity::class.java, Bundle().apply {
//            putDouble(Constant.PARAM_TOTAL_PRICE, price.toDouble())
//            putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
//            putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_ESTORE)
//        })
//        WebPayActivity.navigation(this, Constant.PAY_INFO_ESTORE, orderIds)

        PayCounterActivity.navigation(this, PayUrlGet.MALL, orderIds, totalPrice.toString())
    }


    /**
     * 申请售后
     */
    private fun doRefund() {

        startActivity(StoreRefundActivity::class.java, Bundle().apply {
            putParcelableArrayList(Constant.PARAM_DATA, ArrayList(mAdapter.data))
            putInt(Constant.PARAM_ORDER_ID, orderId)
            putDouble(Constant.PARAM_TOTAL_PRICE, totalPrice)
        })

    }


    /**
     * 查看售后详情
     */
    private fun viewRefundDetail() {
        startActivity(StoreRefundDetailActivity::class.java, Bundle().apply {
            putInt(Constant.PARAM_ORDER_ID, orderId)
        })
    }

    override fun onEventReceived(baseEvent: BaseEvent<*>?) {
        super.onEventReceived(baseEvent)
        if (baseEvent == null) {
            return
        }

        when (baseEvent.code) {
            EVENT_REFRESH_ORDER_LIST -> {
                presenter.getOrderDetail(orderId)
            }
            else -> {

            }
        }
    }

    override fun orderCancelSuccess() {
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
        finish()
    }


}
