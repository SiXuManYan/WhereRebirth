package com.jiechengsheng.city.features.gourmet.restaurant.packages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.BuildConfig
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.AddCartRequest
import com.jiechengsheng.city.api.response.gourmet.cart.Products
import com.jiechengsheng.city.api.response.gourmet.cart.ShoppingCartResponse
import com.jiechengsheng.city.api.response.gourmet.dish.DishDetailResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.gourmet.cart.ShoppingCartActivity
import com.jiechengsheng.city.features.gourmet.order.OrderSubmitActivity
import com.jiechengsheng.city.features.hotel.detail.media.DetailMediaAdapter
import com.jiechengsheng.city.features.hotel.detail.media.MediaData
import com.jiechengsheng.city.utils.BigDecimalUtil
import com.jiechengsheng.city.utils.Constant
import com.shuyu.gsyvideoplayer.GSYVideoManager
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivitySetMeal2Binding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/4/6 14:38.
 * 菜品详情
 */
class SetMealActivity : BaseMvpActivity<SetMealPresenter>(), SetMealView {

    private lateinit var binding: ActivitySetMeal2Binding


    private var isToolbarDark = false
    private var mEatInFoodId = 0
    private var mRestaurantId = 0
    private var mRestaurantName = ""
    private var mData: DishDetailResponse? = null
    private lateinit var mMediaAdapter: DetailMediaAdapter

    private var goodNumber = 1
    private var mInventory = 0
    private var singlePrice: BigDecimal = BigDecimal.ZERO
    private var totalPrice: BigDecimal = BigDecimal.ZERO

    override fun isStatusDark() = isToolbarDark

    override fun getLayoutId() = R.layout.activity_set_meal_2

    override fun initView() {
        binding = ActivitySetMeal2Binding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
        initMedia()
        initScroll()
        binding.numberView.apply {
            alwaysEnableCut = true
            MIN_GOOD_NUM = 1
            MAX_GOOD_NUM = 20
            cut_iv.setImageResource(R.mipmap.ic_cut_blue)
            add_iv.setImageResource(R.mipmap.ic_add_blue)
            cutResIdCommon = R.mipmap.ic_cut_blue
            cutResIdMin = R.mipmap.ic_cut_blue_transparent
            addResIdCommon = R.mipmap.ic_add_blue
            addResIdMax = R.mipmap.ic_add_blue_transparent

            updateNumber(goodNumber)
            cut_iv.visibility = View.VISIBLE
            valueChangeListener = this@SetMealActivity
        }

        binding.shoppingCart.apply {
            setMessageImageResource(R.mipmap.ic_shopping_cart)
            changeContainerSize(50f, 50f)
            changeMessageCountSize(14f, 14f)
        }

        if (BuildConfig.FLAVOR == "dev"){
            binding.payLl.visibility = View.VISIBLE
        }

    }


    private fun initExtra() {
        val bundle = intent.extras
        bundle?.let {
            mEatInFoodId = it.getInt(Constant.PARAM_ID, 0)
            mRestaurantId = it.getInt(Constant.PARAM_RESTAURANT_ID, 0)
            goodNumber = it.getInt(Constant.PARAM_GOOD_NUMBER, 1)
            mRestaurantName = it.getString(Constant.PARAM_RESTAURANT_NAME, "")
        }
    }

