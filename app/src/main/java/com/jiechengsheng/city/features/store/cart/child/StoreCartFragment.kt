package com.jiechengsheng.city.features.store.cart.child

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.*
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.store.StoreGoodsCommit
import com.jiechengsheng.city.api.response.store.StoreOrderCommitData
import com.jiechengsheng.city.api.response.store.cart.StoreCartGroup
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import com.jiechengsheng.city.features.store.order.StoreOrderCommitActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.databinding.FragemntStoreCartBinding
/**
 * Created by Wangsw  2021/7/5 13:40.
 * 购物车
 */
class StoreCartFragment : BaseMvpFragment<StoreCartPresenter>(), StoreCartView,
    SwipeRefreshLayout.OnRefreshListener,
    StoreCartValueChangeListener, OnChildSelectClick, OnGroupSelectClick {

    private var _binding: FragemntStoreCartBinding? = null
    private val binding get() = _binding!!

    /**
     *  列表类型（0：自提，1：商家配送）
     */
    private var listType = 0


    private var isEditMode = false

    private lateinit var mAdapter: StoreCartAdapter
    private lateinit var emptyView: EmptyView

    companion object {

        /**
         * 美食评论
         * @param listType  列表类型（0：自提，1：商家配送）
         */
        fun newInstance(listType: Int): StoreCartFragment {
            val fragment = StoreCartFragment()
            val bundle = Bundle().apply {
                putInt(Constant.PARAM_TYPE, listType)
            }
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun getLayoutId() = R.layout.fragemnt_store_cart

    override fun initView(view: View?) {
        _binding = FragemntStoreCartBinding.bind(requireNotNull(view))
        arguments?.let {
            listType = it.getInt(Constant.PARAM_TYPE, 0)
        }

        binding.swipeLayout.setOnRefreshListener(this)
        binding.swipeLayout.setColorSchemeColors(ColorUtils.getColor(R.color.blue_4C9EF2))
        emptyView = EmptyView(context).apply {
            showEmptyDefault()
        }
        mAdapter = StoreCartAdapter().apply {
            setEmptyView(emptyView)
            numberChangeListener = this@StoreCartFragment
            onChildSelectClick = this@StoreCartFragment
            onGroupSelectClick = this@StoreCartFragment

        }
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

    }


    override fun initData() {
        presenter = StoreCartPresenter(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun loadOnVisible() {
        presenter.getStoreCart(listType)
    }

    override fun bindListener() {
        binding.selectAllTv.setOnClickListener {
            var isSelectAll = binding.selectAllTv.isChecked
            isSelectAll = !isSelectAll

            // data
            presenter.handleSelectAll(mAdapter, isSelectAll)

            // ui
            if (isSelectAll) {
                VibrateUtils.vibrate(10)
                val handlePrice = presenter.handlePrice(mAdapter)
                binding.totalPriceTv.text =
                    StringUtils.getString(R.string.price_unit_format, handlePrice.stripTrailingZeros().toPlainString())
//                select_count_tv.text = getString(R.string.total_format, presenter.getSelectedCount(mAdapter))
            } else {
                binding.totalPriceTv.text = getString(R.string.price_unit_format, "0.00")
//                select_count_tv.text = getString(R.string.total_format, 0)
            }

            binding.selectAllTv.isChecked = isSelectAll
        }


        binding.deleteTv.setOnClickListener {

            if (binding.selectAllTv.isChecked) {
                presenter.clearStoreCart()
                mAdapter.setNewInstance(null)
                emptyView.showEmptyContainer()
            } else {

                presenter.deleteCart(mAdapter)
            }
        }

        binding.settlementTv.setOnClickListener {

            val selectedCount = presenter.getSelectedCount(mAdapter)
            if (selectedCount <= 0) {
                ToastUtils.showShort(R.string.please_select_a_product)
                return@setOnClickListener
            }

            val selectedData = presenter.getSelectedData(mAdapter)

            val appList: ArrayList<StoreOrderCommitData> = ArrayList()
            selectedData.forEach {

                // shop
                val shop = StoreOrderCommitData().apply {
                    shop_id = it.shop_id
                    shop_title = it.shop_name
                    delivery_type = listType + 1
                    delivery_fee = it.delivery_fee.toFloat()
                }

                // shop $good
                it.goods.forEach { good ->
                    val goodData = good.good_data
                    val goodInfo = StoreGoodsCommit().apply {
                        good_id = goodData.id
                        delivery_type = listType + 1

                        if (goodData.images.isNotEmpty()) {
                            image = goodData.images[0]
                        }
                        goodName = goodData.title
                        good_num = good.good_num
                        price = goodData.price
                    }
                    shop.goods.add(goodInfo)
                }
                appList.add(shop)
            }

            startActivityAfterLogin(StoreOrderCommitActivity::class.java, Bundle().apply {
                putSerializable(Constant.PARAM_ORDER_COMMIT_DATA, appList)
            })

        }


    }

    override fun onRefresh() {
        loadOnVisible()
    }


    override fun bindList(data: ArrayList<StoreCartGroup>) {
        if (binding.swipeLayout.isRefreshing) {
            binding.swipeLayout.isRefreshing = false
        }

        if (data.isEmpty()) {
            mAdapter.setNewInstance(null)
            emptyView.showEmptyContainer()
            binding.selectAllTv.visibility = View.GONE
            return
        }
        mAdapter.setNewInstance(data)
        binding.selectAllTv.visibility = View.VISIBLE
        handleBottom()
    }

    override fun onChildNumberChange(cartId: Int, add: Boolean, number: Int) {
        val handlePrice = presenter.handlePrice(mAdapter)
        binding.totalPriceTv.text = StringUtils.getString(R.string.price_unit_format, handlePrice.stripTrailingZeros().toPlainString())

        presenter.changeCartNumber(cartId, add)
    }

    override fun onChildSelected(checked: Boolean) = handleBottom()

    override fun onGroupSelected(nativeIsSelect: Boolean) = handleBottom()

    private fun handleBottom() {
        // 总价格
        val handlePrice = presenter.handlePrice(mAdapter)
        binding.totalPriceTv.text = StringUtils.getString(R.string.price_unit_format, handlePrice.stripTrailingZeros().toPlainString())
        // 选中数量
//        select_count_tv.text = getString(R.string.total_format, presenter.getSelectedCount(mAdapter))
        // 是否全选
        binding.selectAllTv.isChecked = presenter.checkSelectAll(mAdapter)
    }


        override fun deleteSuccess() = onRefresh()

    override fun onEventReceived(baseEvent: BaseEvent<*>) {

        when (baseEvent.code) {

            EventCode.EVENT_STORE_CART_HANDLE -> {
                isEditMode = baseEvent.data as Boolean
                if (isEditMode) {
                    binding.bottomVs.displayedChild = 1
                    binding.totalPriceTv.visibility = View.GONE
                } else {
                    binding.bottomVs.displayedChild = 0
                    binding.totalPriceTv.visibility = View.VISIBLE
                }
            }
            EventCode.EVENT_REFRESH_ORDER_LIST -> {
                // 支付成功，手动调用接口删除
                presenter.deleteCart(mAdapter)
            }

            else -> {
            }
        }


    }
}