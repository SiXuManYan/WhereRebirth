package com.jiechengsheng.city.features.mall.shop.home.info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.request.MallShop
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.mall.shop.home.CollectionHandle
import com.jiechengsheng.city.features.mall.shop.home.MallShopHomePresenter
import com.jiechengsheng.city.features.mall.shop.home.MallShopHomeView
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityMallShopInfoBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2022/5/31 15:12.
 * 店铺信息
 */
class MallShopInfoActivity : BaseMvpActivity<MallShopHomePresenter>(), MallShopHomeView {

    private lateinit var binding: ActivityMallShopInfoBinding


    /** 收藏状态（0：未收藏，1：已收藏） */
    private var collectStatus = 0

    private var collectCount = 0

    private lateinit var data: MallShop

    companion object {

        fun navigation(context: Context, data: MallShop) {
            val bundle = Bundle().apply {
                putParcelable(Constant.PARAM_DATA, data)
            }
            val intent = Intent(context, MallShopInfoActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_mall_shop_info

    override fun initView() {
        binding = ActivityMallShopInfoBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        intent.extras?.let {
            data = it.getParcelable<MallShop>(Constant.PARAM_DATA)!!
            collectCount = data.collect_count
        }
        GlideUtil.load(this, data.logo, binding.shopLogoIv)
        binding.shopNameTv.text = data.title
        updateFanCount(collectCount)


        collectStatus = data.collect_status
        setLikeImage()
        binding.serviceNameTv.text = data.nickname
        binding.addressTv.text = data.address
        if (TextUtils.isEmpty(data.qualification)) {
            binding.qualificationRl.visibility = View.GONE
        } else {
            binding.qualificationRl.visibility = View.VISIBLE
        }


    }

    private fun updateFanCount(count: Int) {
        binding.fansNumberTv.text = getString(R.string.fans_format, count)
    }

    override fun initData() {
        presenter = MallShopHomePresenter(this)
    }

    override fun bindListener() {
        binding.followBt.setOnClickListener {
            if (collectStatus == 0) {
                presenter.collection(data.id, binding.followBt)
            } else {
                presenter.unCollection(data.id, binding.followBt)
            }
        }

        binding.qualificationRl.setOnClickListener {
            QualificationActivity.navigation(this, data.qualification)
        }
    }

    override fun collectionHandleSuccess(collectionStatus: Boolean) {
        if (collectionStatus) {
            collectStatus = 1
            ToastUtils.showShort(R.string.collection_success)
            collectCount++
            updateFanCount(collectCount)
        } else {
            collectStatus = 0
            ToastUtils.showShort(R.string.cancel_collection_success)
            collectCount--
            if (collectCount < 0) collectCount = 0
            updateFanCount(collectCount)
        }

        val apply = CollectionHandle().apply {
            fansCount = collectCount
            status = collectStatus
        }
        EventBus.getDefault().post(BaseEvent(EventCode.EVENT_REFRESH_FOLLOW, apply))
        setLikeImage()
    }

    private fun setLikeImage() {

        if (collectStatus == 0) {
            binding.followBt.setBackgroundResource(R.drawable.shape_blue_radius_16)
            binding.followBt.text = getString(R.string.shop_follow)
            binding.followBt.setTextColor(ColorUtils.getColor(R.color.white))
        } else {
            binding.followBt.setBackgroundResource(R.drawable.stock_blue_radius_16)
            binding.followBt.text = getString(R.string.shop_following)
            binding.followBt.setTextColor(ColorUtils.getColor(R.color.blue_377BFF))
        }

    }

}