package com.jiechengsheng.city.features.merchant.form

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.request.merchant.MerchantSettledData
import com.jiechengsheng.city.api.request.merchant.MerchantSettledPost
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpFragment
import com.jiechengsheng.city.features.store.refund.StoreRefundAdapter
import com.jiechengsheng.city.features.city.CityPickerActivity
import com.jiechengsheng.city.mine.activity.merchant_settled.SettledTypeActivity
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.FeaturesUtil
import com.jiechengsheng.city.utils.RequestResultCode
import com.zhihu.matisse.Matisse
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.FragmentFormSettledBinding

/**
 * Created by Wangsw  2021/11/19 15:43.
 * 商家入住表单
 */
class SettledFormFragment : BaseMvpFragment<SettledFormPresenter>(), SettledFormView {

    private var _binding: FragmentFormSettledBinding? = null
    private val binding get() = _binding!!


    private var merTypeId = 0
    private var areaId = 0

    /** 是否有实体店（1：有，2：没有） */
    private var hasPhysicalStore = 0
    private var isReCommit = false
    private var reCommitMerchantId = 0


    private lateinit var mImageAdapter: StoreRefundAdapter


    override fun getLayoutId() = R.layout.fragment_form_settled


    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val bundle = it.data?.extras ?: return@registerForActivityResult
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                binding.areaTv.text = bundle.getString(Constant.PARAM_SELECT_AREA_NAME)
                val area = bundle.getString(Constant.PARAM_SELECT_AREA_ID, "0")
                areaId = area.toInt()
            }
            RequestResultCode.RESULT_SETTLED_TYPE_TO_MERCHANT_SETTLED -> {
                binding.merTypeTv.text = bundle.getString("typeName", "")
                merTypeId = bundle.getInt("typeId", 0)
            }
        }

    }

    override fun initView(view: View) {
        _binding = FragmentFormSettledBinding.bind(requireNotNull(view))

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
    }

    override fun initData() {
        presenter = SettledFormPresenter(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun bindListener() {
        binding.selectIv.setOnClickListener {
            val size = mImageAdapter.data.size
            if (size == 2) {
                ToastUtils.showShort(R.string.refund_image_max_2)
                return@setOnClickListener
            }
            val max = 2 - size
            FeaturesUtil.handleMediaSelect4Fragment(this, Constant.IMG, max)
        }

        binding.areaTv.setOnClickListener {
            launcher.launch(Intent(requireContext(), CityPickerActivity::class.java))
        }
        binding.merTypeTv.setOnClickListener {
            launcher.launch(Intent(requireContext(), SettledTypeActivity::class.java))
        }

        binding.physicalStoreRg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.yes_rb -> {
                    hasPhysicalStore = 1
                }
                R.id.no_rb -> {
                    hasPhysicalStore = 2
                }


                else -> {
                }
            }

        }


        binding.commitTv.setOnClickListener {

            val apply = MerchantSettledPost().apply {
                first_name = binding.firstNameEt.text.toString()
                middle_name = binding.middleNameEt.text.toString()
                last_name = binding.lastNameEt.text.toString()
                contact_number = binding.contactNumberEt.text.toString()
                email = binding.emailEt.text.toString()
                mer_name = binding.merNameEt.text.toString().trim()
                mer_address = binding.merAddressEt.text.toString()
                mer_tel = binding.merTelEt.text.toString()

                mer_type_id = merTypeId
                area_id = areaId
                has_physical_store = hasPhysicalStore
            }


            apply.apply {
                if (first_name.isBlank() || last_name.isBlank() || contact_number.isBlank()
                    || email.isBlank() || mer_name.isBlank() || mer_address.isBlank() || mer_tel.isBlank()
                ) {
                    ToastUtils.showShort(R.string.please_enter)
                    return@setOnClickListener
                }
                if (mer_type_id == 0 || area_id == 0 || has_physical_store == 0) {
                    ToastUtils.showShort(R.string.please_selected)
                    return@setOnClickListener
                }
            }

            if (mImageAdapter.data.size < 2) {
                ToastUtils.showShort(getString(R.string.please_upload))
                return@setOnClickListener
            }
            binding.commitTv.isClickable = false
            binding.commitTv.postDelayed(Runnable { binding.commitTv.isClickable = true }, 2000)
            presenter.upLoadImage(apply, ArrayList(mImageAdapter.data), isReCommit, reCommitMerchantId)
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

    override fun postResult(result: Boolean) {
        binding.commitTv.isClickable = true
        EventBus.getDefault().post(BaseEvent<Boolean>(EventCode.EVENT_MERCHANT_POST_SUCCESS))
        clearCache()
    }


    override fun onEventReceived(baseEvent: BaseEvent<*>?) {
        super.onEventReceived(baseEvent)
        if (baseEvent == null) {
            return
        }
        when (baseEvent.code) {
            EventCode.EVENT_MERCHANT_CHANGE_TYPE -> {
                val response = baseEvent.data as MerchantSettledData
                isReCommit = true
                response.apply {
                    reCommitMerchantId = id
                    binding.firstNameEt.setText(first_name)
                    binding.middleNameEt.setText(middle_name)
                    binding.lastNameEt.setText(last_name)
                    binding.contactNumberEt.setText(contact_number)
                    binding.emailEt.setText(email)
                    binding.merNameEt.setText(mer_name)
                    binding.merAddressEt.setText(mer_address)
                    binding.merTelEt.setText(mer_tel)
                }
            }
            else -> {

            }
        }
    }

    private fun clearCache() {
        for (index in 0 until binding.containerLl.childCount) {

            val childView = binding.containerLl.getChildAt(index)
            if (childView is AppCompatEditText) {
                childView.setText("")
            }
        }
        binding.merTypeTv.text = ""
        binding.areaTv.text = ""
        binding.yesRb.isChecked = false
        binding.noRb.isChecked = false
        merTypeId = 0
        areaId = 0
        hasPhysicalStore = 0
        isReCommit = false
        reCommitMerchantId = 0
        mImageAdapter.setNewInstance(null)

    }


}

