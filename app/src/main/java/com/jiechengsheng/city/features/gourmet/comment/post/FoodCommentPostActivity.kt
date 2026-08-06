package com.jiechengsheng.city.features.gourmet.comment.post

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.store.refund.StoreRefundAdapter
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.jiechengsheng.city.widget.ratingstar.OnChangeRatingByClickListener
import com.zhihu.matisse.Matisse
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityCommentPostBinding
import android.view.ViewGroup


/**
 * Created by Wangsw  2021/8/17 17:25.
 * 美食评价
 */
class FoodCommentPostActivity : BaseMvpActivity<FoodCommentPostPresenter>(), FoodCommentPostView {

    private lateinit var binding: ActivityCommentPostBinding


    private var orderId = 0
    private var restaurantId = 0
    private var type = 0


    private lateinit var mImageAdapter: StoreRefundAdapter

    override fun getLayoutId() = R.layout.activity_comment_post

    override fun isStatusDark(): Boolean = true

    override fun initView() {
        binding = ActivityCommentPostBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        intent.extras?.let {
            orderId = it.getInt(Constant.PARAM_ORDER_ID)
            restaurantId = it.getInt(Constant.PARAM_RESTAURANT_ID)
            type = it.getInt(Constant.PARAM_TYPE)
        }

        // 相册
        mImageAdapter = StoreRefundAdapter().apply {
            addChildClickViewIds(R.id.delete_iv)
            setOnItemChildClickListener { adapter, view, position ->
                when (view.id) {
                    R.id.delete_iv -> {
                        mImageAdapter.removeAt(position)
                    }
                    else -> {
                    }
                }
            }
        }

        binding.imageRv.apply {
            adapter = mImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }


    }

    override fun initData() {
        presenter = FoodCommentPostPresenter(this)
        presenter.restaurantId = restaurantId
        presenter.commmentType = type
    }

    override fun bindListener() {
        binding.selectIv.setOnClickListener {
            val size = mImageAdapter.data.size
            if (size == 6) {
                ToastUtils.showShort(R.string.refund_image_max_6)
                return@setOnClickListener
            }
            val max = 6 - size
            FeaturesUtil.handleMediaSelect(this, Constant.IMG, max)
        }
        binding.commitTv.setOnClickListener {

            val rating = binding.starView.rating.toInt()
            val content = binding.contentEt.text.toString().trim()

            if (mImageAdapter.data.isNotEmpty()) {
                presenter.upLoadImage(orderId, rating, content, ArrayList(mImageAdapter.data))
            } else {
                presenter.commitStoreComment(orderId, rating, content, null)
            }
        }

        binding.starView.onChangeRatingByClickListener = object : OnChangeRatingByClickListener {
            override fun clickRatingResult(rating: Int) {
                binding.commentValueTv.text = BusinessUtils.getCommentRatingText(rating)
            }

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        val elements = Matisse.obtainPathResult(data)
        elements.forEach {
            mImageAdapter.addData(it)
        }
    }

    override fun commitSuccess() {
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_ORDER_LIST))
        ToastUtils.showShort(R.string.commit_success)
        finish()
    }
}