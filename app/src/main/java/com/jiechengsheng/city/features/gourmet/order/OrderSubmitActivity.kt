package com.jiechengsheng.city.features.gourmet.order

import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BarUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.gourmet.cart.ShoppingCartResponse
import com.jiechengsheng.city.api.response.gourmet.order.FoodOrderSubmitData
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.gourmet.cart.ShoppingCartAdapter
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.databinding.ActivityOrderSubmitBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/4/20 16:54.
 * 美食提交订单
 */
class OrderSubmitActivity : BaseMvpActivity<OrderSubmitPresenter>(), OrderSubmitView {

    private lateinit var binding: ActivityOrderSubmitBinding


    private lateinit var mAdapter: ShoppingCartAdapter
    private lateinit var mTotalPrice: String

    override fun getLayoutId() = R.layout.activity_order_submit

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityOrderSubmitBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)

        initRecycler()

    }


    private fun initRecycler() {
        mAdapter = ShoppingCartAdapter()

        binding.recyclerView.apply {
            adapter = mAdapter
            isNestedScrollingEnabled = true
            layoutManager = object : LinearLayoutManager(this@OrderSubmitActivity, LinearLayoutManager.VERTICAL, false) {
                override fun canScrollVertically() = false
            }
        }
    }

    override fun initData() {
        presenter = OrderSubmitPresenter(this)
        val data = intent.getSerializableExtra(Constant.PARAM_DATA) as ArrayList<ShoppingCartResponse>
        mTotalPrice = intent.getStringExtra(Constant.PARAM_TOTAL_PRICE)!!
        if (data == null || data.isEmpty()) {
            return
        }

        val list = presenter.convertList(data)
        mAdapter.setNewInstance(list)
        binding.totalPriceTv.text = getString(R.string.price_unit_format, mTotalPrice)
    }

    override fun bindListener() {
        binding.buyAfterTv.setOnClickListener {

            if (mAdapter.data.isEmpty()) {
                return@setOnClickListener
            }
            val phone = binding.phoneEt.text.toString().trim()

            presenter.submitOrder(mAdapter.data, phone)
        }
    }

    override fun summitSuccess(response: FoodOrderSubmitData) {

        val orderIds = ArrayList<Int>()
        response.orders.forEach {
            orderIds.add(it.id)
        }

//        startActivityAfterLogin(StorePayActivity::class.java, Bundle().apply {
//            putDouble(Constant.PARAM_TOTAL_PRICE, mTotalPrice.toDouble())
//            putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
//            putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_FOOD)
//        })
//        WebPayActivity.navigation(this, Constant.PAY_INFO_FOOD, orderIds)
        PayCounterActivity.navigation(this, PayUrlGet.RESTAURANT, orderIds, mTotalPrice)
        finish()

        finish()
    }

    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        if (baseEvent.code == EventCode.EVENT_CANCEL_PAY) {
            finish()
        }
    }


}