package com.jiechengsheng.city.features.bills.account.edit

import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.google.gson.JsonElement
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.bills.BillAccount
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.bills.account.BillAccountPresenter
import com.jiechengsheng.city.features.bills.account.BillAccountView
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityBillsAccountEditBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2022/12/9 14:53.
 * 缴费历史账号 添加和编辑
 */
class BillAccountEditActivity : BaseMvpActivity<BillAccountPresenter>(), BillAccountView {

    private lateinit var binding: ActivityBillsAccountEditBinding

    /** 账单类型（1-话费，2-水费，3-电费，4-网费） */
    private var billsType = 0
    private var cacheData: BillAccount? = null

    private var allEditView: ArrayList<AppCompatEditText> = ArrayList()

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_bills_account_edit

    override fun initView() {
        binding = ActivityBillsAccountEditBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        intent.extras?.let {
            billsType = it.getInt(Constant.PARAM_TYPE)
            cacheData = it.getParcelable<BillAccount>(Constant.PARAM_DATA)
        }


        val accountEt = findViewById<AppCompatEditText>(R.id.account_et)
        val accountNameEt = findViewById<AppCompatEditText>(R.id.account_name_et)
        allEditView.add(accountEt)
        allEditView.add(accountNameEt)

    }

    override fun initData() {
        presenter = BillAccountPresenter(this)
        if (cacheData != null) {
            binding.deleteTv.visibility = View.VISIBLE
            binding.accountEt.setText(cacheData!!.first_field)
            binding.accountNameEt.setText(cacheData!!.second_field)
            binding.selectTv.isChecked = cacheData!!.status == 1
            binding.titleTv.setText(R.string.edit_account)
        } else {
            binding.titleTv.setText(R.string.add_account)
        }

    }


    override fun handleSuccess(response: JsonElement) {
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_BILL_HISTORY_ACCOUNT))
        finish()
    }

    override fun bindListener() {
        allEditView.forEach {
            it.addTextChangedListener(
                afterTextChanged = {
                    val result = it.toString().trim()
                    if (result.isBlank()) {
                        BusinessUtils.setViewClickable(false, binding.saveTv)
                    } else {
                        val isBlank = BusinessUtils.checkEditBlank(allEditView)
                        BusinessUtils.setViewClickable(!isBlank, binding.saveTv)
                    }
                }
            )
        }

        binding.saveTv.setOnClickListener {

            val first = binding.accountEt.text.toString().trim()
            val second = binding.accountNameEt.text.toString().trim()
            if (first.isBlank() || second.isBlank()) {
                return@setOnClickListener
            }

            if (cacheData != null) {
                presenter.editAccount(billsType,
                    first,
                    second,
                    binding.selectTv.isChecked, cacheData!!.id)
            } else {
                presenter.addAccount(billsType,
                    first,
                    second,
                    binding.selectTv.isChecked)
            }
        }

        binding.selectTv.setOnClickListener {
            val checked = binding.selectTv.isChecked
            binding.selectTv.isChecked = !checked

        }

        binding.deleteTv.setOnClickListener {
            AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.confirm_delete_account))
                .setPositiveButton(R.string.confirm) { _: DialogInterface?, _: Int -> presenter.deleteAccount(cacheData!!.id) }
                .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                .create().show()
        }
    }


}