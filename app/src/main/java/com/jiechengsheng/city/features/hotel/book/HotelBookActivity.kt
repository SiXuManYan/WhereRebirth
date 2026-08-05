package com.jiechengsheng.city.features.hotel.book

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.HotelOrderRequest
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.hotel.HotelOrderCommitResponse
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.BigDecimalUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.widget.NumberView2
import com.jiechengsheng.city.widget.calendar.JcsCalendarAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityHotelBoolkBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/10/13 15:17.
 *  酒店预订
 */
class HotelBookActivity : BaseMvpActivity<HotelBookPresenter>(), HotelBookView, NumberView2.OnValueChangeListener {

    private lateinit var binding: ActivityHotelBoolkBinding

    private var hotelRoomId = 0
    private var singlePrice = 0.0
    private var totalPrice: BigDecimal = BigDecimal.ZERO

    /** 房间数量 */
    private var roomNumber = 1
    private var roomImage = ""

    private var roomType = ""
    private var breakFastType = ""
    private var roomArea = ""
    private var roomPeople = ""
    private var hotelName = ""
    private var cancelable = ""

    /**
     * 国家码
     * 默认 菲律宾+63前缀
     */
    private var mCountryPrefix = StringUtils.getStringArray(R.array.country_prefix)[0]


    private lateinit var mStartDateBean: JcsCalendarAdapter.CalendarBean
    private lateinit var mEndDateBean: JcsCalendarAdapter.CalendarBean

    override fun isStatusDark() = true

    companion object {

        fun navigation(
            context: Context,
            hotelRoomId: Int,
            singlePrice: Double,
            roomType: String,
            breakFastType: String,
            roomArea: String,
            roomPeople: String,
            hotelName: String,
            cancelable: String,
            roomNumber: Int? = 1,
            roomImage: String,
            startDate: JcsCalendarAdapter.CalendarBean,
            endDate: JcsCalendarAdapter.CalendarBean,
        ) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ROOM_ID, hotelRoomId)
                putDouble(Constant.PARAM_PRICE, singlePrice)
                putString(Constant.PARAM_ROOM_TYPE, roomType)
                putString(Constant.PARAM_BREAKFAST, breakFastType)
                putString(Constant.PARAM_ROOM_AREA, roomArea)
                putString(Constant.PARAM_ROOM_PEOPLE, roomPeople)
                putString(Constant.PARAM_NAME, hotelName)
                putString(Constant.PARAM_CANCELABLE, cancelable)
                putInt(Constant.PARAM_ROOM_NUMBER, roomNumber!!)
                putString(Constant.PARAM_ROOM_IMAGE, roomImage)

