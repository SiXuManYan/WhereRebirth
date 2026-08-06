package com.jiechengsheng.city.features.mall.shop.home.info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.databinding.ActivityQualificationBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2022/5/31 16:25.
 * 企业资质
 */
class QualificationActivity : BaseActivity() {

    private lateinit var binding: ActivityQualificationBinding

    companion object {

        fun navigation(context: Context, imageUrl: String?) {
            val bundle = Bundle().apply {
                putString(Constant.PARAM_IMAGE, imageUrl)
            }
            val intent = Intent(context, QualificationActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_qualification

    override fun initView() {
        binding = ActivityQualificationBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        val url = intent.getStringExtra(Constant.PARAM_IMAGE)
        GlideUtil.load(this, url, binding.qualificationIv)

    }

    override fun initData() = Unit

    override fun bindListener() = Unit
}