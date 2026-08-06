package com.jiechengsheng.city.features.travel.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.travel.TravelDetail
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.comment.CommentPostActivity
import com.jiechengsheng.city.features.hotel.comment.child.HotelCommentAdapter
import com.jiechengsheng.city.features.hotel.detail.media.DetailMediaAdapter
import com.jiechengsheng.city.features.hotel.detail.media.MediaData
import com.jiechengsheng.city.features.travel.comment.TravelCommentActivity
import com.jiechengsheng.city.frames.common.Html5Url
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.jiechengsheng.city.utils.MobUtil
import com.jiechengsheng.city.view.empty.EmptyView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.jiechengsheng.city.databinding.ActivityTravelDetailBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/10/18 16:16.
 *  旅游详情
 */
class TravelDetailActivity : BaseMvpActivity<TravelDetailPresenter>(), TravelDetailView {

    private lateinit var binding: ActivityTravelDetailBinding


    private var travelId = 0

    private var isToolbarDark = false

    /** 是否收藏  收藏状态（1：未收藏，2：已收藏） */
    private var collect_status = 1

    private var businessPhone = ""
    private var webUrl = ""
    private var facebook = ""
    private var mLat = 0.0
    private var mLng = 0.0

    /** 景点名称 */
    private var name = ""

    /** 景点图片 */
    private var image = ""

    /** 轮播 */
    private lateinit var mMediaAdapter: DetailMediaAdapter


    private lateinit var emptyView: EmptyView

    /** 评价 */
    private lateinit var mCommentAdapter: HotelCommentAdapter

    companion object {

        fun navigation(context: Context, travelId: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ID, travelId)
            }
            val intent = Intent(context, TravelDetailActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun isStatusDark() = isToolbarDark

    override fun getLayoutId() = R.layout.activity_travel_detail

    override fun initView() {
        binding = ActivityTravelDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
        initScroll()
        initMedia()
    }


    private fun initExtra() {
        val bundle = intent.extras ?: return
        bundle.apply {
            travelId = getInt(Constant.PARAM_ID)
        }

    }


    private fun initScroll() {
        // alpha
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
            setLikeImage()
            setBackImage()
            setShareImage()

            binding.useView.background.alpha = alpha
            binding.toolbar.background.alpha = alpha

            changeStatusTextColor()
        }


        emptyView = EmptyView(this).apply {
            empty_iv.visibility = View.GONE
            parent_ll.layoutParams.height = SizeUtils.dp2px(92f)
            setEmptyHint(R.string.no_content)
        }

        mCommentAdapter = HotelCommentAdapter().apply {
            setEmptyView(emptyView)
        }
        binding.commentRv.layoutManager = object : LinearLayoutManager(this, VERTICAL, false) {
            override fun canScrollVertically(): Boolean = false
        }
        binding.commentRv.adapter = mCommentAdapter


    }

