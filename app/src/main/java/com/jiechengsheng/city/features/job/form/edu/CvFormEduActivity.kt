package com.jiechengsheng.city.features.job.form.edu

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.job.Degree
import com.jiechengsheng.city.api.response.job.EduDet
import com.jiechengsheng.city.api.response.job.EduRequest
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.job.form.CvFormPresenter
import com.jiechengsheng.city.features.job.form.CvFormView
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.OnWorkTimeSelected
import com.jiechengsheng.city.widget.list.DividerDecoration
import me.shaohui.bottomdialog.BottomDialog
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityJobCvEduBinding

/**
 * Created by Wangsw  2022/10/20 14:48.
 * 鏁欒偛鑳屾櫙
 */
class CvFormEduActivity : BaseMvpActivity<CvFormPresenter>(), CvFormView {

    private lateinit var binding: ActivityJobCvEduBinding

    /**
     * 鏁欒偛缁忓巻id
     * 涓嶄负0鏃朵负淇敼鍐呭
     */
    private var draftEduId = 0

    private var eduRequest = EduRequest()

    /** 涓撲笟鏍囬 */
    private var extendTitle: String? = ""

    var lastDegreeId = 0

    private var degreeDialog: BottomSheetDialog? = null

    private lateinit var mDegreeAdapter: DegreeAdapter

    private var startDialog: BottomDialog? = null
    private var endDialog: BottomDialog? = null

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_job_cv_edu