    private fun initMedia() {
        mMediaAdapter = DetailMediaAdapter()
        PagerSnapHelper().attachToRecyclerView(binding.mediaRv)
        binding.mediaRv.apply {
            layoutManager = LinearLayoutManager(this@SetMealActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mMediaAdapter
        }
        binding.mediaRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastItemPosition = layoutManager.findLastVisibleItemPosition()

                if (GSYVideoManager.instance().playPosition >= 0) {
                    val position = GSYVideoManager.instance().playPosition
                    if (GSYVideoManager.instance().playTag == DetailMediaAdapter.TAG && (position < firstItemPosition || position > lastItemPosition)) {
                        if (GSYVideoManager.isFullState(this@SetMealActivity)) {
                            return
                        }
                        GSYVideoManager.releaseAllVideos()
                        mMediaAdapter.notifyDataSetChanged()
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.pointView.onPageSelected(firstItemPosition)
                }
            }
        })
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
            binding.useView.background.alpha = alpha
            binding.toolbar.background.alpha = alpha
            changeStatusTextColor()
        }
    }


    override fun initData() {
        presenter = SetMealPresenter(this)
        presenter.getDetail(mEatInFoodId)
        presenter.getCartCount()
    }

    override fun bindListener() {
        binding.shoppingCart.setOnClickListener { startActivityAfterLogin(ShoppingCartActivity::class.java) }
        binding.buyAfterTv.setOnClickListener { onBuyAfter() }
        binding.buyNowTv.setOnClickListener { onBuyNow() }
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
        presenter?.getCartCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun bindData(data: DishDetailResponse, inventoryValue: Int) {

        val mediaList = ArrayList<MediaData>()
        val media = MediaData().apply {
            type = MediaData.IMAGE
            cover = data.image
            src = data.image
        }
        mediaList.add(media)
        mMediaAdapter.setNewInstance(mediaList)
        binding.pointView.setPointCount(1)
        mInventory = inventoryValue
        binding.numberView.MAX_GOOD_NUM = inventoryValue
        binding.numberView.updateNumber(goodNumber)
        binding.numberView.checkMax()

        val price = data.price
        singlePrice = price
        binding.nowPriceTv.text = getString(R.string.price_unit_format, price.toPlainString())
        val oldPrice = getString(R.string.price_unit_format, data.original_price)
        val builder = SpanUtils().append(oldPrice).setStrikethrough().create()
        binding.oldPriceTv.text = builder
        binding.salesTv.text = getString(R.string.sale_format, data.sale_num)
        data.meals.apply {
            if (isNotBlank()) {
                binding.setMealContentTv.text = this
            } else {
                binding.setMealContentTv.gravity = Gravity.CENTER
            }
        }
        data.rule.apply {
            if (isNotBlank()) {
                binding.ruleTv.text = this
            } else {
                binding.ruleTv.gravity = Gravity.CENTER
            }
        }
        mData = data

        if (data.inventory.isNotBlank()) {
            binding.stockTv.text = getString(R.string.stock_format_2, data.inventory)
        }

        binding.nameTv.text = data.title
        handlePrice()
    }

    override fun bindCartCount(nums: Int) {
        binding.shoppingCart.setMessageCount(nums)
    }

    private fun onBuyNow() {
        if (mInventory < goodNumber) {
            ToastUtils.showShort(R.string.out_of_stock)
            return
        }
        val product = Products().apply {
            good_data.id = mData!!.id
            good_data.title = mData!!.title
            good_data.image = mData!!.image
            good_data.price = mData!!.price
            good_data.original_price = mData!!.original_price
            good_num = goodNumber
            nativeIsSelect = true
        }
        val response = ShoppingCartResponse().apply {
            restaurant_id = mRestaurantId.toString()
            nativeIsSelect = true
            restaurant_name = mRestaurantName
            products.add(product)
        }

        val value = ArrayList<ShoppingCartResponse>().apply {
            add(response)
        }
        startActivityAfterLogin(OrderSubmitActivity::class.java, Bundle().apply {
            putSerializable(Constant.PARAM_DATA, value)
            val totalPrice = BigDecimalUtil.mul(mData!!.price, BigDecimal(goodNumber))
            putString(Constant.PARAM_TOTAL_PRICE, totalPrice.toEngineeringString())
        })
    }

    private fun onBuyAfter() {
        if (mInventory < goodNumber) {
            ToastUtils.showShort(R.string.out_of_stock)
            return
        }
        val request = AddCartRequest().apply {
            good_id = mData!!.id
            good_num = goodNumber
        }
        presenter.addCart(request)
    }

    override fun onNumberChange(goodNum: Int, isAdd: Boolean) {
        goodNumber = goodNum
        handlePrice()
    }

    private fun handlePrice() {
        totalPrice = BigDecimalUtil.mul(singlePrice, BigDecimal(goodNumber))
        binding.totalPriceTv.text = getString(R.string.price_unit_format, totalPrice.toPlainString())
    }


}