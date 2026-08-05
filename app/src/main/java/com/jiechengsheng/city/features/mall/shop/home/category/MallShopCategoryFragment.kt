package com.jiechengsheng.city.features.mall.shop.home.category

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.MallShopCategory
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.widget.list.DividerDecoration
import com.jiechengsheng.city.databinding.FragmentRefreshListBinding
/**
 * Created by Wangsw  2022/1/24 10:31.
 * 店铺详情分类
 */
class MallShopCategoryFragment : BaseMvpFragment<MallShopCategoryPresenter>(), MallShopCategoryView {

    private var _binding: FragmentRefreshListBinding? = null
    private val binding get() = _binding!!

    private var mShopId = 0

    private lateinit var mAdapter: MallShopCategoryAdapter


    companion object {

        fun newInstance(shopId: Int): MallShopCategoryFragment {
            val fragment = MallShopCategoryFragment()

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_SHOP_ID, shopId)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_refresh_list

    override fun initView(view: View) {
        _binding = FragmentRefreshListBinding.bind(requireNotNull(view))

        arguments?.let {
            mShopId = it.getInt(Constant.PARAM_SHOP_ID)
        }

        mAdapter = MallShopCategoryAdapter()
        binding.recycler.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.color_F0F0F0), 1, 0, 0))
        }
    }

    override fun initData() {
        presenter = MallShopCategoryPresenter(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun loadOnVisible() {
        presenter.getCategory(mShopId)
    }

    override fun bindListener() {
        binding.swipeLayout.setOnClickListener {
            presenter.getCategory(mShopId)
        }
    }

    override fun bindCategory(response: ArrayList<MallShopCategory>) {
        binding.swipeLayout.isRefreshing = false
        mAdapter.setNewInstance(response)
    }
}