package com.jiechengsheng.city.features.mall.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.MallCategory
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.mall.cart.MallCartActivity
import com.jiechengsheng.city.features.mall.home.child.MallHomeChildFragment
import com.jiechengsheng.city.features.search.SearchAllActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.databinding.ActivityMallHomeBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/11/30 16:22.
 * 商城首页
 */
class MallHomeActivity : BaseMvpActivity<MallHomePresenter>(), MallHomeView {

    private lateinit var binding: ActivityMallHomeBinding

    private lateinit var mAdapter: TopCategoryAdapter

    private var firstCategory: ArrayList<MallCategory> = ArrayList()

    override fun getLayoutId() = R.layout.activity_mall_home

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityMallHomeBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initTop()
    }

    private fun initTop() {

        mAdapter = TopCategoryAdapter().apply {
            setOnItemClickListener { _, _, position ->
                val selectCategory = mAdapter.data[position]

                // 更新标题
                binding.pagerVp.currentItem = position
                showFilter(false)
            }
        }

        binding.topCategoryRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }

    }

    private fun showFilter(show: Boolean) {
        if (show) {
            binding.topCategoryLl.visibility = View.VISIBLE
            binding.allIv.setImageResource(R.mipmap.ic_up_black)
        } else {
            binding.topCategoryLl.visibility = View.GONE
            binding.allIv.setImageResource(R.mipmap.ic_down_black)

        }
    }


    override fun initData() {
        presenter = MallHomePresenter(this)
        presenter.getFirstCategory(true)
    }

    override fun bindListener() {

        binding.cartIv.setOnClickListener {
            startActivityAfterLogin(MallCartActivity::class.java)
        }

        binding.searchLl.setOnClickListener {
            startActivity(SearchAllActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_TYPE, 6)
            })
        }
        binding.allIv.setOnClickListener {
            showFilter(binding.topCategoryLl.visibility != View.VISIBLE)
        }
        binding.dismissView.setOnClickListener {
            showFilter(false)
        }

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

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) = Unit
        })

        val innerPagerAdapter = InnerPagerAdapter(supportFragmentManager, 0)
        innerPagerAdapter.notifyDataSetChanged()
        binding.pagerVp.adapter = innerPagerAdapter

        binding.tabsStl.setViewPager(binding.pagerVp, titles.toTypedArray())

        mAdapter.setNewInstance(firstCategory)
    }


    private inner class InnerPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

        override fun getPageTitle(position: Int): CharSequence = firstCategory[position].name

        override fun getItem(position: Int): Fragment = MallHomeChildFragment().apply {
            targetFirstCategory = firstCategory[position]
        }

        override fun getCount(): Int = firstCategory.size
    }
}