package com.jiechengsheng.city.features.category.edit

import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.*
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.ErrorResponse
import com.jiechengsheng.city.api.response.category.Category
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.category.CategoryAdapter2
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import org.greenrobot.eventbus.EventBus
import java.util.*
import com.jiechengsheng.city.databinding.ActivityCategoryEditBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/4/15 18:02.
 * 分类编辑页
 */
class CategoryEditActivity : BaseMvpActivity<CategoryEditPresenter>(), CategoryEditView {

    private lateinit var binding: ActivityCategoryEditBinding


    private lateinit var mSelectedAdapter: CategoryAdapter2
    private lateinit var mUnSelectedAdapter: CategoryAdapter2

    /**
     * 用户在此页面新增的关注的分类
     */
    var newAddCategory = ArrayList<Category>()

    /**
     * 用户在此页面删除的分类
     */
    var newCutCategory = ArrayList<Category>()


    override fun getLayoutId() = R.layout.activity_category_edit


    override fun initView() {
        binding = ActivityCategoryEditBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.blue_377BFF))

        binding.swipeLayout.setOnRefreshListener {
            presenter.getEditableCategory()
        }

        mSelectedAdapter = CategoryAdapter2().apply {
            setEmptyView(EmptyView(this@CategoryEditActivity).apply {
                showEmptyNothing()
                addEmptyList(this)
            })
            showEditButton(true)
            addChildClickViewIds(R.id.cut_iv)

            setOnItemChildClickListener { _, _, position ->
                VibrateUtils.vibrate(20)
                val category = this.data[position]

                category.follow_status = false

                // 本地存储
                newAddCategory.remove(category)
                newCutCategory.add(category)

                // 未关注列表增加这条数据
                mUnSelectedAdapter.addData(category)

                this.removeAt(position)
            }

        }
        mUnSelectedAdapter = CategoryAdapter2().apply {
            setEmptyView(EmptyView(this@CategoryEditActivity).apply {
                showEmptyNothing()
                addEmptyList(this)
            })
            showEditButton(true)
            addChildClickViewIds(R.id.add_iv)

            setOnItemChildClickListener { _, _, position ->
                VibrateUtils.vibrate(20)

                val category = this.data[position]
                category.follow_status = true

                // 本地存储
                newAddCategory.add(category)
                newCutCategory.remove(category)

                // 已关注列表增加这条数据
                mSelectedAdapter.addData(category)

                this.removeAt(position)
            }
        }

        binding.selectedRv.apply {
            adapter = mSelectedAdapter
            addItemDecoration(getItemDecoration())
            isNestedScrollingEnabled = true
            layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }

        binding.unselectedRv.apply {
            adapter = mUnSelectedAdapter
            addItemDecoration(getItemDecoration())
            isNestedScrollingEnabled = true
            layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }
    }


    override fun isStatusDark(): Boolean = false


    override fun initData() {
        presenter = CategoryEditPresenter(this)
        presenter.getEditableCategory()
    }

    override fun bindListener() {
        binding.closeTv.setOnClickListener {
            VibrateUtils.vibrate(20)
            finish()
        }
        binding.finishTv.setOnClickListener {
            VibrateUtils.vibrate(20)
            presenter.followCategory(mSelectedAdapter.data)
        }
    }

    private fun getItemDecoration(): DividerDecoration {
        return DividerDecoration(ColorUtils.getColor(R.color.white), SizeUtils.dp2px(5f), 0, 0).apply {
            setDrawHeaderFooter(false)
        }
    }

    override fun bindFollowData(follow: ArrayList<Category>){
        mSelectedAdapter.setNewInstance(follow)
        binding.swipeLayout.isRefreshing = false
    }


    override fun bindUnFollowData(unFollow: ArrayList<Category>) {
        mUnSelectedAdapter.setNewInstance(unFollow)
        binding.swipeLayout.isRefreshing = false
    }

    override fun followSuccess() {
        ToastUtils.showShort("Successful operation")

        if (newAddCategory.isNotEmpty()) {
            EventBus.getDefault().post(BaseEvent<ArrayList<Category>>(EventCode.EVENT_ADD_NEW_CATEGORY, newAddCategory))
        }
        if (newCutCategory.isNotEmpty()) {
            EventBus.getDefault().post(BaseEvent<ArrayList<Category>>(EventCode.EVENT_CUT_NEW_CATEGORY, newCutCategory))
        }
        finish()
    }

    override fun onError(errorResponse: ErrorResponse?) {
        binding.swipeLayout.isRefreshing = false
        super.onError(errorResponse)
    }

}