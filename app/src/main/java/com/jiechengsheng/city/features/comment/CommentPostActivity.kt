package com.jiechengsheng.city.features.comment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.ErrorResponse
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.store.refund.StoreRefundAdapter
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.jiechengsheng.city.utils.GlideUtil
import com.zhihu.matisse.Matisse

import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityCommentPost2Binding
import android.view.ViewGroup


/**
 * Created by Wangsw  2021/10/19 15:04.
 * 发表评价
 * 0 酒店
 * 1 旅游
 * 2 商城
 */
class CommentPostActivity : BaseMvpActivity<CommentPostPresenter>(), CommentView {

    private lateinit var binding: ActivityCommentPost2Binding

    /** 订单id */
    private var orderId = 0

    /** 业务id 如：酒店id  旅游id  订单id ,*/
    private var targetId = 0

    /** 评论类型( 0 酒店 , 1 旅游 ，2 mall商城) */
    private var commentType = 0

    /** 名称 */
    private var name = ""

    /** 头像 */
    private var avatar = ""


    private lateinit var mImageAdapter: StoreRefundAdapter

    override fun getLayoutId() = R.layout.activity_comment_post_2

    override fun isStatusDark(): Boolean = true

    companion object {

        /**
         * @param commentType  评论类型( 0 酒店 , 1 旅游)
         * @param targetId 业务id （酒店、旅游,商品）
         * @param orderId 订单id(如需要)
         * @param name 名字(如需要)
         * @param avatar 名字(如需要)
         *
         */
        fun navigation(
            context: Context,
            commentType: Int,
            targetId: Int? = null,
            orderId: Int? = null,
            name: String? = "",
            avatar: String? = "",
        ) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_TYPE, commentType)

                targetId?.let {
                    putInt(Constant.PARAM_ID, it)
                }


                orderId?.let {
                    putInt(Constant.PARAM_ORDER_ID, it)
                }

                name?.let {
                    putString(Constant.PARAM_NAME, it)
                }

                avatar?.let {
                    putString(Constant.PARAM_AVATAR, it)
                }

            }
            val intent = Intent(context, CommentPostActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun initView() {
        binding = ActivityCommentPost2Binding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        intent.extras?.let {
            commentType = it.getInt(Constant.PARAM_TYPE, 0)
            targetId = it.getInt(Constant.PARAM_ID, 0)
            orderId = it.getInt(Constant.PARAM_ORDER_ID, 0)
            name = it.getString(Constant.PARAM_NAME, "")
            avatar = it.getString(Constant.PARAM_AVATAR, "")
        }

        // 相册
        mImageAdapter = StoreRefundAdapter().apply {
            addChildClickViewIds(R.id.delete_iv)
            setOnItemChildClickListener { adapter, view, position ->
                when (view.id) {
                    R.id.delete_iv -> mImageAdapter.removeAt(position)
                }
            }
        }

        binding.imageRv.apply {
            adapter = mImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        if (name.isNotBlank()) {
            binding.targetUserLl.visibility = View.VISIBLE
            binding.nameTv.text = name
            GlideUtil.load(this, avatar, binding.imageIv, 5)
        } else {
            binding.targetUserLl.visibility = View.GONE
        }


        if (commentType == 0) {
            binding.commentStarView.apply {
                setCheckStarDrawable(R.mipmap.ic_comment_diamond_yellow)
                setStarDrawable(R.mipmap.ic_comment_diamond_gray)
                refreshView()
            }
        }

    }

    override fun initData() {
        presenter = CommentPostPresenter(this)
        presenter.targetId = targetId

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
            binding.commitTv.isClickable = false
            val rating = binding.commentStarView.checkStarCount
            val content = binding.contentEt.text.toString().trim()

            if (mImageAdapter.data.isNotEmpty()) {
                presenter.upLoadImage(commentType, orderId, rating, content, ArrayList(mImageAdapter.data))
                return@setOnClickListener
            }
            when (commentType) {
                0 -> presenter.postHotelComment(orderId, rating, content, null)
                1 -> presenter.postTravelComment(rating, content)
                2 -> presenter.commitMallComment(rating, rating, content, null)

                else -> {
                }
            }


        }

        binding.commentStarView.setStoreItemOnClickListener {
            binding.commentValueTv.text = BusinessUtils.getCommentRatingText(it + 1)
        }
        binding.backIv.setOnClickListener {
            finish()
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
        startActivity(CommentSuccessActivity::class.java)
        finish()
    }

    override fun onError(errorResponse: ErrorResponse?) {
        super.onError(errorResponse)
        binding.commitTv.isClickable = true
    }

}