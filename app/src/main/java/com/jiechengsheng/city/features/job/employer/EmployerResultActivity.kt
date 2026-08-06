package com.jiechengsheng.city.features.job.employer

import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.databinding.ActivityEmployerResultBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/10/27 15:30.
 *  提交成功
 */
class EmployerResultActivity :BaseActivity() {

    private lateinit var binding: ActivityEmployerResultBinding

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_employer_result

    override fun initView() {
        binding = ActivityEmployerResultBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

    }

    override fun initData() = Unit

    override fun bindListener() {
        binding.finishTv.setOnClickListener {
            finish()
        }


    }
}