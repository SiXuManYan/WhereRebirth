package com.jiechengsheng.city.features.mall.second

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.MallCategory
import com.jiechengsheng.city.api.response.mall.MallGood
import com.jiechengsheng.city.api.response.mall.request.MallGoodListRequest
import com.jiechengsheng.city.api.response.mall.request.SortEnum
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.base.mvp.FixedHeightBottomSheetDialog
import com.jiechengsheng.city.features.mall.cart.MallCartActivity
import com.jiechengsheng.city.features.mall.home.child.MallRecommendAdapter
import com.jiechengsheng.city.features.search.SearchAllActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import com.jiechengsheng.city.databinding.ActivityMallSecondBinding
/**
 * Created by Wangsw  2021/12/3 15:30.
 * 閺傛壆澧楅崯鍡楃厔娴滃瞼楠囬崚妤勩€?
 */
class MallSecondActivity : BaseMvpActivity<MallSecondPresenter>(), MallSecondView {

    private lateinit var binding: ActivityMallSecondBinding


    /** 瑜版挸澧犳い鐢告桨鐎电懓绨查惃鍕癌缁狙冨瀻缁?*/
    private var categoryId = 0
    private var goodRequest = MallGoodListRequest()
    private var page = Constant.DEFAULT_FIRST_PAGE

    /** 閸熷棗鎼ч崚妤勩€?*/
    private lateinit var mAdapter: MallRecommendAdapter


    lateinit var mThirdCategoryAdapter: ThirdCategoryFilterAdapter

