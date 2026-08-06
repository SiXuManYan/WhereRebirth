package com.jiechengsheng.city.features.job.collection

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.job.Job
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import com.jiechengsheng.city.features.job.home.JobHomeAdapter
import com.jiechengsheng.city.features.job.home.JobHomePresenter
import com.jiechengsheng.city.features.job.home.JobHomeView
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import com.jiechengsheng.city.databinding.FragmentJobCollectionBinding
/**
 * Created by Wangsw  2022/12/15 11:05.
 * 职位收藏
 */
class JobCollectionFragment : BaseMvpFragment<JobHomePresenter>(), JobHomeView, SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentJobCollectionBinding? = null
    private val binding get() = _binding!!

    private var page = Constant.DEFAULT_FIRST_PAGE
    private lateinit var mAdapter: JobHomeAdapter
    private lateinit var emptyView: EmptyView

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.fragment_job_collection
    override fun initView(view: View?) {
        _binding = FragmentJobCollectionBinding.bind(requireNotNull(view))
        initContent()
    }

    private fun initContent() {
        binding.swipeLayout.setOnRefreshListener(this)
        binding.swipeLayout.setColorSchemeColors(ColorUtils.getColor(R.color.color_1c1380))
        emptyView = EmptyView(requireContext()).apply {
            setEmptyImage(R.mipmap.ic_empty_job_collection)
            setEmptyHint(R.string.empty_data_default)
        }

        addEmptyList(emptyView)

        mAdapter = JobHomeAdapter().apply {
            setEmptyView(emptyView)
            loadMoreModule.isEnableLoadMoreIfNotFullPage = true
            loadMoreModule.setOnLoadMoreListener {
                page++
                presenter.getJobCollectionList(page)
            }
        }


        val manager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycler.apply {
            adapter = mAdapter
            layoutManager = manager
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.grey_F5F5F5), SizeUtils.dp2px(4f), 0, 0))
        }

    }

    override fun initData() {
        presenter = JobHomePresenter(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun loadOnVisible() {
        onRefresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isViewVisible) {
            onRefresh()
        }
    }

    override fun bindListener() {
        binding.backIv.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onRefresh() {
        if (!User.isLogon()) {
            return
        }
        page = Constant.DEFAULT_FIRST_PAGE
        presenter.getJobCollectionList(page)
    }

    override fun bindJobCollectionList(toMutableList: MutableList<Job>, lastPage: Boolean) {
        if (binding.swipeLayout.isRefreshing) {
            binding.swipeLayout.isRefreshing = false
        }
        val loadMoreModule = mAdapter.loadMoreModule
        if (toMutableList.isEmpty()) {
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
            mAdapter.setNewInstance(toMutableList)
            loadMoreModule.checkDisableLoadMoreIfNotFullPage()
        } else {
            mAdapter.addData(toMutableList)
            if (lastPage) {
                loadMoreModule.loadMoreEnd()
            } else {
                loadMoreModule.loadMoreComplete()
            }
        }
    }

    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        when (baseEvent.code) {
            EventCode.EVENT_LOGIN_SUCCESS -> {
                if (isViewCreated) {
                    onRefresh()
                }
            }
            else -> {}
        }
    }


}