package com.jiechengsheng.city.features.comment

import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.databinding.ActivityCommentSuccessBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/12/23 15:33.
 * 评价成功
 */
class CommentSuccessActivity : BaseActivity() {

    private lateinit var binding: ActivityCommentSuccessBinding

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_comment_success

    override fun initView() {
        binding = ActivityCommentSuccessBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

    }

    override fun onBackPressed() = Unit

    override fun initData() {

    }

    override fun bindListener() {
        binding.finishTv.setOnClickListener {
            finish()
        }
    }
}