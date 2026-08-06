package com.jiechengsheng.city.features.job.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonElement
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.network.BaseMvpObserver
import com.jiechengsheng.city.api.network.BaseMvpPresenter
import com.jiechengsheng.city.api.network.BaseMvpView
import com.jiechengsheng.city.api.request.MtjClickHomeJob
import com.jiechengsheng.city.api.response.job.JobCollection
import com.jiechengsheng.city.api.response.job.JobDetail
import com.jiechengsheng.city.api.response.job.JobSendCv
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.job.company.CompanyActivity
import com.jiechengsheng.city.features.job.report.ReportActivity
import com.jiechengsheng.city.frames.common.Html5Url
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.utils.MobUtil
import com.jiechengsheng.city.utils.image.GlideRoundedCornersTransform
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityJobDetailBinding

/**
 * Created by Wangsw  2022/9/28 14:04.
 *  鑱屼綅璇︽儏
 */
class JobDetailActivity : BaseMvpActivity<JobDetailPresenter>(), JobDetailView {

    private lateinit var binding: ActivityJobDetailBinding


    var jobId = 0
    var isCollect = false

    /** 娣诲姞绠€鍘嗗悗锛屽埛鏂版湰椤电姸鎬?*/
    private var needRefreshForCv = false

    /** 鏄惁閫氳繃鎷涜仒棣栭〉鐐瑰嚮 */
    private var isFromJobHome = false

    private var degreeDialog: BottomSheetDialog? = null

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_job_detail


    companion object {
        fun navigation(context: Context, jobId: Int, fromJobHome: Boolean? = false) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_ID, jobId)

                fromJobHome?.let {
                    putBoolean(Constant.PARAM_TYPE, it)
                }
            }
            val intent = Intent(context, JobDetailActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun initView() {
        binding = ActivityJobDetailBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, Color.WHITE)
        jobId = intent.getIntExtra(Constant.PARAM_ID, 0)
        isFromJobHome = intent.getBooleanExtra(Constant.PARAM_TYPE, false)
    }

    override fun initData() {
        presenter = JobDetailPresenter(this)
        presenter.getData(jobId)
        if (isFromJobHome) {
            presenter.mtjClickHomeJob(jobId)
        }
    }

    override fun bindListener() {
        binding.collectIv.setOnClickListener {
            if (!User.isLogon()) {
                startActivity(LoginActivity::class.java)
                return@setOnClickListener
            }
            presenter.handleCollection(isCollect, jobId)
        }

        binding.reportIv.setOnClickListener {
            startActivityAfterLogin(ReportActivity::class.java, Bundle().apply {
                putInt(Constant.PARAM_ID, jobId)
            })
        }

        binding.shareIv.setOnClickListener {
            val url = String.format(Html5Url.SHARE_FACEBOOK, Html5Url.MODEL_JOB, jobId)
            MobUtil.shareFacebookWebPage(url, this)
        }

        binding.companyRv.setOnClickListener {
            CompanyActivity.navigation(this, jobId)
        }

    }

    override fun bindDetail(response: JobDetail) {
        binding.jobTitleTv.text = response.job_title
        binding.dutyTv.text = response.duty
        binding.jobRqTv.text = response.requirement


        binding.salaryTv.text = response.salary
        binding.cityTv.text = response.city
        binding.createTimeTv.text = response.created_at

        isCollect = response.is_collect
        setLikeImage()


        if (response.is_send) {
            setSendSuccessUi()
        } else {
            binding.applyTv.text = getString(R.string.apply_now)
            binding.applyTv.alpha = 1.0f
            binding.applyTv.isClickable = true
            binding.applyTv.setOnClickListener {

                if (!User.isLogon()) {
                    startActivity(LoginActivity::class.java)
                    return@setOnClickListener
                }
                if (response.is_complete && response.is_complete_pdf) {
                    // 鐢宠鑱屼綅
                    presenter.sendCV(jobId)
                } else {
                    if (degreeDialog != null) {
                        degreeDialog?.show()
                    } else {
                        showDegree()
                    }
                }
            }
        }

        response.company_info?.let {
            GlideUtil.load(this, it.logo, binding.logoIv, 24, GlideRoundedCornersTransform.CornerType.ALL, R.mipmap.ic_company_default_logo)
            binding.companyNameTv.text = it.company_title
            binding.companyDescTv.text = it.profile
        }


    }

    private fun setSendSuccessUi() {
        binding.applyTv.text = getString(R.string.applied)
        binding.applyTv.alpha = 0.5f
        binding.applyTv.isClickable = false
    }


    override fun sendSuccess(response: JsonElement) {
        ToastUtils.showShort(R.string.successful_delivery)
        EventBus.getDefault().post(BaseEvent<Any>(EventCode.EVENT_CV_SEND_SUCCESS))
        setSendSuccessUi()
    }

    override fun collectionResult(result: Boolean) {
        isCollect = result
        setLikeImage()
        if (result) {
            ToastUtils.showShort(R.string.collection_success)
        } else {
            ToastUtils.showShort(R.string.cancel_collection_success)
        }

    }


    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        when (baseEvent.code) {
            EventCode.EVENT_LOGIN_SUCCESS -> {
                presenter.getData(jobId)
            }
            EventCode.EVENT_REFRESH_CV_PROFILE,
            EventCode.EVENT_REFRESH_CV_EXPERIENCE,
            -> {
                needRefreshForCv = true
            }

            else -> {}
        }

    }


    override fun onResume() {
        super.onResume()
        if (needRefreshForCv) {
            presenter.getData(jobId)
            needRefreshForCv = false
        }

    }


    private fun setLikeImage() {

        binding.collectIv.setImageResource(
            if (isCollect) {

                R.mipmap.ic_like_red_night
            } else {
                R.mipmap.ic_like_normal_night
            }
        )
    }


    private fun showDegree() {
        val timeDialog = BottomSheetDialog(this)
        this.degreeDialog = timeDialog
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_complete_cv, null)
        timeDialog.setContentView(view)
        try {
            val parent = view.parent as ViewGroup
            parent.setBackgroundResource(android.R.color.transparent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        view.findViewById<Button>(R.id.ok).setOnClickListener {
            timeDialog.dismiss()
        }
        timeDialog.show()
    }


}


