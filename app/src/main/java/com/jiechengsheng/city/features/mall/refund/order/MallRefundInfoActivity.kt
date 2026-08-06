package com.jiechengsheng.city.features.mall.refund.order

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.JsonElement
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.network.BaseMvpObserver
import com.jiechengsheng.city.api.network.BaseMvpPresenter
import com.jiechengsheng.city.api.network.BaseMvpView
import com.jiechengsheng.city.api.response.mall.MallRefundInfo
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.mall.refund.MallRefundEditActivity
import com.jiechengsheng.city.features.mall.refund.complaint.ComplaintActivity
import com.jiechengsheng.city.features.store.refund.image.RefundImage
import com.jiechengsheng.city.features.store.refund.image.StoreRefundAdapter2
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityMallRefundOrderDetailBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2022/3/25 14:26.
 * 鍟嗗搧鍞悗璇︽儏
 */
class MallRefundInfoActivity : BaseMvpActivity<MallRefundInfoPresenter>(), MallRefundInfoIVniew {

    private lateinit var binding: ActivityMallRefundOrderDetailBinding

    /** 鍟嗗搧璁㈠崟id */
    private var goodOrderId = 0

    /** 鍞悗ID */
    private var refundId = 0

    override fun isStatusDark() = true

    private lateinit var mImageAdapter: StoreRefundAdapter2

