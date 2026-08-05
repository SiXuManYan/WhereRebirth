package com.jiechengsheng.city.features.mechanism

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.MechanismDetailResponse
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.media.MediaDetailActivity
import com.jiechengsheng.city.features.web.WebViewActivity
import com.jiechengsheng.city.frames.common.Html5Url
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.jiechengsheng.city.utils.MobUtil
import com.jiechengsheng.city.widget.JcsBanner
import com.jiechengsheng.city.databinding.ActivityMechanismBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/9/4 15:08.
 *  机构详情
 */
class MechanismActivity : BaseMvpActivity<MechanismPresenter>(), MechanismView {

    private lateinit var binding: ActivityMechanismBinding

    private var infoId = 0

    /** 是否收藏  2 已收藏*/
    private var collect_status = 1


    private lateinit var mAdapter: ImageBannerAdapter

    private var isToolbarDark = false
    private var businessPhone = ""
    private var webUrl = ""
    private var facebook = ""
    private var mLat = 0.0
    private var mLng = 0.0


    companion object {

        fun navigation(context: Context, id: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ID, id)
            }
            val intent = Intent(context, MechanismActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun getLayoutId() = R.layout.activity_mechanism

    override fun isStatusDark() = isToolbarDark

    override fun initView() {
        binding = ActivityMechanismBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        infoId = intent.getIntExtra(Constant.PARAM_ID, 0)
        initScroll()
        initMedia()
    }

    private fun initMedia() {
        mAdapter = ImageBannerAdapter()
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.mediaRv)

        binding.mediaRv.apply {
            layoutManager = LinearLayoutManager(this@MechanismActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstItemPosition: Int = layoutManager.findFirstVisibleItemPosition()
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.pointView.onPageSelected(firstItemPosition)
                    }
                }
            })
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            MediaDetailActivity.navigationOnlyStringImage(this, position, mAdapter.data)
        }

    }


    override fun initData() {
        presenter = MechanismPresenter(this)
        presenter.getDetail(infoId)
    }

    override fun bindListener() {
        binding.backIv.setOnClickListener { finish() }
        binding.phoneLl.setOnClickListener {
            val data = Uri.parse("tel:$businessPhone")
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = data
            startActivity(intent)
        }
        binding.navLl.setOnClickListener {
            FeaturesUtil.startNaviGoogle(this, mLat.toFloat(), mLng.toFloat())
        }
        binding.webTv.setOnClickListener {
            WebViewActivity.navigation(this, webUrl)
        }
        binding.facebookTv.setOnClickListener {
            WebViewActivity.navigation(this, facebook)
        }
        binding.shareIv.setOnClickListener {
            val url = String.format(Html5Url.SHARE_FACEBOOK, Html5Url.MODEL_GENERAL, infoId)
            MobUtil.shareFacebookWebPage(url, this@MechanismActivity)
        }
        binding.likeIv.setOnClickListener {
            if (!User.isLogon()) {
                startActivity(LoginActivity::class.java)
                return@setOnClickListener
            }
            if (collect_status == 1) {
                presenter.collection(infoId)
            } else {
                presenter.unCollection(infoId)
            }
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

    override fun bindDetail(data: MechanismDetailResponse) {

        binding.nameTv.text = data.title
        binding.addressNameTv.text = data.address
        collect_status = data.collect_status
        businessPhone = data.tel
        webUrl = data.web_site
        facebook = data.facebook
        setLikeImage()

        // 营业时间
        val weeks = StringUtils.getStringArray(R.array.weeks)
        val startWeek: String = weeks[data.week_start - 1]
        val endWeek: String = weeks[data.week_end - 1]
        binding.timeTv.text = getString(R.string.valid_period_format2, startWeek, endWeek)

        binding.phoneTv.apply {
            text = businessPhone
            visibility = if (businessPhone.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }

        }
        binding.mailTv.apply {
            text = data.email
            visibility = if (data.email.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        binding.webTv.apply {
            text = data.web_site
            visibility = if (data.web_site.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        binding.facebookTv.apply {
            text = data.facebook
            visibility = if (data.facebook.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }

        }
        val content = data.abstractX
        binding.contentTv.text = if (content.isBlank()) {
            getString(R.string.no_introduce)
        } else {
            content
        }
        val images = data.images
        if (images.isEmpty()) {
            images.add(JcsBanner.DEFAULT)
        }
        mLat = data.lat
        mLng = data.lng
        binding.pointView.setPointCount(data.images.size)
        mAdapter.setNewInstance(data.images)

    }

    override fun collectionHandleSuccess(collectionStatus: Int) {
        collect_status = collectionStatus

        if (collectionStatus == 2) {
            ToastUtils.showShort(R.string.collection_success)
        } else {
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