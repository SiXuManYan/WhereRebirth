package com.jiechengsheng.city.features.address.edit

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.home.watcher.AfterTextChangeWatcher
import com.jiechengsheng.city.features.city.CityPickerActivity
import com.jiechengsheng.city.utils.Constant
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityAddressEditBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/3/22 14:19.
 * 收货地址编辑
 */
class AddressEditActivity : BaseMvpActivity<AddressEditPresenter>(), AddressEditView {

    private lateinit var binding: ActivityAddressEditBinding

    private var mAddress: String? = null
    private var mRecipient: String? = null
    private var mSex = 0
    private var mPhone: String? = null
    private var mAddressId: String? = null
    private var mAreaId = 0
    private var mAreaName = ""

    /**
     * 是否是修改
     */
    private var isChange = false

    override fun getLayoutId() = R.layout.activity_address_edit

    override fun initView() {
        binding = ActivityAddressEditBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)

    }

    override fun isStatusDark(): Boolean = true

    override fun initData() {
        presenter = AddressEditPresenter(this)
        val bundle = intent.extras
        if (bundle != null) {
            isChange = true
            binding.deleteTv.visibility = View.VISIBLE
            mAddressId = bundle.getString(Constant.PARAM_ADDRESS_ID, "")
            mAddress = bundle.getString(Constant.PARAM_ADDRESS, "")
            mRecipient = bundle.getString(Constant.PARAM_RECIPIENT, "")
            mSex = bundle.getInt(Constant.PARAM_SEX, 0)
            mPhone = bundle.getString(Constant.PARAM_PHONE, "")
            mAreaId = bundle.getInt(Constant.PARAM_AREA_ID, 0)
            mAreaName = bundle.getString(Constant.PARAM_AREA_NAME, "")
            binding.cityTv.text = mAreaName

            binding.addressEt.setText(mAddress)
            binding.recipientEt.setText(mRecipient)
            if (mSex == 1) {
                binding.manRb.isChecked = true
            } else {
                binding.womanRb.isChecked = true
            }
            binding.phoneEt.setText(mPhone)
            binding.titleTv.setText(R.string.edit_address)
            return
        }
        binding.deleteTv.visibility = View.GONE
        binding.titleTv.setText(R.string.add_address)
    }

    override fun bindListener() {
        binding.addressEt.addTextChangedListener(object : AfterTextChangeWatcher() {
            override fun onAfterTextChanged(editable: Editable) {
                checkEnable()
            }
        })
        binding.recipientEt.addTextChangedListener(object : AfterTextChangeWatcher() {
            override fun onAfterTextChanged(editable: Editable) {
                checkEnable()
            }
        })
        binding.phoneEt.addTextChangedListener(object : AfterTextChangeWatcher() {
            override fun onAfterTextChanged(editable: Editable) {
                checkEnable()
            }
        })
        binding.saveTv.setOnClickListener { handleSave() }
        binding.deleteTv.setOnClickListener { deleteAddress() }
        binding.cityTv.setOnClickListener {
            launcher.launch(Intent(this, CityPickerActivity::class.java).putExtra(Constant.PARAM_HIDE_CURRENT_LOCATION,true))
        }
    }


    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val bundle = it.data?.extras ?: return@registerForActivityResult
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                mAreaName = bundle.getString(Constant.PARAM_SELECT_AREA_NAME,"")
                val areaId = bundle.getString(Constant.PARAM_SELECT_AREA_ID, "0")
                mAreaId = areaId.toInt()
                binding.cityTv.text = mAreaName
            }

        }

    }

    private fun checkEnable() {
        val address = binding.addressEt.text.toString().trim()
        val recipient = binding.recipientEt.text.toString().trim()
        val phone = binding.phoneEt.text.toString().trim()
        if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(recipient) && !TextUtils.isEmpty(phone)) {
            binding.saveTv.alpha = 1.0f
        } else {
            binding.saveTv.alpha = 0.5f
        }
    }

    private fun handleSave() {
        val address = binding.addressEt.text.toString().trim()
        val recipient = binding.recipientEt.text.toString().trim()
        val phone = binding.phoneEt.text.toString().trim()
        mSex = if (binding.manRb.isChecked) {
            1
        } else {
            2
        }

        presenter.handleSave(address, recipient, phone, mSex, isChange, mAddressId, mAreaId)
    }

    private fun deleteAddress() {
        AlertDialog.Builder(this)
            .setTitle(R.string.hint)
            .setCancelable(false)
            .setMessage(getString(R.string.confirm_delete_address))
            .setPositiveButton(R.string.confirm) { _: DialogInterface?, _: Int -> presenter.deleteAddress(mAddressId) }
            .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
            .create().show()
    }

    override fun editSuccess() {
        ToastUtils.showShort("edit success")
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_ADDRESS))
        finish()
    }

    override fun addAddressSuccess() {
        ToastUtils.showShort("add success")
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_ADDRESS))
        finish()
    }

    override fun deleteAddressSuccess() {
        ToastUtils.showShort("delete success")
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_ADDRESS))
        finish()
    }
}