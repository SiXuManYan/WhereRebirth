package com.jiechengsheng.city.features.job.record.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.blankj.utilcode.util.BarUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.job.Job
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.databinding.ActivityJobAppliedResultBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/12/19 15:05.
 * 申请结果
 */
class JobAppliedResultActivity  :BaseActivity(){

    private lateinit var binding: ActivityJobAppliedResultBinding

    /**
     *  1已申请 2申请失败 3待面试 4面试成功 5面试失败
     *  @see com.jiechengsheng.city.api.response.job.Job.status
     */
    private var status = 0

    companion object {

        fun navigation(context: Context, status: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_STATUS, status)
            }
            val intent = Intent(context, JobAppliedResultActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun isStatusDark() = true


    override fun getLayoutId()= R.layout.activity_job_applied_result

    override fun initView() {
        binding = ActivityJobAppliedResultBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this,Color.WHITE)
        status = intent.getIntExtra(Constant.PARAM_STATUS , 0)

    }

    override fun initData() {
        when (status) {
            Job.STATUS_APPLIED -> {
                binding.statusIv.setImageResource(R.mipmap.ic_status_applied)
                binding.messageTv.setText(R.string.status_applied_message)
                binding.hintTv.setText(R.string.status_applied_hint)
            }
            Job.STATUS_APPLIED_FAILED -> {
                binding.statusIv.setImageResource(R.mipmap.ic_status_applied_failed)
                binding.messageTv.setText(R.string.status_applied_failed_message)
            }
            Job.STATUS_TO_INTERVIEWS -> {
                binding.statusIv.setImageResource(R.mipmap.ic_status_to_interviews)
                binding.messageTv.setText(R.string.status_to_interviews_message)
                binding.hintTv.setText(R.string.status_to_interviews_hint)
            }
            Job.STATUS_INTERVIEWS_SUCCEED -> {
                binding.statusIv.setImageResource(R.mipmap.ic_status_interviews_succeed)
                binding.messageTv.setText(R.string.status_interviews_succeed_message)
                binding.hintTv.setText(R.string.status_interviews_succeed_hint)
            }
            Job.STATUS_INTERVIEWS_FAILED -> {
                binding.statusIv.setImageResource(R.mipmap.ic_status_interviews_failed)
                binding.messageTv.setText(R.string.status_interviews_failed)
            }
        }

    }

    override fun bindListener() {

    }
}