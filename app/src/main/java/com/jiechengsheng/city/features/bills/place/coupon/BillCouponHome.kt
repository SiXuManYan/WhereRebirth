package com.jiechengsheng.city.features.bills.place.coupon

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseBottomSheetDialogFragment
import com.jiechengsheng.city.base.mvp.FixedHeightBottomSheetDialog
import com.jiechengsheng.city.features.mall.buy.coupon.OrderCouponHomePresenter
import com.jiechengsheng.city.features.mall.buy.coupon.OrderCouponHomeView
import com.jiechengsheng.city.databinding.FragmentOrderCouponHomeBinding
/**
 * Created by Wangsw  2022/10/12 14:38.
 *
 */
class BillCouponHome : BaseBottomSheetDialogFragment<OrderCouponHomePresenter>(), OrderCouponHomeView {

    private var _binding: FragmentOrderCouponHomeBinding? = null
    private val binding get() = _binding!!

    /** 1-话费，2-水费，3-电费，4-网费 */
    var module = 0

    /** 金额 */
    var price = ""

    /** 账号 */
    var account = ""

    /** 已选择购物券id */
    var alreadySelectedCouponId = 0

    val titles =
        arrayOf(StringUtils.getString(R.string.available), StringUtils.getString(R.string.unavailable))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        FixedHeightBottomSheetDialog(requireContext(), theme, ScreenUtils.getScreenHeight() * 8/ 10)

    override fun getLayoutId() = R.layout.fragment_order_coupon_home

    override fun initView(parent: View) {
        _binding = FragmentOrderCouponHomeBinding.bind(requireNotNull(view))
        binding.pager.apply {
            offscreenPageLimit = titles.size
            adapter = InnerPagerAdapter(childFragmentManager, 0)
        }
        binding.tabs.setViewPager(binding.pager)
    }

    override fun initData() {
        presenter = OrderCouponHomePresenter(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun bindListener() {
        binding.closeIv.setOnClickListener {
            dismiss()
        }
    }

    private inner class InnerPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

        override fun getPageTitle(position: Int): CharSequence = titles[position]

        override fun getItem(position: Int): Fragment {

            val apply = BillCouponChild().apply {
                listType = position + 1
                selectedCouponId = alreadySelectedCouponId
                module = this@BillCouponHome.module
                price = this@BillCouponHome.price
                account = this@BillCouponHome.account
            }
            return apply

        }

        override fun getCount(): Int = titles.size
    }

    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        when (baseEvent.code) {
            EventCode.EVENT_SELECTED_PLATFORM_COUPON -> dismiss()
            else -> {}
        }


    }

}
