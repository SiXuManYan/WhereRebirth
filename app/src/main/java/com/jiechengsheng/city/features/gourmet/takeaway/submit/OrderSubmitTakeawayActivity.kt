package com.jiechengsheng.city.features.gourmet.takeaway.submit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.address.AddressResponse
import com.jiechengsheng.city.api.response.gourmet.dish.DeliveryTimeRetouch
import com.jiechengsheng.city.api.response.gourmet.dish.DishResponse
import com.jiechengsheng.city.api.response.gourmet.order.TakeawayOrderSubmitData
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.bean.OrderSubmitChildRequest
import com.jiechengsheng.city.bean.OrderSubmitTakeawayRequest
import com.jiechengsheng.city.features.address.AddressActivity
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.time.TimeUtil
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import com.jiechengsheng.city.databinding.ActivityOrderSubmitTakeawayBinding
/**
 * Created by Wangsw  2021/4/26 17:54.
 * 澶栧崠璁㈠崟鎻愪氦椤?
 */
class OrderSubmitTakeawayActivity : BaseMvpActivity<OrderSubmitTakeawayPresenter>(), OrderSubmitTakeawayView {

    private lateinit var binding: ActivityOrderSubmitTakeawayBinding


    /** 椁愬巺id */
    private var mRestaurantId: String = "0"

    /** 鍖呰璐?*/
    private var packing_charges: String = "0"

    /** 閰嶉€佽垂 */
    private var delivery_cost: String = "0"

    /** 鎬婚噾棰?*/
    private var totalPrice: String = "0"

    /** 鍟嗗鍚嶇О */
    private var restaurant_name: String = ""

    /** 鑿滃搧鍒楄〃 */
    private lateinit var dish_list: ArrayList<DishResponse>

    var timeDialog: BottomSheetDialog? = null

    /** 鏀惰揣鍦板潃 */
    var mSelectAddressData: AddressResponse? = null

    /** 閰嶉€佹椂闂寸被鍨嬶紙1锛氱珛鍗抽厤閫侊紝2锛氶€夊畾鏃堕棿锛?*/
    private var mDeliveryTimeType = 0

    /** 閰嶉€佹椂闂?*/
    private var mDeliveryTime = ""

    private lateinit var emptyView: EmptyView

    private lateinit var mAdapter: OrderSubmitTakeawayAdapter

    private lateinit var mTimeAdapter: TimeAdapter

    override fun getLayoutId() = R.layout.activity_order_submit_takeaway

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityOrderSubmitTakeawayBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        BarUtils.setStatusBarColor(this, Color.WHITE)
        // extra
        val bundle = intent.extras ?: return
        mRestaurantId = bundle.getString(Constant.PARAM_ID, "0")
        packing_charges = bundle.getString(Constant.PARAM_PACKING_CHARGES, "0")
        delivery_cost = bundle.getString(Constant.PARAM_DELIVERY_COST, "0")
        totalPrice = bundle.getString(Constant.PARAM_TOTAL_PRICE, "0")
        restaurant_name = bundle.getString(Constant.PARAM_NAME, "0")
        dish_list = bundle.getSerializable(Constant.PARAM_DATA) as ArrayList<DishResponse>

