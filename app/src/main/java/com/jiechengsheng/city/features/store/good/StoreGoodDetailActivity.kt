package com.jiechengsheng.city.features.store.good

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.store.StoreGoodDetail
import com.jiechengsheng.city.api.response.store.StoreGoodsCommit
import com.jiechengsheng.city.api.response.store.StoreOrderCommitData
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.store.order.StoreOrderCommitActivity
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.BigDecimalUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.view.XBanner.AbstractUrlLoader
import com.jiechengsheng.city.view.XBanner.XBanner
import pl.droidsonroids.gif.GifImageView
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityStoreGoodDetailBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/6/18 14:23.
 * 鍟嗗煄鍟嗗搧璇︽儏
 */
class StoreGoodDetailActivity : BaseMvpActivity<StoreGoodDetailPresenter>(), StoreGoodDetailView {

    private lateinit var binding: ActivityStoreGoodDetailBinding

    var good_id: Int = 0
    var shop_id: Int = 0

    var isBuyNow = false

    /**
     * 閰嶉€佹椂闂?
     */
    var delivery_times = ""

    /**
     * 鑷彇鏃堕棿
     */
    var take_times = ""

    /**
     * 閰嶉€佽垂
     */
    var delivery_fee: Float = 0f

    /**
     * 鍟嗗鍚嶇О
     */
    var shop_name = ""

    /**
     * 鍟嗗搧鍥剧墖
     */
    var good_image = ""
    var good_name = ""

    var now_price: BigDecimal = BigDecimal.ZERO


    override fun getLayoutId() = R.layout.activity_store_good_detail

    override fun initView() {
        binding = ActivityStoreGoodDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }
        good_id = bundle.getInt(Constant.PARAM_ID, 0)
        delivery_fee = bundle.getFloat(Constant.PARAM_DELIVERY_FEE, 0f)
        shop_name = bundle.getString(Constant.PARAM_SHOP_NAME, "")
        shop_id = bundle.getInt(Constant.PARAM_SHOP_ID)

        val bannerParams = binding.llBanner.layoutParams.apply {
            height = ScreenUtils.getScreenWidth() * 206 / 375
        }
        binding.llBanner.layoutParams = bannerParams
        binding.topBanner.setBannerTypes(XBanner.CIRCLE_INDICATOR)
                .setTitleHeight(50)
                .isAutoPlay(true)
                .setDelay(5000)
                .setUpIndicators(R.drawable.ic_selected, R.drawable.ic_unselected)
                .setUpIndicatorSize(6, 6)
                .setIndicatorGravity(XBanner.INDICATOR_CENTER)
                .setImageLoader(object : AbstractUrlLoader() {
                    override fun loadImages(context: Context, url: String, image: ImageView) {
                        GlideUtil.load(context, url, image)
                    }

                    override fun loadGifs(context: Context, url: String, gifImageView: GifImageView, scaleType: ImageView.ScaleType) {
                        GlideUtil.load(context, url, gifImageView)
                    }
                })

