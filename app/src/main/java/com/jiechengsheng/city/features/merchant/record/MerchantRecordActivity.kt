package com.jiechengsheng.city.features.merchant.record

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.ColorUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.network.BaseMvpObserver
import com.jiechengsheng.city.api.network.BaseMvpPresenter
import com.jiechengsheng.city.api.network.BaseMvpView
import com.jiechengsheng.city.api.response.PageResponse
import com.jiechengsheng.city.api.response.merchant.MerchantApplyRecord
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.databinding.ActivityRefreshListBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/1/11 15:02.
 * 申请记录
 */
class MerchantRecordActivity :BaseMvpActivity<MerchantRecordPresenter>(),MerchantRecordView, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityRefreshListBinding

    private var page = Constant.DEFAULT_FIRST_PAGE
    private lateinit var emptyView: EmptyView
    private lateinit var mAdapter: MerchantRecordAdapter

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_refresh_list

    override fun initView() {
        binding = ActivityRefreshListBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        mJcsTitle.setMiddleTitle(getString(R.string.apply_record))
        binding.swipeLayout.apply {
            setOnRefreshListener(this@MerchantRecordActivity)
            setColorSchemeColors(ColorUtils.getColor(R.color.blue_4C9EF2))
        }
        emptyView = EmptyView(this).apply {
            showEmptyDefault()
            addEmptyList(this)
        }
        mAdapter = MerchantRecordAdapter().apply {
            setEmptyView(emptyView)
        }
        binding.recycler.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun initData() {
        presenter = MerchantRecordPresenter(this)

        onRefresh()
    }

    override fun bindListener() {


    }

    override fun bindData(data: MutableList<MerchantApplyRecord>, lastPage: Boolean) {
        if (binding.swipeLayout.isRefreshing) {
            binding.swipeLayout.isRefreshing = false
        }

        val loadMoreModule = mAdapter.loadMoreModule
        if (data.isEmpty()) {
            if (page == Constant.DEFAULT_FIRST_PAGE) {
                mAdapter.setNewInstance(null)
                loadMoreModule.loadMoreComplete()
                emptyView.showEmptyContainer()
            } else {
                loadMoreModule.loadMoreEnd()
            }
            return
        }
        if (page == Constant.DEFAULT_FIRST_PAGE) {
            mAdapter.setNewInstance(data)
            loadMoreModule.checkDisableLoadMoreIfNotFullPage()
        } else {
            mAdapter.addData(data)
            if (lastPage) {
                loadMoreModule.loadMoreEnd()
            } else {
                loadMoreModule.loadMoreComplete()
            }
        }
    }

    override fun onRefresh() {
        page = Constant.DEFAULT_FIRST_PAGE
        presenter.getData(page)
    }

}


interface MerchantRecordView:BaseMvpView{
    fun bindData(toMutableList: MutableList<MerchantApplyRecord>, lastPage: Boolean)

}

class MerchantRecordPresenter(private var view:MerchantRecordView):BaseMvpPresenter(view){

    fun getData(page: Int) {
        requestApi(mRetrofit.merchantApplyRecord(page), object : BaseMvpObserver<PageResponse<MerchantApplyRecord>>(view,page){
            override fun onSuccess(response: PageResponse<MerchantApplyRecord>) {
                val isLastPage = response.lastPage == page
                val data = response.data

                view.bindData(data.toMutableList(), isLastPage)
            }
        })
    }
}