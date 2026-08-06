package com.jiechengsheng.city.features.job.employer

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.job.EmployerRequest
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityEmployerBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2022/10/25 13:55.
 * 雇主注册
 */
class EmployerActivity : BaseMvpActivity<EmployerPresenter>(), EmployerView {

    private lateinit var binding: ActivityEmployerBinding

    private var allEditView: ArrayList<AppCompatEditText> = ArrayList()

    private var isSendEmployer = false

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_employer

    override fun initView() {
        binding = ActivityEmployerBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        isSendEmployer = intent.getBooleanExtra(Constant.PARAM_STATUS, false)
        switchContent(isSendEmployer)
    }

    private fun switchContent(isSendEmployer: Boolean) {
        if (isSendEmployer) {
            binding.formNsv.visibility = View.GONE
            binding.alreadyCommitLl.visibility = View.VISIBLE
        } else {
            binding.formNsv.visibility = View.VISIBLE
            binding.alreadyCommitLl.visibility = View.GONE
        }
    }

    override fun initData() {
        presenter = EmployerPresenter(this)
        if (isSendEmployer) {
            presenter.getEmployerEmail()
        }
    }

    override fun bindListener() {
        for (index in 0 until binding.containerLl.childCount) {
            val child = binding.containerLl.getChildAt(index)
            if (child is AppCompatEditText && child.id != R.id.middle_name_et) {
                allEditView.add(child)
            }
        }
        allEditView.forEach {
            it.addTextChangedListener(
                afterTextChanged = {
                    val result = it.toString().trim()
                    if (result.isBlank()) {
                        BusinessUtils.setViewClickable(false, binding.commitTv)
                    } else {
                        val isBlank = BusinessUtils.checkEditBlank(allEditView)
                        BusinessUtils.setViewClickable(!isBlank, binding.commitTv)
                    }
                }
            )
        }

        binding.commitTv.setOnClickListener {
            val first = binding.firstNameEt.text.toString().trim()
            val middle = binding.middleNameEt.text.toString().trim()
            val last = binding.lastNameEt.text.toString().trim()
            val number = binding.contactNumberEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            val company = binding.companyNameEt.text.toString().trim()

            val apply = EmployerRequest().apply {
                first_name = first
                middle_name = middle
                last_name = last
                contact_number = number
                this.email = email
                init_pwd = password
                company_title = company
            }
            showConfirmDialog(apply)
        }

    }


    private fun showConfirmDialog(apply: EmployerRequest) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.dialog_employer_confirm, null)

        val cancelTv = view.findViewById<TextView>(R.id.cancel_tv)
        val confirmTv = view.findViewById<TextView>(R.id.confirm_tv)
        val contentTv = view.findViewById<TextView>(R.id.content_tv)
        contentTv.text = apply.email


        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()


        val window: Window? = alertDialog.window
        if (window != null) {

            // 更改背景
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            window.setContentView(view)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 更改默认宽度
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(window.attributes)
            lp.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(80f)
            window.attributes = lp

        }
        cancelTv.setOnClickListener {
            alertDialog.dismiss()
        }

        confirmTv.setOnClickListener {
            presenter.applyEmployer(apply)

            alertDialog.dismiss()
        }

    }

    override fun applySuccess() {
        // ToastUtils.showShort(R.string.submit_success)
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_EMPLOYER_SUBMIT))
        startActivity(EmployerResultActivity::class.java)
        finish()
    }

    override fun showEmployerEmail(email: String) {
        binding.emailTv.text = getString(R.string.application_email_format, email)
    }


}