        binding.storeCart.numberView.apply {
            alwaysEnableCut = true
            MIN_GOOD_NUM = 1
            cut_iv.setImageResource(R.mipmap.ic_cut_blue)
            add_iv.setImageResource(R.mipmap.ic_add_blue)
            updateNumberJudgeMin(1)
            cut_iv.visibility = View.VISIBLE
        }
    }

    override fun isStatusDark() = true

    override fun initData() {
        presenter = StoreGoodDetailPresenter(this)
        presenter.getData(good_id)
    }

    override fun onResume() {
        super.onResume()
        binding.topBanner.startPlayIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.topBanner?.releaseBanner()
    }

    override fun onPause() {
        super.onPause()
        binding.topBanner.pause()
    }


    override fun bindListener() {
        binding.buyAfterTv.setOnClickListener {
            if (!User.isLogon()) {
                startActivity(LoginActivity::class.java)
                return@setOnClickListener
            }
            binding.storeCart.cartLl.visibility = View.VISIBLE
            binding.storeCart.handleTv.text = binding.buyAfterTv.text
            isBuyNow = false
        }
        binding.buyNowTv.setOnClickListener {
            if (!User.isLogon()) {
                startActivity(LoginActivity::class.java)
                return@setOnClickListener
            }
            binding.storeCart.cartLl.visibility = View.VISIBLE
            binding.storeCart.handleTv.text = binding.buyNowTv.text
            isBuyNow = true
        }

        binding.storeCart.closeCartV.setOnClickListener {
            binding.storeCart.cartLl.visibility = View.GONE
        }

        binding.storeCart.businessServiceRg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.express_rb -> binding.storeCart.timeTv.text = delivery_times
                R.id.self_rb -> binding.storeCart.timeTv.text = take_times
            }
        }

        binding.storeCart.handleTv.setOnClickListener {
            if (!binding.storeCart.expressRb.isChecked && !binding.storeCart.selfRb.isChecked) {
                ToastUtils.showShort(R.string.delivery_method_none)
                return@setOnClickListener

            }

            val deliveryType = if (binding.storeCart.expressRb.isChecked) {
                2
            } else {
                1
            }

            val goodNum = binding.storeCart.numberView.goodNum
            val finalPrice = BigDecimalUtil.mul(now_price, BigDecimal(goodNum))

            if (isBuyNow) {
                val goodInfo = StoreGoodsCommit().apply {
                    good_id = this@StoreGoodDetailActivity.good_id
                    delivery_type = deliveryType
                    image = good_image
                    goodName = good_name
                    good_num = goodNum
                    price = finalPrice
                }

                val apply = StoreOrderCommitData().apply {
                    shop_id = this@StoreGoodDetailActivity.shop_id
                    shop_title = this@StoreGoodDetailActivity.shop_name
                    delivery_type = deliveryType
                    delivery_fee = this@StoreGoodDetailActivity.delivery_fee
                    goods.add(goodInfo)

                }

                val appList: ArrayList<StoreOrderCommitData> = ArrayList()
                appList.add(apply)

                startActivityAfterLogin(StoreOrderCommitActivity::class.java, Bundle().apply {
                    putSerializable(Constant.PARAM_ORDER_COMMIT_DATA, appList)
                })
                binding.storeCart.cartLl.visibility = View.GONE
                binding.storeCart.numberView.updateNumberJudgeMin(1)
            } else {
                presenter.addCart(good_id, goodNum, deliveryType)
            }


        }


    }


    override fun bindData(data: StoreGoodDetail) {
        val images = data.images
        if (images.isNotEmpty()) {
            binding.topBanner.setImageUrls(images).start()
            good_image = images[0]
        }

        binding.titleTv.text = data.title
        good_name = data.title
        now_price = data.price
        binding.nowPriceTv.text = StringUtils.getString(R.string.price_unit_format, now_price.toPlainString())
        val oldPrice = StringUtils.getString(R.string.price_unit_format, data.original_price.toPlainString())
        val builder = SpanUtils().append(oldPrice).setStrikethrough().create()
        binding.oldPriceTv.text = builder
        binding.descTv.text = data.desc
        when (data.delivery_type) {
            1 -> {
                binding.storeCart.expressRb.visibility = View.GONE
                binding.storeCart.selfRb.visibility = View.VISIBLE
            }
            2 -> {
                binding.storeCart.expressRb.visibility = View.VISIBLE
                binding.storeCart.selfRb.visibility = View.GONE
            }
            3 -> {
                binding.storeCart.expressRb.visibility = View.VISIBLE
                binding.storeCart.selfRb.visibility = View.VISIBLE
            }
        }

        delivery_times = data.delivery_times
        take_times = data.take_times
        val inventory = data.inventory
        binding.storeCart.stockTv.text = getString(R.string.stock_format, inventory)
        binding.storeCart.numberView.MAX_GOOD_NUM = inventory
    }


    override fun addSuccess() {
        binding.storeCart.cartLl.visibility = View.GONE
        binding.storeCart.numberView.updateNumberJudgeMin(1)
    }
}