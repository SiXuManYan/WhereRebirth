package com.jiechengsheng.city.features.mall.home.child

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.appbar.AppBarLayout
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.BannerResponse
import com.jiechengsheng.city.api.response.mall.MallBannerCategory
import com.jiechengsheng.city.api.response.mall.MallCategory
import com.jiechengsheng.city.api.response.mall.MallGood
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.view.XBanner.AbstractUrlLoader
import com.jiechengsheng.city.view.XBanner.XBanner
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import pl.droidsonroids.gif.GifImageView
import com.jiechengsheng.city.databinding.FragmentMallHomeChildBinding

/**
 * Created by Wangsw  2021/11/30 17:01.
 * 商城首页商品
 */
class MallHomeChildFragment : BaseMvpFragment<MallHomeChildPresenter>(), MallHomeChildView {

    private var _binding: FragmentMallHomeChildBinding? = null
    private val binding get() = _binding!!

    /** 当前页面对应的一级分类 */
    lateinit var targetFirstCategory: MallCategory

    /** 二级分类轮播 */
    private lateinit var mBannerAdapter: MallHomeChildBannerAdapter

    /** 商品推荐 */
    private lateinit var mAdapter: MallRecommendAdapter

    private lateinit var emptyView: EmptyView

    private var page = Constant.DEFAULT_FIRST_PAGE

    override fun getLayoutId() = R.layout.fragment_mall_home_child

    override fun initView(view: View) {
        _binding = FragmentMallHomeChildBinding.bind(requireNotNull(view))
        initAd()
        initBanner()
        initContent()
    }

    private fun initAd() {
//        val bannerParams = binding.llBanner.layoutParams.apply {
//            height = ScreenUtils.getScreenWidth() * 194 / 345
//        }
//        binding.llBanner.layoutParams = bannerParams

        binding.topBanner.setBannerTypes(XBanner.CIRCLE_INDICATOR)
            .setTitleHeight(50)
            .isAutoPlay(true)
            .setDelay(5000)
            .setUpIndicators(R.drawable.ic_selected, R.drawable.ic_unselected)
            .setUpIndicatorSize(6, 6)
            .setIndicatorGravity(XBanner.INDICATOR_CENTER)
            .setImageLoader(object : AbstractUrlLoader() {
                override fun loadImages(context: Context, url: String, image: ImageView) {
                    GlideUtil.load(context, url, image, 4)
                }

                override fun loadGifs(context: Context, url: String, gifImageView: GifImageView, scaleType: ImageView.ScaleType) {
                    GlideUtil.load(context, url, gifImageView, 4)
                }
            })
    }


    private fun initBanner() {
        if (!::targetFirstCategory.isInitialized) return

        binding.pointView.apply {
            selectedDrawableResId = R.drawable.shape_point_selected_377bff
            commonDrawableResId = R.drawable.shape_point_selected_d8d8d8
        }

        mBannerAdapter = MallHomeChildBannerAdapter().apply {
            setEmptyView(R.layout.view_empty_data_brvah_default)
        }
        binding.bannerRv.apply {
            adapter = mBannerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(binding.bannerRv)

        binding.bannerRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val firstItemPosition: Int = layoutManager.findFirstVisibleItemPosition()
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.pointView.onPageSelected(firstItemPosition)
                    }
                }
            }
        })


    }

    private fun initContent() {

        emptyView = EmptyView(requireContext())
        emptyView.showEmptyDefault()
        addEmptyList(emptyView)

        mAdapter = MallRecommendAdapter().apply {
            setEmptyView(emptyView)
            loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            loadMoreModule.setOnLoadMoreListener {
                page++
                presenter.getRecommend(targetFirstCategory.id, page)
            }
        }
        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val decoration = DividerDecoration(ColorUtils.getColor(R.color.transplant), SizeUtils.dp2px(10f), 0, 0)
        binding.contentRv.apply {
            adapter = mAdapter
            layoutManager = gridLayoutManager
            addItemDecoration(decoration)
        }
    }

    override fun initData() {
        presenter = MallHomeChildPresenter(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    


    override fun loadOnVisible() {
        if (!::targetFirstCategory.isInitialized) return

        page = Constant.DEFAULT_FIRST_PAGE
        presenter.handleBanner(targetFirstCategory)
        presenter.getRecommend(targetFirstCategory.id, page)
        presenter.getTopBanner()
    }


    override fun bindListener() {

        binding.swipeLayout?.setOnRefreshListener {
            loadOnVisible()
        }

        binding.topAbl.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            binding.swipeLayout?.isEnabled = verticalOffset >= 0
        })
    }

    override fun bindBannerData(result: ArrayList<MallBannerCategory>) {
        binding.swipeLayout?.isRefreshing = false
        mBannerAdapter.setNewInstance(result)
        binding.pointView.setPointCount(result.size)
    }

    override fun bindRecommend(data: MutableList<MallGood>, lastPage: Boolean) {
        binding.swipeLayout?.isRefreshing = false
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

    override fun bindTopBannerData(bannerUrls: ArrayList<String>, response: ArrayList<BannerResponse>) {
        binding.swipeLayout?.isRefreshing = false
        if (bannerUrls.size <= 1) {
            binding.topBanner.setUpIndicatorSize(0, 0)
        } else {
            binding.topBanner.setUpIndicatorSize(6, 6)
        }
        binding.topBanner.setImageUrls(bannerUrls)
        binding.topBanner.setBannerPageListener(object : XBanner.BannerPageListener {

            override fun onBannerDragging(item: Int) = Unit

            override fun onBannerIdle(item: Int) = Unit

            override fun onBannerClick(item: Int) {
                val data = response[item]
                BusinessUtils.handleBannerClick(context, data)
            }

        }).start()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isViewCreated) {
            return
        }
        if (isVisibleToUser) {
            binding.topBanner.start()
        } else {
            binding.topBanner.pause()
        }
    }

    override fun onPause() {
        binding.topBanner.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.topBanner?.releaseBanner()
        super.onDestroy()
    }


}