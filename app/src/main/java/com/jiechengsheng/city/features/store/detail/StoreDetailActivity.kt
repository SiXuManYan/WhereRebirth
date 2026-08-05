package com.jiechengsheng.city.features.store.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.StoreShopRequest
import com.jiechengsheng.city.api.response.store.StoreDetail
import com.jiechengsheng.city.api.response.store.StoreGoods
import com.jiechengsheng.city.api.response.store.StoreGoodsCommit
import com.jiechengsheng.city.api.response.store.StoreOrderCommitData
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.store.detail.comment.chiild.StoreCommentChildFragment
import com.jiechengsheng.city.features.store.detail.good.StoreGoodFragment
import com.jiechengsheng.city.features.store.order.StoreOrderCommitActivity
import com.jiechengsheng.city.frames.common.Html5Url
import com.jiechengsheng.city.utils.*
import com.jiechengsheng.city.view.XBanner.AbstractUrlLoader
import com.jiechengsheng.city.view.XBanner.XBanner
import org.greenrobot.eventbus.EventBus
import pl.droidsonroids.gif.GifImageView
import java.math.BigDecimal
import com.jiechengsheng.city.databinding.ActivityStoreDetailBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/6/15 10:33.
 * 鍟嗗煄搴楅摵璇︽儏
 */
class StoreDetailActivity : BaseMvpActivity<StoreDetailPresenter>(), StoreDetailView {

    private lateinit var binding: ActivityStoreDetailBinding

    private val pager: com.jiechengsheng.city.widget.NoScrollViewPager
        get() = findViewById(R.id.pager)

    var shop_id: Int = 0
    var shop_name: String = ""

    /**
     * 閰嶉€佽垂
     */
    var delivery_fee: Float = 0f


    /**
     * 閰嶉€佹椂闂?
     */
    var delivery_times = ""

    /**
     * 鑷彇鏃堕棿
     */
    var take_times = ""

    val TAB_TITLES =
        arrayOf(
            StringUtils.getString(R.string.good),
            StringUtils.getString(R.string.comment)
        )

    var isBuyNow = false

    var goodSelect: StoreGoods? = null

    var now_price: BigDecimal = BigDecimal.ZERO

    var good_id = 0
    var good_image = ""
    var good_name = ""

    /**
     * 鏀惰棌鐘舵€侊紙1锛氭湭鏀惰棌锛?锛氬凡鏀惰棌锛?
     */
    private var collect_status = 0


    companion object {

        fun navigation(context: Context, id: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ID, id)
            }
            val intent = Intent(context, StoreDetailActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun getLayoutId() = R.layout.activity_store_detail

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityStoreDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)


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

        binding.pager.setNoScroll(true)
        binding.pager.offscreenPageLimit = TAB_TITLES.size
        binding.pager.adapter = InnerPagerAdapter(supportFragmentManager, 0)
        binding.tabsType.setViewPager(binding.pager)

        // number

        binding.storeCart.numberView.apply {
            MIN_GOOD_NUM = 1
            alwaysEnableCut = true
            MIN_GOOD_NUM = 1
            cut_iv.setImageResource(R.mipmap.ic_cut_blue)
            add_iv.setImageResource(R.mipmap.ic_add_blue)
            updateNumberJudgeMin(1)
            cut_iv.visibility = View.VISIBLE
        }

        binding.storeCart.handleTv.text = getString(R.string.buy_now)
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

