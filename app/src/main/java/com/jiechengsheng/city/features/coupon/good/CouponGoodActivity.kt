package com.jiechengsheng.city.features.coupon.good

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.MallCategory
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.coupon.good.child.CouponGoodFragment
import com.jiechengsheng.city.features.mall.home.MallHomePresenter
import com.jiechengsheng.city.features.mall.home.MallHomeView
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.databinding.ActitiyCouponGoodBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/3/4 14:29.
 * 优惠券商品列表
 */
class CouponGoodActivity : BaseMvpActivity<MallHomePresenter>(), MallHomeView {

    private lateinit var binding: ActitiyCouponGoodBinding


    /** 代金券id */
    private var couponId = 0

    /** 店铺券对应的店铺id */
    private var shopId = 0

    private var firstCategory: ArrayList<MallCategory> = ArrayList()

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.actitiy_coupon_good

    override fun initView() {
        binding = ActitiyCouponGoodBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        couponId = intent.getIntExtra(Constant.PARAM_ID, 0)
        shopId = intent.getIntExtra(Constant.PARAM_SHOP_ID, 0)
    }

    override fun initData() {
        presenter = MallHomePresenter(this)
        presenter.getFirstCategory(true)
    }

    override fun bindListener() {

    }

    override fun bindCategory(response: ArrayList<MallCategory>, titles: ArrayList<String>) {
        if (response.isEmpty()) {
            return
        }
        firstCategory.clear()
        firstCategory.addAll(response)

        binding.pagerVp.offscreenPageLimit = response.size
        binding.pagerVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageSelected(position: Int) = Unit

            override fun onPageScrollStateChanged(state: Int) = Unit
        })

        val innerPagerAdapter = InnerPagerAdapter(supportFragmentManager, 0)
        innerPagerAdapter.notifyDataSetChanged()
        binding.pagerVp.adapter = innerPagerAdapter
        binding.tabsStl.setViewPager(binding.pagerVp, titles.toTypedArray())

    }


    private inner class InnerPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

        override fun getPageTitle(position: Int): CharSequence = firstCategory[position].name


        override fun getItem(position: Int): Fragment = CouponGoodFragment().apply {
            mCategoryId = firstCategory[position].id
            mCouponId = couponId
            mShopId = shopId
        }

        override fun getCount(): Int = firstCategory.size
    }
}