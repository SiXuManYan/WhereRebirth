package com.jiechengsheng.city.features.gourmet.refund

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.RefundMethod
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.refund.method.RefundMethodActivity
import com.jiechengsheng.city.utils.Constant
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityRefundComplexBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2022/5/10 15:44.
 * 美食、外卖、酒店、账单申请退款
 */
class ComplexRefundActivity : BaseMvpActivity<ComplexRefundPresenter>(), ComplexRefundView {

    private lateinit var binding: ActivityRefundComplexBinding


    /** 商品订单id */
    private var orderId = 0

    /** 打款id */
    private var remitId = 0

    /** 0美食 1外卖 2缴费账单*/
    private var type = 0


    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_refund_complex

    companion object {

        fun navigation(
            context: Context,
            orderId: Int,
            refundPrice: String,
            totalPrice: String,
            useType: Int = ComplexRefundPresenter.TYPE_FOOD,
        ) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ORDER_ID, orderId)
                putString(Constant.PARAM_REFUND_PRICE, refundPrice)
                putString(Constant.PARAM_TOTAL_PRICE, totalPrice)
                putInt(Constant.PARAM_TYPE, useType)
            }
            val intent = Intent(context, ComplexRefundActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val bundle = it.data?.extras ?: return@registerForActivityResult
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                val refundMethod = bundle.getParcelable<RefundMethod>(Constant.PARAM_REFUND_METHOD)!!
                loadRefundMethod(refundMethod)
            }
        }

    }


    override fun initView() {
        binding = ActivityRefundComplexBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
    }

    private fun initExtra() {
        intent.extras?.let {
            orderId = it.getInt(Constant.PARAM_ORDER_ID, 0)
            type = it.getInt(Constant.PARAM_TYPE, 0)

            val refundPrice = it.getString(Constant.PARAM_REFUND_PRICE, "")
            val totalPrice = it.getString(Constant.PARAM_TOTAL_PRICE, "")

            binding.refundPriceTv.text = StringUtils.getString(R.string.price_unit_format, refundPrice)
            binding.totalPriceTv.text = StringUtils.getString(R.string.price_unit_format, totalPrice)

        }

        if (type == ComplexRefundPresenter.TYPE_TAKEAWAY) {
            binding.refundMethodLl.visibility = View.VISIBLE
        } else {
            binding.refundMethodLl.visibility = View.GONE
        }
    }

    override fun initData() {
        presenter = ComplexRefundPresenter(this)
    }

    override fun bindListener() {

        binding.refundMethodTv.setOnClickListener {
            launcher.launch(Intent(this, RefundMethodActivity::class.java)
                .putExtra(Constant.PARAM_HANDLE_SELECT, true))
        }
        binding.confirmTv.setOnClickListener {
            if (remitId == 0 && type == ComplexRefundPresenter.TYPE_TAKEAWAY) {
                ToastUtils.showShort(R.string.please_add_refund_methods)
                return@setOnClickListener
            }
            presenter.refundOrder(orderId, remitId, type)
        }
    }


    private fun loadRefundMethod(refundMethod: RefundMethod) {
        remitId = refundMethod.id

        binding.refundNameTv.text = refundMethod.name
        binding.refundUserNameTv.text = refundMethod.user_name
        binding.refundAccountTv.text = refundMethod.account
        binding.refundMethodInfoLl.visibility = View.VISIBLE
    }

    override fun refundSuccess() {
        ToastUtils.showShort(getString(R.string.refund_commit_toast))
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
        finish()
    }
}
