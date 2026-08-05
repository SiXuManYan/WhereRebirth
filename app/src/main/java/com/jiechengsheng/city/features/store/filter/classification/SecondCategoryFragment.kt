package com.jiechengsheng.city.features.store.filter.classification

import android.os.Bundle
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.category.Category
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import com.jiechengsheng.city.utils.Constant
import org.greenrobot.eventbus.EventBus
import java.util.*
import com.jiechengsheng.city.databinding.SingleRecyclerViewBinding

/**
 * Created by Wangsw  2021/6/9 15:04.
 * 二级分类
 */
class SecondCategoryFragment : BaseMvpFragment<SecondCategoryPresenter>(), SecondCategoryView, OnItemClickListener {

    private var _binding: SingleRecyclerViewBinding? = null
    private val binding get() = _binding!!

    private var pid = 0
    lateinit var mAdapter: SecondCategoryAdapter

    companion object {

        /**
         * @param pid 一级分类
         */
        fun newInstance(pid: Int): SecondCategoryFragment {
            val fragment = SecondCategoryFragment()
            val bundle = Bundle().apply {
                putInt(Constant.PARAM_PID, pid)
            }
            fragment.arguments = bundle
            return fragment
        }

    }



    override fun getLayoutId() = R.layout.single_recycler_view

    override fun initView(view: View) {
        _binding = SingleRecyclerViewBinding.bind(requireNotNull(view))
        arguments?.let {
            pid = it.getInt(Constant.PARAM_PID, 0)
        }

        mAdapter = SecondCategoryAdapter()
        mAdapter.setOnItemClickListener(this@SecondCategoryFragment)
        binding.contentRv.adapter = mAdapter
    }

    override fun initData() {
        presenter = SecondCategoryPresenter(this)
        presenter.getSecondCategory(pid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun bindListener() = Unit

    override fun bindData(response: ArrayList<Category>) = mAdapter.setNewInstance(response)

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        mAdapter.data .forEachIndexed { index, category ->
            category.nativeIsSelected = index == position
        }
        mAdapter.notifyDataSetChanged()
        val category = mAdapter.data[position]
        EventBus.getDefault().post(BaseEvent(category))
    }

}