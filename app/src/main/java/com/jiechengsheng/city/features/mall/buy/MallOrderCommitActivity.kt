package com.jiechengsheng.city.features.mall.buy


import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.payment.PayUrlGet
import com.jiechengsheng.city.api.response.address.AddressResponse
import com.jiechengsheng.city.api.response.mall.MallCartGroup
import com.jiechengsheng.city.api.response.mall.request.MallCommitResponse
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.address.AddressActivity
import com.jiechengsheng.city.features.mall.buy.coupon.OrderCouponHomeFragment
import com.jiechengsheng.city.features.payment.counter.PayCounterActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.widget.list.DividerDecoration
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityMallOrderCommitBinding

/**
 * Created by Wangsw  2021/12/15 13:56.
 * 鍟嗗煄涓嬪崟
 */
class MallOrderCommitActivity : BaseMvpActivity<MallOrderCommitPresenter>(), MallOrderCommitView {

    private lateinit var binding: ActivityMallOrderCommitBinding


    private var totalPrice: BigDecimal = BigDecimal.ZERO

    private var data: ArrayList<MallCartGroup> = ArrayList()

    /** 鏀惰揣鍦板潃 */
    var mSelectAddressData: AddressResponse? = null

    /** 鎬婚厤閫佽垂 */
    var mTotalServiceDeliveryFee: BigDecimal? = BigDecimal.ZERO

    /** 骞冲彴浼樻儬鍒告€婚噾棰?*/
    private var mPlatformCouponTotalMoney: BigDecimal = BigDecimal.ZERO

    /** 搴楅摵浼樻儬鍒告€婚噾棰?*/
    private var mShopCouponTotalMoney: BigDecimal = BigDecimal.ZERO

    /**
     * 褰撳墠骞冲彴浼樻儬鍒竔d
     * 鍖呭惈浠ヤ笅鏉ユ簮
     *      1.杩涘叆椤甸潰锛岃姹傚埌榛樿浼樻儬鍒?
     *      2.浠庡綋鍓嶉〉寮圭獥閫夋嫨浼樻儬鍒?
     *  鍊硷細
     *  0锛氭湭閫夋嫨鎴栨病鏈夊尮閰嶅埌浼樻儬鍒?
     */
    private var currentPlatformCouponId = 0

    /** 褰撳墠杩涜鏇存敼搴楅摵鍒哥殑 搴楅摵id */
    private var currentHandleShopId = 0


    private lateinit var mAdapter: MallOrderCommitAdapter

    private lateinit var mSelectedCouponDialog: OrderCouponHomeFragment

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_mall_order_commit

    override fun initView() {
        binding = ActivityMallOrderCommitBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        val bundle = intent.extras
        bundle?.let {
            data = it.getSerializable(Constant.PARAM_DATA) as ArrayList<MallCartGroup>
        }
        initContent()
    }

