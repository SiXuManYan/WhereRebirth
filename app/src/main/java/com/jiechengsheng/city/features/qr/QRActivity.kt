package com.jiechengsheng.city.features.qr

import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.gourmet.qr.QrResponse
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.databinding.ActivityQrBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/4/23 16:28.
 * 二维码
 */
class QRActivity : BaseMvpActivity<QRPresenter>(), QRView {

    private lateinit var binding: ActivityQrBinding

    private var orderId = "";

    override fun getLayoutId() = R.layout.activity_qr

    override fun initView() {
        binding = ActivityQrBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

        val bundle = intent.extras
        if (bundle == null) {
            finish()
            return
        }
        orderId = bundle.getString(Constant.PARAM_ORDER_ID, "")

    }

    override fun initData() {
        presenter = QRPresenter(this);
        presenter.getQrDetail(orderId)
    }

    override fun bindListener() {

    }

    override fun bindDetail(it: QrResponse) {
        binding.nameTv.text = it.restaurant_name
        binding.goodNameTv.text = it.good_name
        binding.expireDateTv.text = getString(R.string.expire_date, it.expire_date)
        binding.couponNoTv.text = it.coupon_no
        GlideUtil.load(this, it.coupon_qr_code, binding.qrIv)

    }


}