    override fun initView() {
        binding = ActivityJobCvEduBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        draftEduId = intent.getIntExtra(Constant.PARAM_ID, 0)
        if (draftEduId > 0) {
            binding.deleteTv.visibility = View.VISIBLE
        }
        initDegree()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initDegree() {
        mDegreeAdapter = DegreeAdapter().apply {

            setOnItemClickListener { _, _, position ->

                // 瀛﹀巻
                val degree = mDegreeAdapter.data[position]
                val id = degree.id
                eduRequest.educational_level_id = id
                binding.degreeTv.text = degree.educational_level

                mDegreeAdapter.data.forEach {
                    it.nativeSelected = it.id == id
                }
                mDegreeAdapter.notifyDataSetChanged()

                // 鏄剧ず涓撲笟
                extendTitle = degree.extend_title
                if (extendTitle.isNullOrBlank()) {
                    binding.extendTitleLl.visibility = View.GONE
                } else {
                    binding.extendTitleLl.visibility = View.VISIBLE
                    binding.extendTitleTv.text = extendTitle
                }
                binding.extendValueEt.setText("")
                eduRequest.vocational_course = ""

                degreeDialog?.dismiss()

            }
        }
    }

    override fun initData() {
        presenter = CvFormPresenter(this)
        presenter.getEduDet(draftEduId)
        presenter.getDegreeList()

        startDialog = BusinessUtils.showYearDialog(this, object : OnWorkTimeSelected {
            override fun onWorkTimeSelected(string: String) {
                binding.startDateTv.text = string
            }
        })

        endDialog = BusinessUtils.showYearDialog(this, object : OnWorkTimeSelected {
            override fun onWorkTimeSelected(string: String) {
                binding.endDateTv.text = string
            }
        })


    }

    override fun bindListener() {
        binding.schoolNameEt.addTextChangedListener(afterTextChanged = {
            eduRequest.educational_attainment = it.toString().trim()
            handleClickable()
        })

        binding.extendValueEt.addTextChangedListener(afterTextChanged = {
            eduRequest.vocational_course = it.toString().trim()
            handleClickable()
        })
        binding.startDateTv.addTextChangedListener(afterTextChanged = {
            eduRequest.start_date = it.toString().trim()
            handleClickable()
        })

        binding.endDateTv.addTextChangedListener(afterTextChanged = {
            eduRequest.end_date = it.toString().trim()
            handleClickable()
        })

        binding.degreeTv.setOnClickListener {
            if (degreeDialog != null) {
                degreeDialog?.show()
            } else {
                showDegree()
            }
        }


        binding.deleteTv.setOnClickListener {
            AlertDialog.Builder(this, R.style.JobAlertDialogTheme)
                .setCancelable(false)
                .setTitle(R.string.hint)
                .setMessage(R.string.delete_hint)
                .setPositiveButton(R.string.confirm) { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    presenter.deleteEducation(draftEduId)
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                }
                .create().show()
        }


        binding.startDateTv.setOnClickListener {
            startDialog?.show()
        }

        binding.endDateTv.setOnClickListener {
            endDialog?.show()
        }

        binding.saveTv.setOnClickListener(object : ClickUtils.OnDebouncingClickListener(500) {
            override fun onDebouncingClick(v: View?) {

                val startDate = eduRequest.start_date
                val endDate = eduRequest.end_date

                if (!BusinessUtils.checkDateLegal(startDate, endDate,"yyyy")) {
                    ToastUtils.showShort(R.string.legal_time)
                    return
                }

                presenter.handleSaveEdu(draftEduId, eduRequest)
            }
        })

    }


    @SuppressLint("NotifyDataSetChanged")
    override fun bindEduDet(response: EduDet) {

        val schoolName = response.educational_attainment
        binding.schoolNameEt.setText(schoolName)


        // 瀛﹀巻
        val degree = response.educational_level
        lastDegreeId = degree.id
        binding.degreeTv.text = degree.educational_level
        binding.extendTitleTv.text = degree.extend_title

        mDegreeAdapter.data.forEach {
            if (it.id == lastDegreeId) {
                it.nativeSelected = true
            }
        }
        mDegreeAdapter.notifyDataSetChanged()


        val vocationalCourse = response.vocational_course
        if (vocationalCourse.isNullOrBlank()) {
            binding.extendTitleLl.visibility = View.GONE
        } else {
            binding.extendValueEt.setText(vocationalCourse)
            binding.extendTitleLl.visibility = View.VISIBLE
        }

        val startDate = response.start_date
        val endDate = response.end_date
        binding.startDateTv.text = startDate
        binding.endDateTv.text = endDate


        // 淇敼澶囩敤
        eduRequest.apply {
            this.educational_attainment = schoolName
            this.educational_level_id = degree.id
            this.vocational_course = vocationalCourse
            this.start_date = startDate
            this.end_date = endDate
        }

        // 澶勭悊鍘嗗彶鏁版嵁鏄惁鍙彁浜?
        handleClickable()

    }


    private fun handleClickable() {
        val school = eduRequest.educational_attainment
        val levelId = eduRequest.educational_level_id
        val course = eduRequest.vocational_course
        val startDate = eduRequest.start_date
        val endDate = eduRequest.end_date

        if (school.isNotBlank() && levelId != 0) {

            if (TextUtils.isEmpty(extendTitle)) {
                checkDate(startDate, endDate)
                return
            } else {
                if (course.isNullOrBlank()) {
                    binding.saveTv.isClickable = false
                    binding.saveTv.alpha = 0.5f
                    return
                } else {
                    checkDate(startDate, endDate)
                    return
                }
            }

        } else {
            binding.saveTv.isClickable = false
            binding.saveTv.alpha = 0.5f
            return
        }


    }

    private fun checkDate(startDate: String, endDate: String) {
        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            binding.saveTv.isClickable = true
            binding.saveTv.alpha = 1.0f
        } else {
            binding.saveTv.isClickable = false
            binding.saveTv.alpha = 0.5f
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun bindDegreeList(response: ArrayList<Degree>) {
        mDegreeAdapter.setNewInstance(response)

        mDegreeAdapter.data.forEach {
            if (it.id == lastDegreeId) {
                it.nativeSelected = true
            }
        }
        mDegreeAdapter.notifyDataSetChanged()

    }


    private fun showDegree() {
        val timeDialog = BottomSheetDialog(this)
        this.degreeDialog = timeDialog
        val view = LayoutInflater.from(this).inflate(R.layout.layout_select_time, null)
        val title_tv = view.findViewById<TextView>(R.id.title_tv)
        title_tv.setText(R.string.select_degree)
        timeDialog.setContentView(view)
        try {
            val parent = view.parent as ViewGroup
            parent.setBackgroundResource(android.R.color.transparent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val rv = view.findViewById<RecyclerView>(R.id.recycler_view)
        rv.apply {
            adapter = mDegreeAdapter
            addItemDecoration(DividerDecoration(ColorUtils.getColor(R.color.grey_F5F5F5), 1, SizeUtils.dp2px(15f), 0))
        }

        view.findViewById<ImageView>(R.id.close_iv).setOnClickListener {
            timeDialog.dismiss()
        }
        timeDialog.show()
    }


    override fun handleSuccess() {
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_REFRESH_CV_EDU))
        finish()
    }

    override fun deleteEducationSuccess() {
        EventBus.getDefault().post(BaseEvent(EventCode.EVENT_DELETE_CV_EDU, draftEduId))
        finish()
    }

}