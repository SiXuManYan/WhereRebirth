package com.jiechengsheng.city.features.gourmet.takeaway

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.gourmet.takeaway.TakeawayDetailResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.gourmet.takeaway.submit.OrderSubmitTakeawayActivity
import com.jiechengsheng.city.utils.BigDecimalUtil
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityTakeAway2Binding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/4/25 10:04.
 * 澶栧崠璇︽儏
 */
class TakeawayActivity : BaseMvpActivity<TakeawayPresenter>(), TakeawayView, TakeawayAdapter.OnSelectCountChange {

    private lateinit var binding: ActivityTakeAway2Binding


    /** 椁愬巺id */
    private lateinit var restaurant_id: String

    /** 鍖呰璐?*/
    private var packing_charges: BigDecimal = BigDecimal.ZERO

    /** 閰嶉€佽垂 */
    private var delivery_cost: BigDecimal = BigDecimal.ZERO

    /** 鍟嗗鍚嶇О */
    private var restaurant_name = ""

    private lateinit var mDishAdapter: TakeawayAdapter
    private lateinit var mCartAdapter: TakeawayAdapter


    private var isToolbarDark = false

    /** IM鑱婂ぉ寮€鍚姸鎬侊紙1锛氬紑鍚紝2锛氬叧闂級 */
    private var im_status = 0
    private var toolbarStatus = 0
    private var businessPhone = ""
    private var mer_uuid = ""
    private var mer_name = ""

    private lateinit var emptyView: EmptyView

    override fun getLayoutId() = R.layout.activity_take_away_2

    override fun isStatusDark() = isToolbarDark

    override fun initView() {
        binding = ActivityTakeAway2Binding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        val id = intent.getStringExtra(Constant.PARAM_ID)
        if (id.isNullOrBlank()) {
            finish()
            return
        }
        restaurant_id = id
        initRecyclerView()
        initScroll()
    }

    private fun initRecyclerView() {
        // 鑿滃搧鍒楄〃
        mDishAdapter = TakeawayAdapter().apply {
            setEmptyView(EmptyView(this@TakeawayActivity).apply {
                showEmptyDefault()
            })
            onSelectCountChange = this@TakeawayActivity
        }

        binding.dishRv.apply {
            isNestedScrollingEnabled = true
            layoutManager = object : LinearLayoutManager(this@TakeawayActivity, VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            addItemDecoration(DividerDecoration(
                Color.WHITE, SizeUtils.dp2px(10f),
                0, 0
            ).apply { setDrawHeaderFooter(true) })
            adapter = mDishAdapter
        }


        emptyView = EmptyView(this).apply {
            showEmptyDefault()
            setEmptyMessage(R.string.empty_cart)
        }

        // 璐墿杞﹀垪琛?
        mCartAdapter = TakeawayAdapter().apply {
            setEmptyView(emptyView)
            onSelectCountChange = object : TakeawayAdapter.OnSelectCountChange {

                override fun selectCountChange(goodNum: Int, id: Int) {

                    // 鏇存柊鑿滃搧鍒楄〃鏁伴噺
                    mDishAdapter.data.forEachIndexed { index, dishResponse ->
                        if (dishResponse.id == id) {
                            dishResponse.nativeSelectCount = goodNum
                            mDishAdapter.notifyItemChanged(index)
                        }
                    }
                    // 鏇存柊
                    handleBottom()
                }
            }
        }

        binding.takeAwayCart.shoppingCartRv.apply {
            layoutManager = LinearLayoutManager(this@TakeawayActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerDecoration(
                Color.WHITE, SizeUtils.dp2px(10f),
                0, 0
            ).apply { setDrawHeaderFooter(true) })
            adapter = mCartAdapter
        }

    }

    private fun initScroll() {

        binding.useView.setBackgroundColor(getColor(R.color.white))
        binding.toolbar.setBackgroundColor(getColor(R.color.white))
        binding.useView.background.alpha = 0
        binding.toolbar.background.alpha = 0
        binding.scrollView.setOnScrollChangeListener { _, _, y, _, _ ->
            val headHeight = binding.mediaFl.measuredHeight - binding.toolbar.measuredHeight
            var alpha = (y.toFloat() / headHeight * 255).toInt()
            if (alpha >= 255) {
                alpha = 255
            }
            if (alpha <= 5) {
                alpha = 0
            }
            isToolbarDark = alpha > 130
            setBackImage()

            binding.useView.background.alpha = alpha
            binding.toolbar.background.alpha = alpha
            if (alpha == 255) {
                binding.titleTv.text = binding.restaurantTv.text
            }
            if (alpha == 0) {
                binding.titleTv.text = ""
            }
            changeStatusTextColor()
        }

    }


    private fun setBackImage() {
        binding.backIv.setImageResource(
            if (isToolbarDark) {
                R.mipmap.ic_back_black
            } else {
                R.mipmap.ic_back_light
            }
        )
    }

    override fun initData() {
        presenter = TakeawayPresenter(this)
        presenter.getDetailData(restaurant_id)
    }

    override fun bindListener() {

        binding.serviceIv.setOnClickListener {
            if (im_status == 1 && mer_uuid.isNotBlank()) {

                BusinessUtils.showImUnavailableDialog(this)

            } else {
                if (businessPhone.isNotBlank()) {
                    val data = Uri.parse("tel:$businessPhone")
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        this.data = data
                    }
                    startActivity(intent)
                }
            }

        }

        binding.cartIv.setOnClickListener {
            val selectedList = presenter.getSelectedList(mDishAdapter)
            if (selectedList.isNullOrEmpty()) {
                emptyView.showEmptyContainer()
            }

            mCartAdapter.setNewInstance(selectedList)

            if (binding.takeAwayCart.cartLl.visibility != View.VISIBLE) {
                binding.takeAwayCart.cartLl.visibility = View.VISIBLE
            } else {
                binding.takeAwayCart.cartLl.visibility = View.GONE
            }
        }

        binding.takeAwayCart.closeCartV.setOnClickListener {
            binding.takeAwayCart.cartLl.visibility = View.GONE
        }

        binding.settlementTv.setOnClickListener {
            val totalSelectCount = presenter.getTotalSelectCount(mDishAdapter)
            if (totalSelectCount <= 0) {
                ToastUtils.showShort(R.string.nothing_selected)
                return@setOnClickListener
            }
            val totalPrice = presenter.handlePrice(mDishAdapter)
            val selectedList = presenter.getSelectedList(mDishAdapter)
            // 棰濆鍔犱笂閰嶉€佽垂
            val finalTotalPrice = BigDecimalUtil.add(totalPrice, BigDecimalUtil.add(delivery_cost, packing_charges))

            startActivityAfterLogin(OrderSubmitTakeawayActivity::class.java, Bundle().apply {
                putString(Constant.PARAM_ID, restaurant_id)
                putString(Constant.PARAM_PACKING_CHARGES, packing_charges.toPlainString())
                putString(Constant.PARAM_DELIVERY_COST, delivery_cost.toPlainString())
                putString(Constant.PARAM_NAME, restaurant_name)
                putString(Constant.PARAM_TOTAL_PRICE, finalTotalPrice.toPlainString())
                putSerializable(Constant.PARAM_DATA, selectedList)
            })
        }

        binding.takeAwayCart.clearCartTv.setOnClickListener {
            mCartAdapter.setNewInstance(null)
            emptyView.showEmptyContainer()
            presenter.clearCart(mDishAdapter)
            handleBottom()
        }

    }