interface JobDetailView : BaseMvpView {
    fun bindDetail(response: JobDetail)
    fun sendSuccess(response: JsonElement)
    fun collectionResult(result: Boolean)

}


class JobDetailPresenter(var view: JobDetailView) : BaseMvpPresenter(view) {
    fun getData(jobId: Int) {
        requestApi(mRetrofit.jobDetail(jobId, 2), object : BaseMvpObserver<JobDetail>(view) {
            override fun onSuccess(response: JobDetail) {
                view.bindDetail(response)
            }

        })
    }


    /**
     * 鍙戦€佺畝鍘?锛?鐢宠鑱屼綅
     */
    fun sendCV(jobId: Int) {

        val apply = JobSendCv().apply {
            job_id = jobId
        }

        requestApi(mRetrofit.jobSendCv(apply), object : BaseMvpObserver<JsonElement>(view) {
            override fun onSuccess(response: JsonElement) {
                view.sendSuccess(response)
            }
        })
    }

    /**
     * 缁熻浠庢嫑鑱橀椤佃亴浣嶅垪琛ㄥ叆鍙ｇ偣鍑?
     */
    fun handleCollection(collect: Boolean, jobId: Int) {
        val apply = JobCollection().apply {
            job_id = jobId
        }

        if (collect) {
            requestApi(mRetrofit.jobDelCollection(apply), object : BaseMvpObserver<JsonElement>(view) {
                override fun onSuccess(response: JsonElement) {
                    view.collectionResult(false)
                }
            })
        } else {
            requestApi(mRetrofit.jobCollection(apply), object : BaseMvpObserver<JsonElement>(view) {
                override fun onSuccess(response: JsonElement) {
                    view.collectionResult(true)
                }
            })
        }


    }

    fun mtjClickHomeJob(jobId: Int) {
        val apply = MtjClickHomeJob().apply {
            job_id = jobId
        }
        requestApi(mRetrofit.mtjClickHomeJob(apply), object : BaseMvpObserver<JsonElement>(view) {
            override fun onSuccess(response: JsonElement) {

            }
        })

    }

}