    private fun initContent() {
        mAdapter = MallOrderCommitAdapter().apply {
            addChildClickViewIds(R.id.select_shop_coupon_rl)
            setOnItemChildClickListener { adapter, view, position ->

                val mallCartGroup = mAdapter.data[position]

                currentHandleShopId = mallCartGroup.shop_id

                if (view.id == R.id.select_shop_coupon_rl) {

                    // 閫夋嫨搴楅摵浼樻儬鍒?闇€瑕侀澶栦紶 shop_goods shop_id
                    mSelectedCouponDialog.apply {

                        alreadySelectedCouponId = mallCartGroup.nativeShopCouponId
                        goodsJsonStr = null

                        shopId = mallCartGroup.shop_id
                        shopGoodsJson =
                            this@MallOrderCommitActivity.presenter.getShopGoodsJsonString(
                                this@MallOrderCommitActivity.data,
                                mallCartGroup.shop_id)

                    }

                    mSelectedCouponDialog.show(supportFragmentManager, mSelectedCouponDialog.tag)

                }


            }

        }
        binding.contentRv.apply {
            adapter = mAdapter
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.white), SizeUtils.dp2px(10f), 0, 0))
        }
        mAdapter.setNewInstance(data)


    }

    /** 澶勭悊閫夋嫨鍦板潃 */
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras ?: return@registerForActivityResult

            val selectData = bundle.getSerializable(Constant.PARAM_DATA) as AddressResponse
            mSelectAddressData = selectData
            binding.selectAddressTv.text = selectData.address
            binding.addressValueLl.visibility = ViewGroup.VISIBLE
            val sex = if (selectData.sex == 1) {
                getString(R.string.sir)
            } else {
                getString(R.string.lady)
            }
            binding.addressNameTv.text = getString(R.string.recipient_format, selectData.contact_name, sex, selectData.contact_number)
            binding.phoneTv.text = selectData.contact_number

            presenter.getDelivery(mAdapter, selectData.city_id)

        }
    }


    override fun initData() {
        presenter = MallOrderCommitPresenter(this)
        handleTotalPrice()
        presenter.getDefaultCoupon(mAdapter, data, isFirstRequest = true)
        mSelectedCouponDialog = OrderCouponHomeFragment()

    }

    private fun handleTotalPrice() {

        // 鏈€缁堟敮浠樹环鏍?
        totalPrice = presenter.handlePrice(mAdapter, mTotalServiceDeliveryFee, mPlatformCouponTotalMoney, mShopCouponTotalMoney)


        // 鏀粯浠锋牸
        binding.totalPriceTv.text = getString(R.string.price_unit_format, totalPrice.toPlainString())

        // ## 浠锋牸鏄庣粏 ##
        // 鍟嗗搧浠锋牸
        val oldPrice = presenter.getAllGoodPrice(mAdapter)
        binding.totalPriceOldTv.text = getString(R.string.price_unit_format, oldPrice.toPlainString())

        // 鎬昏繍璐?
        val totalDeliveryFee = presenter.getTotalDeliveryFee(mAdapter)
        binding.totalDeliveryFeeTv.text = getString(R.string.price_unit_format, totalDeliveryFee.toPlainString())

        // 鍟嗗浼樻儬鍒搁噾棰?
        binding.shopOffersTv.text = getString(R.string.price_unit_format, mShopCouponTotalMoney.toPlainString())

        // 骞冲彴浼樻儬鍒搁噾棰?
        if (mPlatformCouponTotalMoney == BigDecimal.ZERO) {
            binding.couponTv.text = ""
        } else {
            binding.couponTv.text = getString(R.string.price_unit_format, mPlatformCouponTotalMoney.toPlainString())
        }
        // 鍚堣
        binding.totalPriceCopyTv.text = getString(R.string.price_unit_format, totalPrice.toPlainString())
    }

    override fun bindListener() {
        binding.addressLl.setOnClickListener {
            searchLauncher.launch(Intent(this, AddressActivity::class.java)
                .putExtra(Constant.PARAM_HANDLE_SELECT, true))
        }
        binding.submitTv.setOnClickListener {
            if (mSelectAddressData == null) {
                ToastUtils.showShort(R.string.address_edit_hint)
                return@setOnClickListener
            }
            presenter.orderCommit(data, mSelectAddressData?.id, currentPlatformCouponId)
        }
        binding.selectedCoupon.setOnClickListener {
            mSelectedCouponDialog.apply {
                shopGoodsJson = null
                shopId = null

                goodsJsonStr = this@MallOrderCommitActivity.presenter.getGoodsJsonString(data)
                alreadySelectedCouponId = currentPlatformCouponId
            }
            mSelectedCouponDialog.show(supportFragmentManager, mSelectedCouponDialog.tag)
            binding.selectedCoupon.isClickable = false
            Handler(mainLooper).postDelayed({
                binding.selectedCoupon.isClickable = true
            }, 1000)
        }
    }

    override fun commitSuccess(response: MallCommitResponse) {

/*        startActivityAfterLogin(StorePayActivity::class.java, Bundle().apply {
            putDouble(Constant.PARAM_TOTAL_PRICE, response.total_price.toDouble())
            putIntegerArrayList(Constant.PARAM_ORDER_IDS, response.orders)
            putInt(Constant.PARAM_TYPE, Constant.PAY_INFO_MALL)
        })*/

//        WebPayActivity.navigation(this, Constant.PAY_INFO_MALL, response.orders)
        PayCounterActivity.navigation(this, PayUrlGet.MALL, response.orders, totalPrice.toPlainString())

    }


    override fun bindTotalDelivery(totalServiceDeliveryFee: BigDecimal?) {
        mTotalServiceDeliveryFee = totalServiceDeliveryFee
        handleTotalPrice()

    }

    override fun bindDefaultCoupon(platformCouponId: Int, platformCouponTotalMoney: BigDecimal, shopCouponTotalMoney: BigDecimal) {
        currentPlatformCouponId = platformCouponId
        mPlatformCouponTotalMoney = platformCouponTotalMoney
        mShopCouponTotalMoney = shopCouponTotalMoney

        handleTotalPrice()
    }


    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        when (baseEvent.code) {
            EventCode.EVENT_SELECTED_PLATFORM_COUPON -> {
                val selectedPlatformCouponId = baseEvent.data as Int
                currentPlatformCouponId = selectedPlatformCouponId
                presenter.getDefaultCoupon(mAdapter, data, currentPlatformCouponId)
            }
            EventCode.EVENT_SELECTED_SHOP_COUPON -> {
                val selectedShopCouponId = baseEvent.data as Int
                // 鏇存崲搴楅摵浼樻儬鍒稿悗锛屽皢骞冲彴鍒哥疆绌?
                currentPlatformCouponId = 0
                // 鏇存柊搴楅摵item涓殑 nativeShopCouponId
                presenter.updateItemShopCouponId(mAdapter, currentHandleShopId, selectedShopCouponId)
                // 鑾峰彇榛樿浼樻儬鍒?
                presenter.getDefaultCoupon(mAdapter, data, currentPlatformCouponId)
            }

        }
    }

}