package com.jiechengsheng.city.features.feedback.form

import android.content.Intent
import android.graphics.Color
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.ErrorResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.store.refund.StoreRefundAdapter
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.zhihu.matisse.Matisse
import com.jiechengsheng.city.databinding.ActivityFeedbackPostBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/10/13 16:33.
 * 意见反馈表单
 */
class FeedBackPostActivity : BaseMvpActivity<FeedBackPostPresenter>(), FeedBackPostView {

    private lateinit var binding: ActivityFeedbackPostBinding

    private lateinit var mImageAdapter: StoreRefundAdapter


    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_feedback_post

    override fun initView() {
        binding = ActivityFeedbackPostBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        initImage()


    }

    private fun initImage() {
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

        binding.contentEt.addTextChangedListener (afterTextChanged = {
            val trim = it.toString().trim()
            if (trim.isBlank()) {
                binding.commitTv.isClickable = false
                binding.commitTv.alpha = 0.5f
            }else {
                binding.commitTv.isClickable = true
                binding.commitTv.alpha = 1.0f
            }

        })

    }

    override fun initData() {
        presenter = FeedBackPostPresenter(this)
    }

    override fun bindListener() {
        binding.selectIv.setOnClickListener {
            val size = mImageAdapter.data.size
            if (size == 4) {
                ToastUtils.showShort(R.string.up_to_4)
                return@setOnClickListener
            }
            val max = 4 - size
            FeaturesUtil.handleMediaSelect(this, Constant.IMG, max)
        }

        binding.commitTv.setOnClickListener {

            val content = binding.contentEt.text.toString().trim()
            val phone = binding.phoneEt.text.toString().trim()

            if (content.isBlank()) {
                return@setOnClickListener
            }
            binding.commitTv.isClickable = false
            if (mImageAdapter.data.isNotEmpty()) {
                presenter.upLoadImage(content, phone, ArrayList(mImageAdapter.data))
                return@setOnClickListener
            }
            presenter.postFeedback(content, phone, null)
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
        ToastUtils.showShort(R.string.question_click_hint)
        finish()
    }

    override fun onError(errorResponse: ErrorResponse?) {
        super.onError(errorResponse)
        binding.commitTv.isClickable = true
    }


}