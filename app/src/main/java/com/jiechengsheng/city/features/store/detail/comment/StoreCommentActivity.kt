package com.jiechengsheng.city.features.store.detail.comment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.StringUtils
import com.google.android.material.radiobutton.MaterialRadioButton
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.store.comment.StoreCommentCount
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.store.detail.comment.chiild.StoreCommentChildFragment
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.databinding.ActivityStoreCommentBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/7/17 15:54.
 * 全部评价
 */
class StoreCommentActivity : BaseMvpActivity<StoreCommentListPresenter>(), StoreCommentListView {

    private lateinit var binding: ActivityStoreCommentBinding

    var shop_id: Int = 0

    val TAB_TITLES =
            arrayOf(StringUtils.getString(R.string.all),
                    StringUtils.getString(R.string.has_image_format),
                    StringUtils.getString(R.string.newest_format)
            )

    override fun getLayoutId() = R.layout.activity_store_comment

    override fun isStatusDark() = true

    override fun initView() {
        binding = ActivityStoreCommentBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }
        shop_id = bundle.getInt(Constant.PARAM_SHOP_ID, 0)

        binding.pager.offscreenPageLimit = TAB_TITLES.size
        binding.pager.adapter = InnerPagerAdapter(supportFragmentManager, 0)
    }

    override fun initData() {
        presenter = StoreCommentListPresenter(this)
        presenter.getCount(shop_id)
    }

    override fun bindListener() {

        binding.typeRg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.all_rb -> binding.pager.setCurrentItem(0, true)
                R.id.image_rb -> binding.pager.setCurrentItem(1, true)
                R.id.newest_rb -> binding.pager.setCurrentItem(2, true)
            }
        }
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageSelected(position: Int) {
                val child = binding.typeRg.getChildAt(position) as MaterialRadioButton
                child.isChecked = true
            }

        })


    }

    private inner class InnerPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {


        override fun getPageTitle(position: Int): CharSequence? = TAB_TITLES[position]


        override fun getItem(position: Int): Fragment =
                StoreCommentChildFragment.newInstance(shop_id, true, position + 1)

        override fun getCount(): Int = TAB_TITLES.size
    }

    override fun bindCount(response: StoreCommentCount) {
        binding.allRb.text = getString(R.string.all_format , response.all)
        binding.imageRb.text = getString(R.string.has_image_format , response.image)
        binding.newestRb.text = getString(R.string.newest_format , response.newest)
    }

}