    companion object {

        fun navigation(context: Context, categoryId: Int) {
            val bundle = Bundle().apply {
                putInt(Constant.PARAM_CATEGORY_ID, categoryId)
            }
            val intent = Intent(context, MallSecondActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var emptyView: EmptyView

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_mall_second

    override fun initView() {
        binding = ActivityMallSecondBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        categoryId = intent.getIntExtra(Constant.PARAM_CATEGORY_ID, 0)
        initCategoryFilter()
        initContent()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initCategoryFilter() {

        mThirdCategoryAdapter = ThirdCategoryFilterAdapter().apply {
            setOnItemClickListener { _, _, position ->
                // 缁涙盯鈧?
                val datas = mThirdCategoryAdapter.data
                datas.forEach {
                    it.nativeIsSelected = false
                }
                notifyDataSetChanged()

                val category = data[position]
                category.nativeIsSelected = true
                notifyItemChanged(position)
                goodRequest.apply {
                    page = Constant.DEFAULT_FIRST_PAGE
                    categoryId = category.id
                    presenter.getMallList(goodRequest)
                }
            }
        }
        binding.categoryRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mThirdCategoryAdapter
        }
    }

    private fun initContent() {

        emptyView = EmptyView(this)
        emptyView.showEmptyDefault()
        addEmptyList(emptyView)

        mAdapter = MallRecommendAdapter().apply {
            setEmptyView(emptyView)
            loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            loadMoreModule.setOnLoadMoreListener {
                page++
                goodRequest.page = page
                presenter.getMallList(goodRequest)
            }
        }
        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val decoration = DividerDecoration(ColorUtils.getColor(R.color.transplant), SizeUtils.dp2px(10f), 0, 0)
        binding.contentRv.apply {
            adapter = mAdapter
            layoutManager = gridLayoutManager
            addItemDecoration(decoration)
        }
        binding.swipeLayout.setOnRefreshListener {
            goodRequest.page = Constant.DEFAULT_FIRST_PAGE
            presenter.getMallList(goodRequest)
        }
    }

    override fun initData() {
        presenter = MallSecondPresenter(this)
        presenter.getThirdCategory(categoryId)
    }

    override fun bindListener() {
        binding.cartIv.setOnClickListener {
            startActivityAfterLogin(MallCartActivity::class.java)
        }

        binding.mallFilter.dismissView.setOnClickListener {
            binding.mallFilter.filterContainerLl.visibility = View.GONE
            if (binding.salesTv.isChecked) {
                binding.complexTv.isChecked = false
            }
        }

        binding.complexLl.setOnClickListener {
            if (binding.mallFilter.filterContainerLl.visibility == View.VISIBLE) {
                binding.mallFilter.filterContainerLl.visibility = View.GONE
                binding.complexTv.isChecked = false
                binding.complexIv.rotation = 0f
            } else {
                binding.mallFilter.filterContainerLl.visibility = View.VISIBLE
                binding.complexTv.isChecked = true
                binding.complexIv.rotation = 180f
            }
            if (binding.mallFilter.filterComplexCtv.isChecked || binding.mallFilter.filterPriceDownCtv.isChecked || binding.mallFilter.filterPriceUpCtv.isChecked) {
                binding.complexTv.isChecked = true
            }

        }

        // 缂佺厧鎮?
        binding.mallFilter.filterComplexCtv.setOnClickListener {
            binding.salesTv.isChecked = false

            goodRequest.apply {
                page = Constant.DEFAULT_FIRST_PAGE
                order = null
                sold = null
            }
            presenter.getMallList(goodRequest)

            binding.complexTv.text = binding.mallFilter.filterComplexCtv.text
            binding.mallFilter.filterContainerLl.visibility = View.GONE

            binding.mallFilter.filterComplexCtv.isChecked = true
            binding.mallFilter.filterPriceDownCtv.isChecked = false
            binding.mallFilter.filterPriceUpCtv.isChecked = false
            binding.complexTv.isChecked = true

        }

        // 娴犻攱鐗搁梽宥呯碍
        binding.mallFilter.filterPriceDownCtv.setOnClickListener {
            binding.salesTv.isChecked = false
            goodRequest.apply {
                page = Constant.DEFAULT_FIRST_PAGE
                order = SortEnum.desc
                sold = null
            }
            presenter.getMallList(goodRequest)

            binding.complexTv.text = binding.mallFilter.filterPriceDownCtv.text
            binding.mallFilter.filterContainerLl.visibility = View.GONE
            binding.mallFilter.filterComplexCtv.isChecked = false
            binding.mallFilter.filterPriceDownCtv.isChecked = true
            binding.mallFilter.filterPriceUpCtv.isChecked = false
            binding.complexTv.isChecked = true
        }

        // 娴犻攱鐗搁崡鍥х碍
        binding.mallFilter.filterPriceUpCtv.setOnClickListener {
            binding.salesTv.isChecked = false
            goodRequest.apply {
                page = Constant.DEFAULT_FIRST_PAGE
                order = SortEnum.asc
                sold = null
            }
            presenter.getMallList(goodRequest)

            binding.complexTv.text = binding.mallFilter.filterPriceUpCtv.text
            binding.mallFilter.filterContainerLl.visibility = View.GONE
            binding.mallFilter.filterComplexCtv.isChecked = false
            binding.mallFilter.filterPriceDownCtv.isChecked = false
            binding.mallFilter.filterPriceUpCtv.isChecked = true
            binding.complexTv.isChecked = true
        }

        // 闁库偓闁?
        binding.salesTv.setOnClickListener {
            binding.complexTv.isChecked = false
            binding.mallFilter.filterContainerLl.visibility = View.GONE
            binding.salesTv.isChecked = !binding.salesTv.isChecked
            goodRequest.apply {
                page = Constant.DEFAULT_FIRST_PAGE
                order = null
                sold = if (binding.salesTv.isChecked) {
                    SortEnum.desc
                } else {
                    null
                }
            }
            presenter.getMallList(goodRequest)
        }

        binding.otherFilterLl.setOnClickListener {
            binding.mallFilter.filterContainerLl.visibility = View.GONE
            showOtherFilterDialog()
        }

        binding.searchLl.setOnClickListener {
            startActivity(SearchAllActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_TYPE, 6)
                // 闁俺绻?娑撳楠囬崚鍡欒鐠哄疇娴嗛幖婊呭偍閿涘矁顩﹂崥灞炬缁?缁狙冨瀻缁?
                putString(Constant.PARAM_CATEGORY_ID, categoryId.toString())
            })
        }
    }

    override fun bindThirdCategory(response: ArrayList<MallCategory>) {
        mThirdCategoryAdapter.setNewInstance(response)
        if (response.isNotEmpty()) {
            goodRequest.apply {
                categoryId = response[0].id
            }
            presenter.getMallList(goodRequest)
        }

    }

    override fun bindData(data: MutableList<MallGood>, lastPage: Boolean) {
        binding.swipeLayout.isRefreshing = false
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


    private lateinit var filterDialog: FixedHeightBottomSheetDialog

    private fun showOtherFilterDialog() {

        if (!::filterDialog.isInitialized) {
            filterDialog = FixedHeightBottomSheetDialog(this, R.style.bottom_sheet_edit, ScreenUtils.getScreenHeight() * 70 / 100)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_mall_other_filter, null)
            filterDialog.setContentView(view)

            try {
                val parent = view.parent as ViewGroup
                parent.setBackgroundResource(android.R.color.transparent)
            } catch (e: Exception) {
            }

            val min_et = view.findViewById<AppCompatEditText>(R.id.min_et)
            val max_et = view.findViewById<AppCompatEditText>(R.id.max_et)

            view.findViewById<ImageView>(R.id.close_iv).setOnClickListener {
                filterDialog.dismiss()
            }

            view.findViewById<TextView>(R.id.confirm_tv).setOnClickListener {
                if (!min_et.text.isNullOrBlank() && !max_et.text.isNullOrBlank()) {
                    goodRequest.apply {
                        page = Constant.DEFAULT_FIRST_PAGE
                        startPrice = min_et.text.toString()
                        endPrice = max_et.text.toString()
                    }
                    presenter.getMallList(goodRequest)
                }
                filterDialog.dismiss()
            }

            view.findViewById<TextView>(R.id.reset_tv).setOnClickListener {
                goodRequest.apply {
                    page = Constant.DEFAULT_FIRST_PAGE
                    startPrice = null
                    endPrice = null
                }
                presenter.getMallList(goodRequest)
                filterDialog.dismiss()
            }
        }
        filterDialog.show()
    }


}