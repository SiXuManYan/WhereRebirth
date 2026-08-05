package com.jiechengsheng.city.features.merchant.recult

import android.view.View
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.network.BaseMvpPresenter
import com.jiechengsheng.city.api.network.BaseMvpView
import com.jiechengsheng.city.api.request.merchant.MerchantSettledData
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.FragemntSettledResultBinding

/**
 * Created by Wangsw  2021/11/19 18:15.
 * 商家入驻审核中、审核结果
 */
class SettledResultFragment : BaseMvpFragment<SettledResultPresenter>(), SettledResultView {

    private var _binding: FragemntSettledResultBinding? = null
    private val binding get() = _binding!!


    /**
     * 审核状态（1：待审核，2：审核通过，3：审核未通过）
     */
    var type = 0


    override fun getLayoutId() = R.layout.fragemnt_settled_result

    override fun initView(view: View) {
        _binding = FragemntSettledResultBinding.bind(requireNotNull(view))
        changeContent(type)
    }

    override fun initData() {


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun bindListener() = Unit


    /**
     * 审核状态（1：待审核，2：审核通过，3：审核未通过）
     */
    private fun changeContent(type: Int) {
        when (type) {
            2 -> {
                binding.typeImageIv.setImageResource(R.mipmap.ic_audit_success)
                binding.typeTextTv.text = getString(R.string.merchant_verify_success_title)
                binding.typeDescTv.text = getString(R.string.merchant_verify_success_desc)
                // 再次提交暂时隐藏
                binding.recommitTv.visibility = View.GONE
//                binding.recommitTv.text = getString(R.string.commit_again)
//                binding.recommitTv.setOnClickListener {
//                    EventBus.getDefault().post(BaseEvent<Boolean>(EventCode.EVENT_MERCHANT_COMMIT_NEW))
//                }
            }
            3 -> {
                binding.typeImageIv.setImageResource(R.mipmap.ic_audit_fail)
                binding.typeTextTv.text = getString(R.string.merchant_verify_faild_title)
                binding.typeDescTv.text = getString(R.string.merchant_verify_faild_desc)
                binding.recommitTv.visibility = View.VISIBLE
                binding.recommitTv.text = getString(R.string.recommit)
                binding.recommitTv.setOnClickListener {
                    EventBus.getDefault().post(BaseEvent<Boolean>(EventCode.EVENT_MERCHANT_RECOMMIT))
                }
            }
            else -> {
                binding.typeImageIv.setImageResource(R.mipmap.ic_auditing)
                binding.typeTextTv.text = getString(R.string.merchant_verify_ing_title)
                binding.typeDescTv.text = getString(R.string.merchant_verify_ing_desc)
                binding.recommitTv.visibility = View.GONE

            }
        }

    }


    override fun onEventReceived(baseEvent: BaseEvent<*>?) {
        super.onEventReceived(baseEvent)
        if (baseEvent == null) {
            return
        }
        when (baseEvent.code) {
            EventCode.EVENT_MERCHANT_POST_SUCCESS -> {
                type = 1
                changeContent(type)
            }
            EventCode.EVENT_MERCHANT_CHANGE_TYPE -> {
                type = if (baseEvent.data is MerchantSettledData) {
                    (baseEvent.data as MerchantSettledData).is_verify
                } else {
                    (baseEvent.data as Int)
                }
                changeContent(type)
            }
            else -> {

            }
        }
    }


}

interface SettledResultView : BaseMvpView {

}

class SettledResultPresenter(private var view: SettledResultView) : BaseMvpPresenter(view) {

}