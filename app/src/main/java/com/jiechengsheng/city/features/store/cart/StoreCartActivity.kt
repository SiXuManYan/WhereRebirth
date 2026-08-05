package com.jiechengsheng.city.features.store.cart

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.features.store.cart.child.StoreCartFragment
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityStoreCartBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/7/5 10:30.
 * 商城购物车
 */
class StoreCartActivity : BaseActivity() {

    private lateinit var binding: ActivityStoreCartBinding

    val titles = arrayOf(StringUtils.getString(R.string.take_carts), StringUtils.getString(R.string.delivery_carts))

    override fun getLayoutId() = R.layout.activity_store_cart

    override fun isStatusDark(): Boolean = true

    override fun initView() {
        binding = ActivityStoreCartBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.white))
        binding.pager.offscreenPageLimit = titles.size
        binding.pager.adapter = StorePagerAdapter(supportFragmentManager, 0)
        binding.tabsType.setViewPager(binding.pager)
    }

    override fun initData() = Unit

    override fun bindListener() {
        binding.backIv.setOnClickListener {
            finish()
        }

        binding.editTv.setOnClickListener {
            binding.rightVs.displayedChild = 1
            EventBus.getDefault().post(BaseEvent<Boolean>(EventCode.EVENT_STORE_CART_HANDLE, true))

        }

        binding.cancelTv.setOnClickListener {
            binding.rightVs.displayedChild = 0
            EventBus.getDefault().post(BaseEvent<Boolean>(EventCode.EVENT_STORE_CART_HANDLE, false))

        }


    }

    private inner class StorePagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {


        override fun getPageTitle(position: Int): CharSequence = titles[position]

        override fun getItem(position: Int): Fragment = StoreCartFragment.newInstance(position)

        override fun getCount(): Int = 2
    }

}