        // list
        initRecyclerView()
        binding.businessNameTv.text = restaurant_name
        binding.packingChargesTv.text = getString(R.string.price_unit_format, packing_charges)
        binding.deliveryCostTv.text = getString(R.string.price_unit_format, delivery_cost)
        val total = getString(R.string.price_unit_format, totalPrice)
        binding.actualPaymentTv.text = total
        binding.totalPriceTv.text = total

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {

        emptyView = EmptyView(this).apply {
            showEmptyDefault()
        }

        mAdapter = OrderSubmitTakeawayAdapter().apply {
            setEmptyView(emptyView)
        }

        binding.dishRv.apply {
            isNestedScrollingEnabled = true
            layoutManager = object : LinearLayoutManager(this@OrderSubmitTakeawayActivity, VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            addItemDecoration(
                DividerDecoration(
                    Color.WHITE, SizeUtils.dp2px(10f),
                    0, 0
                )
            )
            adapter = mAdapter
        }


        // 閫佽揪鏃堕棿
        mTimeAdapter = TimeAdapter().apply {
            setOnItemClickListener { _, _, position ->
                val timeData = mTimeAdapter.data[position]
                mDeliveryTimeType = if (position == 0) {
                    1
                } else {
                    2
                }
                mDeliveryTime = timeData.time
                binding.deliveryTimeTv.text = getString(R.string.delivery_time_format2, TimeUtil.getFormatTimeHM(timeData.time))


                mTimeAdapter.data.forEachIndexed { index, deliveryTimeRetouch ->
                    deliveryTimeRetouch.nativeSelected = index == position
                }
                mTimeAdapter.notifyDataSetChanged()

                timeDialog?.dismiss()
            }

        }

    }

    override fun initData() {
        if (dish_list.isEmpty()) {
            emptyView.showEmptyContainer()
        }
        mAdapter.setNewInstance(dish_list)
        presenter = OrderSubmitTakeawayPresenter(this)
        presenter.getTimeList(mRestaurantId)

    }

    override fun bindListener() {
        binding.selectAddressRl.setOnClickListener {
            showAddress()
        }
        binding.timeTv.setOnClickListener {
            showTime()
        }

        binding.buyAfterTv.setOnClickListener {
            if (mSelectAddressData == null) {
                ToastUtils.showShort(R.string.choose_shipping_address)
                return@setOnClickListener
            }
            if (mDeliveryTime.isEmpty()) {
                ToastUtils.showShort(getString(R.string.please_select_delivery_time))
                return@setOnClickListener
            }

            val goodIds = ArrayList<OrderSubmitChildRequest>()

            mAdapter.data.forEach {
                val apply = OrderSubmitChildRequest().apply {
                    good_id = it.id
                    good_num = it.nativeSelectCount
                }
                goodIds.add(apply)
            }

            val apply = OrderSubmitTakeawayRequest().apply {
                goods = Gson().toJson(goodIds)
                address_id = mSelectAddressData!!.id
                restaurant_id = mRestaurantId
                delivery_time_type = mDeliveryTimeType
                delivery_time = mDeliveryTime
                remark = binding.remarksEt.text.toString().trim()
            }

            presenter.submitOrder(apply)


        }

    }


    /** 澶勭悊閫夋嫨鍦板潃 */
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras ?: return@registerForActivityResult

            val selectData = bundle.getSerializable(Constant.PARAM_DATA) as AddressResponse
            mSelectAddressData = selectData
            binding.selectAddressTv.text = selectData.address
            binding.addressRl.visibility = ViewGroup.VISIBLE
            val sex = if (selectData.sex == 1) {
                getString(R.string.sir)
            } else {
                getString(R.string.lady)
            }
            binding.addressNameTv.text = getString(R.string.recipient_format, selectData.contact_name, sex, selectData.contact_number)
            binding.phoneTv.text = selectData.contact_number
        }
    }


    private fun showAddress() {
        searchLauncher.launch(Intent(this, AddressActivity::class.java).putExtra(Constant.PARAM_HANDLE_SELECT, true))
    }

    override fun bindTime(otherTimes: MutableList<DeliveryTimeRetouch>) {
        mTimeAdapter.setNewInstance(otherTimes)
    }


    private fun showTime() {
        val timeDialog = BottomSheetDialog(this)
        this.timeDialog = timeDialog
        val view = LayoutInflater.from(this).inflate(R.layout.layout_select_time, null)
        timeDialog.setContentView(view)
        try {
            val parent = view.parent as ViewGroup
            parent.setBackgroundResource(android.R.color.transparent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val rv = view.findViewById<RecyclerView>(R.id.recycler_view)
        rv.apply {
            adapter = mTimeAdapter
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.grey_F5F5F5), 1, 0, 0))
        }

        view.findViewById<ImageView>(R.id.close_iv).setOnClickListener {
            timeDialog.dismiss()
        }
        timeDialog.show()
    }

    override fun submitSuccess(response: TakeawayOrderSubmitData) {

        val orderIds = ArrayList<Int>()
        orderIds.add(response.order.id)

/*        startActivityAfterLogin(StorePayActivity::class.java, Bundle().apply {
            putDouble(Constant.PARAM_TOTAL_PRICE, total_price.toDouble())
            putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
            putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_TAKEAWAY)
        })*/

//        WebPayActivity.navigation(this, Constant.PAY_INFO_TAKEAWAY, orderIds)
        PayCounterActivity.navigation(this, PayUrlGet.TAKEAWAY, orderIds, totalPrice)
        finish()
    }

    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        when (baseEvent.code) {
            EventCode.EVENT_CANCEL_PAY -> finish()
        }
    }


}