    companion object {

        fun navigation(context: Context, goodOrderId: Int, refundId: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ORDER_ID, goodOrderId)
                putInt(Constant.PARAM_REFUND_ID, refundId)
            }
            val intent = Intent(context, MallRefundInfoActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private var alreadyComplaint = false


    /** 澶勭悊鐢宠瘔 */
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            alreadyComplaint = true
            ToastUtils.showShort(R.string.complained_success)
        }
    }


    override fun getLayoutId() = R.layout.activity_mall_refund_order_detail

    override fun initView() {
        binding = ActivityMallRefundOrderDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        initExtra()
        initContent()
    }


    private fun initExtra() {
        intent.extras?.let {
            goodOrderId = it.getInt(Constant.PARAM_ORDER_ID, 0)
            refundId = it.getInt(Constant.PARAM_REFUND_ID, 0)
        }
    }


    private fun initContent() {
        // 鐩稿唽
        mImageAdapter = StoreRefundAdapter2()
        binding.imageRv.apply {
            adapter = mImageAdapter
            layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        }

    }

    override fun initData() {
        presenter = MallRefundInfoPresenter(this)
        presenter.getData(goodOrderId, refundId)
    }

    override fun bindListener() {
        // 鍙栨秷鐢宠
        binding.leftTv.setOnClickListener {

            AlertDialog.Builder(this@MallRefundInfoActivity)
                .setTitle(R.string.prompt)
                .setMessage(R.string.confirm_cancel_application)
                .setCancelable(false)
                .setPositiveButton(R.string.ensure) { dialogInterface, i ->
                    presenter.cancelRefund(refundId)
                    dialogInterface.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
                .create().show()

        }

        // 淇敼鐢宠
        binding.rightTv.setOnClickListener {
            MallRefundEditActivity.navigation(this, goodOrderId, refundId, true)
        }

        // 鐢宠瘔
        binding.complaintTv.setOnClickListener {
            if (alreadyComplaint) {
                ToastUtils.showShort(R.string.complained_success)
            } else {
                val intent = Intent(this, ComplaintActivity::class.java).putExtra(Constant.PARAM_ORDER_ID, goodOrderId)
                searchLauncher.launch(intent)
            }
        }

    }


    override fun bindDetail(response: MallRefundInfo) {
        val goods = response.goods
        val goodStatus = goods.status
        binding.statusTv.text = BusinessUtils.getMallGoodRefundStatusText(goodStatus)
        binding.statusDescTv.text = BusinessUtils.getMallGoodRefundStatusDescText(goodStatus)

        val moneyInfo = response.money_info
        binding.priceTv.text = getString(R.string.price_unit_format, moneyInfo.refund_money)

        // 鐗╂祦
        val addressInfo = response.address_info
        val name = addressInfo.name
        val tel = addressInfo.tel
        val address = addressInfo.address

        // 3~8 鐘舵€佹樉绀烘敹璐у湴鍧€ 2022-08-04
        if (goodStatus >= 3 || goodStatus <= 8) {
            if (name.isNotBlank() || tel.isNotBlank() || address.isNotBlank()) {
                binding.logisticsContainerLl.visibility = View.VISIBLE

                binding.logisticsInfoTv.text = StringBuilder()
                    .append(getString(R.string.contact_name_format, name)).append("\r\n")
                    .append(getString(R.string.contact_number_format, tel)).append("\r\n")
                    .append(getString(R.string.contact_address_format, address))
            } else {
                binding.logisticsContainerLl.visibility = View.GONE
            }

        } else {
            binding.logisticsContainerLl.visibility = View.GONE
        }


        // 鍞悗鍟嗗搧
        GlideUtil.load(this, goods.good_image, binding.dishesItem.orderImageIv, 4)
        binding.dishesItem.goodNameTv.text = goods.good_title
        binding.dishesItem.countTv.text = getString(R.string.count_format, goods.good_num)
        binding.dishesItem.nowPriceTv.text = getString(R.string.price_unit_format, goods.good_price)

        // 鍞悗淇℃伅
        response.money_info.let {
            binding.amountTv.text = getString(R.string.price_unit_format, it.refund_money)
            binding.goodPriceTv.text = getString(R.string.price_unit_format, it.goods_total)
            binding.shopOffersTv.text = getString(R.string.price_unit_format, it.shop_coupon_money)
            binding.platformOffersTv.text = getString(R.string.price_unit_format, it.order_coupon_money)
            binding.refundTimeTv.text = it.refund_time
            binding.refundNumberTv.text = it.serial_number
        }

        // 鍞悗鍘熷洜
        binding.descTv.text = response.cancel_reason
        val photos = ArrayList<RefundImage>()
        response.cancel_images.forEach {
            val apply = RefundImage().apply {
                type = RefundImage.TYPE_SHOW
                imageSource = it
            }
            photos.add(apply)

        }
        mImageAdapter.setNewInstance(photos)

        // 澶勭悊搴曢儴鎸夐挳
        when (goodStatus) {
            2 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.visibility = View.VISIBLE
                binding.rightTv.visibility = View.GONE
            }
            4 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.visibility = View.VISIBLE
                binding.rightTv.visibility = View.VISIBLE
            }
            8 -> {
                binding.bottomContainerRl.visibility = View.VISIBLE
                binding.leftTv.visibility = View.GONE
                binding.rightTv.visibility = View.VISIBLE
            }
            else -> {
                binding.bottomContainerRl.visibility = View.GONE
            }
        }

        binding.bottomV.visibility = binding.bottomContainerRl.visibility

        // 鎶曡瘔
        if (goodStatus == 4 && response.complaint == 0) {
            binding.complaintTv.visibility = View.VISIBLE
        } else {
            binding.complaintTv.visibility = View.GONE
        }

        // 閫€娆炬柟寮?
        val refundMethod = response.remit_info
        refundMethod?.let {
            //
            binding.refundNameTv.text = refundMethod.name
            binding.refundUserNameTv.text = refundMethod.user_name
            binding.refundAccountTv.text = refundMethod.account
            binding.refundMethodLl.visibility = View.VISIBLE
        }

        // 閫€娆惧け璐?
        if (goodStatus == 8) {
            binding.failReasonRl.visibility = View.VISIBLE
            binding.reasonSplitV.visibility = View.VISIBLE
            binding.failReasonTv.text = response.error_reason
        } else {
            binding.reasonSplitV.visibility = View.GONE
        }


    }

    override fun cancelSuccess() {
        // 鍒锋柊璁㈠崟璇︽儏浠ュ強璁㈠崟鍒楄〃
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
        finish()
    }


    override fun onEventReceived(baseEvent: BaseEvent<*>?) {
        super.onEventReceived(baseEvent)
        if (baseEvent == null) {
            return
        }

        when (baseEvent.code) {
            EventCode.EVENT_REFRESH_ORDER_LIST -> {
                // 瀹屾垚淇敼銆佹彁浜ゅ敭鍚庯紝鍒锋柊椤甸潰
                presenter.getData(goodOrderId, refundId)
            }
            else -> {

            }
        }
    }


}


interface MallRefundInfoIVniew : BaseMvpView {
    fun bindDetail(response: MallRefundInfo)
    fun cancelSuccess()

}


class MallRefundInfoPresenter(private var view: MallRefundInfoIVniew) : BaseMvpPresenter(view) {


    fun getData(orderId: Int, refundId: Int) {
        requestApi(mRetrofit.mallRefundOrderDetail(orderId, refundId), object : BaseMvpObserver<MallRefundInfo>(view) {
            override fun onSuccess(response: MallRefundInfo) {
                view.bindDetail(response)
            }

        })
    }


    fun cancelRefund(refundId: Int) {
        requestApi(mRetrofit.cancelRefund(refundId), object : BaseMvpObserver<JsonElement>(view) {
            override fun onSuccess(response: JsonElement?) {
                view.cancelSuccess()
            }

        })

    }

}