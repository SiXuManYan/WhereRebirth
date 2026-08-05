package com.jiechengsheng.city.features.bills

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.BannerResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.bills.channel.BillsChannelActivity
import com.jiechengsheng.city.features.bills.guide.BillGuideAdapter
import com.jiechengsheng.city.features.bills.guide.GuideItemClickListener
import com.jiechengsheng.city.features.bills.record.BillsRecordActivity
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.CacheUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.view.XBanner.AbstractUrlLoader
import com.jiechengsheng.city.view.XBanner.XBanner
import pl.droidsonroids.gif.GifImageView
import com.jiechengsheng.city.databinding.ActivityPayBillsBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/4/15 14:12.
 * 水电缴费
 */
class PayBillsActivity : BaseMvpActivity<PayBillsPresenter>(), PayBillsView {

    private lateinit var binding: ActivityPayBillsBinding


    override fun getLayoutId() = R.layout.activity_pay_bills

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityPayBillsBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initBanner()
        initPager()

    }

    private fun initBanner() {

        binding.topBanner.setBannerTypes(XBanner.CIRCLE_INDICATOR)
            .setTitleHeight(50)
            .isAutoPlay(true)
            .setDelay(5000)
            .setUpIndicators(R.drawable.ic_selected, R.drawable.ic_unselected)
            .setUpIndicatorSize(6, 6)
            .setIndicatorGravity(XBanner.INDICATOR_CENTER)
            .setImageLoader(object : AbstractUrlLoader() {
                override fun loadImages(context: Context, url: String, image: ImageView) {
                    GlideUtil.load(context, url, image, 4)
                }

                override fun loadGifs(context: Context, url: String, gifImageView: GifImageView, scaleType: ImageView.ScaleType) {
                    GlideUtil.load(context, url, gifImageView, 4)
                }
            })
    }

    private fun initPager() {
        val alreadyShow = CacheUtil.getShareDefault().getBoolean(Constant.SP_ALREADY_SHOW_BILLS_GUIDE, false)
        if (alreadyShow) {
            binding.realContentLl.visibility = View.VISIBLE
            binding.pagerVp.visibility = View.GONE
            return
        }

        binding.realContentLl.visibility = View.GONE
        binding.pagerVp.visibility = View.VISIBLE

        val adapter = BillGuideAdapter()
        adapter.itemClickListener = object : GuideItemClickListener {
            override fun pagerItemClick(position: Int) {
                if (position == adapter.count - 1) {
                    CacheUtil.getShareDefault().put(Constant.SP_ALREADY_SHOW_BILLS_GUIDE, true)
                    binding.realContentLl.visibility = View.VISIBLE
                    binding.pagerVp.visibility = View.GONE
                } else {
                    binding.pagerVp.setCurrentItem(position + 1, true)
                }
            }
        }
        binding.pagerVp.adapter = adapter
        binding.pagerVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageSelected(position: Int) {

            }
        })

    }

    override fun initData() {
        presenter = PayBillsPresenter(this)
        presenter.getTopBanner()
    }


    override fun bindListener() {
        binding.prepaidLl.setOnClickListener {
            startActivityAfterLogin(BillsChannelActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_TYPE, 1)
            })
        }
        binding.waterLl.setOnClickListener {
            startActivityAfterLogin(BillsChannelActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_TYPE, 2)
            })
        }
        binding.electricLl.setOnClickListener {
            startActivityAfterLogin(BillsChannelActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_TYPE, 3)
            })
        }
        binding.internetLl.setOnClickListener {
            startActivityAfterLogin(BillsChannelActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_TYPE, 4)
            })
        }
        binding.recordIv.setOnClickListener {
            startActivityAfterLogin(BillsRecordActivity::class.java)
        }

    }


    override fun bindTopBannerData(bannerUrls: ArrayList<String>, response: ArrayList<BannerResponse>) {

        binding.topBanner.setImageUrls(bannerUrls)
        binding.topBanner.setBannerPageListener(object : XBanner.BannerPageListener {

            override fun onBannerDragging(item: Int) = Unit

            override fun onBannerIdle(item: Int) = Unit

            override fun onBannerClick(item: Int) {
                val data = response[item]
                BusinessUtils.handleBannerClick(this@PayBillsActivity, data)
            }

        }).start()
    }


    override fun onDestroy() {
        binding.topBanner?.releaseBanner()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.topBanner?.start()
    }


    override fun onPause() {
        super.onPause()
        binding.topBanner?.pause()
    }

}