                putSerializable(Constant.PARAM_START_DATE, startDate)
                putSerializable(Constant.PARAM_END_DATE, endDate)
            }

            val intent = if (User.isLogon()) {
                Intent(context, HotelBookActivity::class.java).putExtras(bundle)
            } else {
                Intent(context, LoginActivity::class.java)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }


            context.startActivity(intent)
        }
    }

    override fun getLayoutId() = R.layout.activity_hotel_boolk

    override fun initView() {
        binding = ActivityHotelBoolkBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
        binding.numberView.apply {
            alwaysEnableCut = true
            MIN_GOOD_NUM = 1
            MAX_GOOD_NUM = 20
            cut_iv.setImageResource(R.mipmap.ic_cut_black_transparent)
            add_iv.setImageResource(R.mipmap.ic_add_black)
            cutResIdCommon = R.mipmap.ic_cut_black
            cutResIdMin = R.mipmap.ic_cut_black_transparent
            addResIdCommon = R.mipmap.ic_add_black
            addResIdMax = R.mipmap.ic_add_black_transparent
            updateNumberJudgeMin(roomNumber)
            cut_iv.visibility = View.VISIBLE
            valueChangeListener = this@HotelBookActivity
        }
        binding.countryTv.text = mCountryPrefix
    }


    private fun initExtra() {
        val bundle = intent.extras ?: return
        bundle.apply {
            hotelRoomId = getInt(Constant.PARAM_ROOM_ID)
            singlePrice = getDouble(Constant.PARAM_PRICE)

            mStartDateBean = getSerializable(Constant.PARAM_START_DATE) as JcsCalendarAdapter.CalendarBean
            mEndDateBean = getSerializable(Constant.PARAM_END_DATE) as JcsCalendarAdapter.CalendarBean

            roomType = getString(Constant.PARAM_ROOM_TYPE, "")
            breakFastType = getString(Constant.PARAM_BREAKFAST, "")
            roomArea = getString(Constant.PARAM_ROOM_AREA, "")
            roomPeople = getString(Constant.PARAM_ROOM_PEOPLE, "")
            hotelName = getString(Constant.PARAM_NAME, "")
            cancelable = getString(Constant.PARAM_CANCELABLE, "")
            roomNumber = getInt(Constant.PARAM_ROOM_NUMBER, 1)
            roomImage = getString(Constant.PARAM_ROOM_IMAGE, "")
            binding.nameTv.text = hotelName
            binding.cancelTv.text = cancelable
        }

        handlePrice()


    }

    private fun handlePrice() {
        totalPrice = BigDecimalUtil.mul(singlePrice, roomNumber.toDouble())
        binding.priceTv.text = getString(R.string.price_unit_format, totalPrice.toPlainString())
    }


    override fun initData() {
        presenter = HotelBookPresenter(this)
        initDefault()
    }

    private fun initDefault() {

        "·$roomType".also { binding.typeTv.text = it }
        "·$breakFastType | $roomArea | $roomPeople".also { binding.contentTv.text = it }
        "·$cancelable".also { binding.cancelTv.text = it }

        binding.startDateTv.text = mStartDateBean.showMonthDayDate
        binding.endDateTv.text = mEndDateBean.showMonthDayDate

        GlideUtil.load(this, roomImage, binding.hotelImageIv, 5)

        val span = (mEndDateBean.time - mStartDateBean.time) / (1000 * 60 * 60 * 24)
        binding.totalDateTv.text = getString(R.string.total_date_format, span.toString())
    }

    override fun onNumberChange(goodNum: Int, isAdd: Boolean) {
        roomNumber = goodNum
        handlePrice()

    }

    override fun bindListener() {
        binding.bookingTv.setOnClickListener {
            val resident = binding.residentEt.text.toString().trim()

            if (resident.isEmpty()) {
                ToastUtils.showShort(R.string.please_enter)
                return@setOnClickListener
            }
            val userPhone = binding.phoneAet.text.toString().trim()
            if (userPhone.isEmpty()) {
                ToastUtils.showShort(R.string.please_enter)
                return@setOnClickListener
            }
            val apply = HotelOrderRequest().apply {
                hotel_room_id = hotelRoomId
                username = resident
                phone = userPhone
                start_date = mStartDateBean.showYearMonthDayDateWithSplit

                end_date = mEndDateBean.showYearMonthDayDateWithSplit
                room_num = roomNumber.toString()
                country_code = mCountryPrefix
                price = totalPrice.toPlainString()
            }

            presenter.commitOrder(apply)
        }

        binding.countryTv.setOnClickListener {
            FeaturesUtil.getCountryPrefix(this) { countryCode: String ->
                mCountryPrefix = countryCode
                binding.countryTv.text = getString(R.string.country_code_format, countryCode)
            }
        }
    }

    override fun commitSuccess(response: HotelOrderCommitResponse) {

        val orderIds = ArrayList<Int>()
        val order = response.order
        orderIds.add(order!!.id)
/*        val bundle = Bundle()
        bundle.putDouble(Constant.PARAM_TOTAL_PRICE, response.total_price.toDouble())
        bundle.putIntegerArrayList(Constant.PARAM_ORDER_IDS, orderIds)
        bundle.putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_HOTEL)
        startActivityAfterLogin(StorePayActivity::class.java, bundle)*/

//        WebPayActivity.navigation(this, Constant.PAY_INFO_HOTEL, orderIds)

        PayCounterActivity.navigation(this, PayUrlGet.HOTEL, orderIds, totalPrice.toPlainString())

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        if (baseEvent.code == EventCode.EVENT_CANCEL_PAY) {
            finish()
        }
    }


}