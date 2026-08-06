package com.jiechengsheng.city.features.daily.scoe

import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.IntegralDetailResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.daily.sign.SignInRuleActivity
import com.jiechengsheng.city.features.integral.child.detail.IntegralChildAdapter
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import com.jiechengsheng.city.databinding.ActivityScoreBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/11/25 18:25.
 * 我的积分
 */
class ScoreActivity : BaseMvpActivity<ScorePresenter>(), ScoreView {

    private lateinit var binding: ActivityScoreBinding

    private val ruleTv: android.widget.CheckedTextView
        get() = findViewById(R.id.rule_tv)

    private var isToolbarDark = false

    private var page = Constant.DEFAULT_FIRST_PAGE

    private lateinit var emptyView: EmptyView

    private lateinit var mAdapter: IntegralChildAdapter

    override fun isStatusDark() = isToolbarDark

    override fun getLayoutId() = R.layout.activity_score

    override fun initView() {
        binding = ActivityScoreBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        initScroll()
    }


    override fun initData() {
        presenter = ScorePresenter(this)
        presenter.getUserInfo()
        presenter.getIntegralDetailList(page)
    }

    override fun bindListener() {
        binding.ruleTv.setOnClickListener {
            startActivity(SignInRuleActivity::class.java)
        }
    }

    private fun initScroll() {
        emptyView = EmptyView(this).apply {
            setEmptyImage(R.mipmap.ic_empty_points)
            setEmptyHint(R.string.empty_points)
            addEmptyList(this)
        }

        mAdapter = IntegralChildAdapter().apply {
            setEmptyView(R.layout.view_empty_data_brvah_default)
            loadMoreModule.isAutoLoadMore = true
            loadMoreModule.isEnableLoadMoreIfNotFullPage = true
            loadMoreModule.setOnLoadMoreListener {
                page++
                presenter.getIntegralDetailList(page)
            }
        }
        binding.contentRv.apply {
            isNestedScrollingEnabled = true
            adapter = mAdapter
            addItemDecoration(DividerDecoration(getColor(R.color.colorPrimary), 1, SizeUtils.dp2px(15f), 0))
            layoutManager = object : LinearLayoutManager(this@ScoreActivity, VERTICAL, false) {
                override fun canScrollVertically() = false
            }
        }

        binding.useView.setBackgroundColor(getColor(R.color.white))
        binding.toolbar.setBackgroundColor(getColor(R.color.white))
        binding.useView.background.alpha = 0
        binding.toolbar.background.alpha = 0
        binding.scrollView.setOnScrollChangeListener { _, _, y, _, _ ->
            val headHeight = binding.mediaFl.measuredHeight - binding.toolbar.measuredHeight
            var alpha = (y.toFloat() / headHeight * 255).toInt()
            if (alpha >= 255) {
                alpha = 255
            }
            if (alpha <= 5) {
                alpha = 0
            }
            isToolbarDark = alpha > 130

            if (isToolbarDark) {
                binding.backIv.setImageResource(R.mipmap.ic_back_black)
            } else {
                binding.backIv.setImageResource(R.mipmap.ic_back_white)
            }
            binding.scoreTitleTv.isChecked = isToolbarDark
            binding.ruleTv.isChecked = isToolbarDark
            binding.useView.background.alpha = alpha
            binding.toolbar.background.alpha = alpha

            changeStatusTextColor()
        }

    }

    override fun bindUserIntegral(integral: String) {
        binding.myScoreTv.text = integral
    }

    override fun bindDetailData(data: List<IntegralDetailResponse>, lastPage: Boolean) {

        val loadMoreModule = mAdapter.loadMoreModule
        if (data.isEmpty()) {
            if (page == Constant.DEFAULT_FIRST_PAGE) {
                loadMoreModule.loadMoreComplete()
                emptyView.showEmptyContainer()
            } else {
                loadMoreModule.loadMoreEnd()
            }
            return
        }
        if (page == Constant.DEFAULT_FIRST_PAGE) {
            mAdapter.setNewInstance(data.toMutableList())
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

}