    /**
     * 处理轮播
     */
    private fun initMedia() {
        mMediaAdapter = DetailMediaAdapter()
        PagerSnapHelper().attachToRecyclerView(binding.mediaRv)

        binding.mediaRv.apply {
            layoutManager = LinearLayoutManager(this@TravelDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mMediaAdapter
        }
        binding.mediaRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                val linearManager = recyclerView.layoutManager as LinearLayoutManager
                val firstItemPosition: Int = linearManager.findFirstVisibleItemPosition()
                val lastItemPosition: Int = linearManager.findLastVisibleItemPosition()

                //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) {

                    //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition

                    // 对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag == DetailMediaAdapter.TAG && (position < firstItemPosition || position > lastItemPosition)) {
                        if (GSYVideoManager.isFullState(this@TravelDetailActivity)) {
                            return
                        }
                        //如果滑出去了上面和下面就是否
                        GSYVideoManager.releaseAllVideos()
                        mMediaAdapter.notifyDataSetChanged()
                    }

                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.pointView.onPageSelected(firstItemPosition)
                }


            }


        })

    }

    override fun initData() {
        presenter = TravelDetailPresenter(this)
        presenter.getData(travelId)
    }

    private fun setLikeImage() {

        binding.likeIv.setImageResource(
            if (collect_status == 2) {
                if (isToolbarDark) {
                    R.mipmap.ic_like_red_night
                } else {
                    R.mipmap.ic_like_red_light
                }
            } else {
                if (isToolbarDark) {
                    R.mipmap.ic_like_normal_night
                } else {
                    R.mipmap.ic_like_normal_light
                }
            }
        )
    }

    private fun setBackImage() {
        binding.backIv.setImageResource(
            if (isToolbarDark) {
                R.mipmap.ic_back_black
            } else {
                R.mipmap.ic_back_light
            }
        )
    }


    private fun setShareImage() {
        binding.shareIv.setImageResource(
            if (isToolbarDark) {
                R.mipmap.ic_share_night
            } else {
                R.mipmap.ic_share_light
            }
        )
    }


    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }


    override fun bindListener() {
        binding.phoneLl.setOnClickListener {
            val data = Uri.parse("tel:$businessPhone")
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = data
            startActivity(intent)
        }
        binding.navLl.setOnClickListener {
            FeaturesUtil.startNaviGoogle(this, mLat.toFloat(), mLng.toFloat())
        }
        binding.shareIv.setOnClickListener {
            val url = String.format(Html5Url.SHARE_FACEBOOK, Html5Url.MODEL_TRAVEL, travelId)
            MobUtil.shareFacebookWebPage(url, this@TravelDetailActivity)
        }
        binding.likeIv.setOnClickListener {
            if (!User.isLogon()) {
                startActivity(LoginActivity::class.java)
                return@setOnClickListener
            }
            if (collect_status == 1) {
                presenter.collection(travelId)
            } else {
                presenter.unCollection(travelId)
            }
        }
        binding.backIv.setOnClickListener {
            finish()
        }
        binding.moreCommentTv.setOnClickListener {
            startActivity(TravelCommentActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_ID, travelId)
            })
        }
        binding.writeCommentLl.setOnClickListener {
            CommentPostActivity.navigation(this, 1, travelId, null, name, image)
        }
    }

    override fun bindDetail(response: TravelDetail, mediaList: ArrayList<MediaData>) {
        collect_status = response.is_collect
        businessPhone = response.phone
        setLikeImage()

        if (mediaList.isNotEmpty()) {
            image = mediaList[0].src
        }

        // 轮播
        mMediaAdapter.setNewInstance(mediaList)
        binding.pointView.setPointCount(mediaList.size)

        name = response.name
        binding.nameTv.text = name
        binding.scoreTv.text = response.grade.toString()

        (response.start_time + "-" + response.end_time).also { binding.timeTv.text = it }
        binding.phoneTv.text = response.phone
        binding.addressNameTv.text = response.address

        // 评价
        val list = response.comments
        if (list.isEmpty()) {
            emptyView.showEmptyContainer()
        }
        mCommentAdapter.setNewInstance(list)

        // 介绍
        val content = response.content
        if (content.isBlank()) {
            binding.descTv.text = StringUtils.getString(R.string.no_content)
            binding.descTv.gravity = Gravity.CENTER
        } else {
            binding.descTv.text = content
        }

        val notice = response.notice
        if (notice.isBlank()) {
            binding.noticeTv.text = StringUtils.getString(R.string.no_content)
            binding.noticeTv.gravity = Gravity.CENTER
        } else {
            binding.noticeTv.text = notice
        }

        binding.commentCountTv.text = getString(R.string.comment_format, response.comments_count)

    }

    override fun collectionHandleSuccess(collectionStatus: Boolean) {
        if (collectionStatus) {
            collect_status = 2
            ToastUtils.showShort(R.string.collection_success)
        } else {
            collect_status = 1
            ToastUtils.showShort(R.string.cancel_collection_success)
        }
        setLikeImage()
    }


    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        when (baseEvent.code) {
            EventCode.EVENT_POSITION  -> {
                val position = baseEvent.data as Int
                binding.mediaRv.scrollToPosition(position)
                binding.pointView.onPageSelected(position)
            }
            else -> {}
        }

    }


}