    override fun bindData(data: TakeawayDetailResponse) {

        GlideUtil.load(this, data.take_out_image, binding.mediaIv)


        businessPhone = data.tel
        mer_uuid = data.mer_uuid
        mer_name = data.mer_name
        val restaurantName = data.restaurant_name
        restaurant_name = restaurantName
        binding.restaurantTv.text = restaurantName
        binding.timeTv.text = getString(R.string.delivery_time_format, data.delivery_time)

        // 閰嶉€佽垂
        delivery_cost = data.delivery_cost
        if (delivery_cost.compareTo(BigDecimal.ZERO) == 1) {
            binding.deliveryFeeTv.text = getString(R.string.delivery_price_format_3, delivery_cost.toPlainString())
        } else {
            binding.deliveryFeeTv.text = getString(R.string.free_delivery)
        }

        // 鍖呰璐?
        packing_charges = data.packing_charges
        binding.takeAwayCart.packagingFeeTv.text = getString(R.string.packaging_fee_format, packing_charges.toPlainString())


        // 鑱婂ぉ
        im_status = data.im_status

        mDishAdapter.setNewInstance(data.goods)

    }


    override fun selectCountChange(goodNum: Int, id: Int) = handleBottom()

    /**
     * 璐墿杞︽暟閲忋€佹€讳环銆佹槸鍚﹁兘缁撶畻
     */
    private fun handleBottom() {
        val totalPrice = presenter.handlePrice(mDishAdapter)
        val totalCount = presenter.getTotalSelectCount(mDishAdapter)

        // 鎬讳环
        binding.totalPriceTv.text = getString(R.string.price_unit_format, totalPrice)

        // 璐墿杞︽暟閲?
        if (totalCount > 0) {
            binding.countTv.visibility = View.VISIBLE
            binding.countTv.text = totalCount.toString()
            binding.cartIv.setImageResource(R.mipmap.ic_cart_blue)
            binding.settlementTv.alpha = 1f
        } else {
            binding.countTv.visibility = View.GONE
            binding.cartIv.setImageResource(R.mipmap.ic_cart_gray)
            binding.settlementTv.alpha = 0.5f

        }

    }


}