    override fun initData() {
        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }
        shop_id = bundle.getInt(Constant.PARAM_ID, 0)
        presenter = StoreDetailPresenter(this)
        presenter.getDetail(shop_id)
    }

    override fun bindListener() {
        binding.foldLl.setOnClickListener {
            if (binding.hideLl.visibility != View.VISIBLE) {
                binding.hideLl.visibility = View.VISIBLE
                binding.foldTv.text = getString(R.string.collapse_details)
                binding.foldIv.rotation = 180f
            } else {
                binding.hideLl.visibility = View.GONE
                binding.foldTv.text = getString(R.string.look_more)
                binding.foldIv.rotation = 0f
            }
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
                    good_id = this@StoreDetailActivity.good_id
                    delivery_type = deliveryType
                    image = good_image
                    goodName = good_name
                    good_num = goodNum
                    price = finalPrice
                }

                val apply = StoreOrderCommitData().apply {
                    shop_id = this@StoreDetailActivity.shop_id
                    shop_title = this@StoreDetailActivity.shop_name
                    delivery_type = deliveryType
                    delivery_fee = this@StoreDetailActivity.delivery_fee
                    goods.add(goodInfo)
                }

                val appList: ArrayList<StoreOrderCommitData> = ArrayList()
                appList.add(apply)

                startActivityAfterLogin(StoreOrderCommitActivity::class.java, Bundle().apply {
                    putSerializable(Constant.PARAM_ORDER_COMMIT_DATA, appList)
                })
                binding.storeCart.cartLl.visibility = View.GONE
                binding.storeCart.numberView.updateNumberJudgeMin(1)
            }


        }


        binding.likeIv.setOnClickListener {
            if (collect_status == 1) {
                presenter.collection(shop_id)
            } else {
                presenter.unCollection(shop_id)
            }
        }

        binding.shareIv.setOnClickListener {
            val url = String.format(Html5Url.SHARE_FACEBOOK, Html5Url.MODEL_E_STORE, shop_id)
            MobUtil.shareFacebookWebPage(url, this@StoreDetailActivity)
        }

    }

    override fun bindDetail(data: StoreDetail) {

        binding.topBanner.setImageUrls(data.images).start()
        binding.titleTv.text = data.title
        shop_name = data.title

        binding.navLl.setOnClickListener {
            if (data.lat != 0f && data.lng != 0f) {
                FeaturesUtil.startNaviGoogle(this, data.lat, data.lng)
            }
        }

        if (data.im_status == 1 && !TextUtils.isEmpty(data.mer_uuid)) {
            binding.phoneValueIv.setImageResource(R.mipmap.ic_phone_bold)
            binding.phoneLl.setOnClickListener {
                BusinessUtils.showImUnavailableDialog(this)
            }
        } else {
            binding.phoneValueIv.setImageResource(R.mipmap.ic_phone_bold)
            binding.phoneLl.setOnClickListener {
                val tel = data.tel
                if (!TextUtils.isEmpty(tel)) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        this.data = Uri.parse("tel:$tel")
                    }
                    startActivity(intent)
                }
            }
        }

        val stringArray = StringUtils.getStringArray(R.array.weeks)
        binding.timeValueTv.text = getString(
            R.string.mechanism_to_format,
            data.start_time,
            data.end_time,
            stringArray[data.week_start - 1],
            stringArray[data.week_end - 1]
        )
        binding.phoneValueTv.text = data.tel
        binding.webValueTv.text = data.web_site
        binding.emailValueTv.text = data.email
        binding.facebookValueTv.text = data.facebook
        binding.addressNameTv.text = data.address
        delivery_fee = data.delivery_fee
        EventBus.getDefault().post(BaseEvent<StoreShopRequest>(StoreShopRequest(shop_name, delivery_fee)))

        delivery_times = data.delivery_times
        take_times = data.take_times

        collect_status = data.collect_status
        setLikeImage()

    }


    private fun setLikeImage() {

        binding.likeIv.setImageResource(
            if (collect_status == 1) {
                R.mipmap.ic_like_red_night
            } else {
                R.mipmap.ic_like_normal_night
            }
        )
    }


    private inner class InnerPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {


        override fun getPageTitle(position: Int): CharSequence? = TAB_TITLES[position]


        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                StoreGoodFragment.newInstance(shop_id)
            } else {
                StoreCommentChildFragment.newInstance(shop_id, false)
            }
        }

        override fun getCount(): Int = TAB_TITLES.size
    }


    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        val data = baseEvent.data
        if (data is StoreGoods) {


            binding.storeCart.numberView.apply {
                updateNumber(1)
                MAX_GOOD_NUM = data.inventory
            }

            binding.storeCart.cartLl.visibility = View.VISIBLE
            isBuyNow = true
            now_price = data.price
            good_id = data.id


            if (data.images.isNotEmpty()) {
                good_image = data.images[0]
            }
            good_name = data.title


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

            binding.storeCart.stockTv.text = getString(R.string.stock_format, data.inventory)
        }

    }

    override fun changeCollection(isCollection: Boolean) {

        collect_status = if (isCollection) {
            2
        } else {
            1
        }
        setLikeImage()
        EventBus.getDefault().post(BaseEvent<String>(EventCode.EVENT_REFRESH_COLLECTION))
    }

}
