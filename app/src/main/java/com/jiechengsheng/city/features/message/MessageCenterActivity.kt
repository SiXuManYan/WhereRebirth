package com.jiechengsheng.city.features.message

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.blankj.utilcode.util.ScreenUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.account.login.LoginActivity
import com.jiechengsheng.city.features.message.notice.SystemNoticeFragment
import com.jiechengsheng.city.storage.entity.User
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.view.empty.EmptyView
import kotlinx.android.synthetic.main.activity_message_center.*

/**
 * Created by Wangsw  2021/2/19 17:07.
 * 消息中心
 */
class MessageCenterActivity : BaseMvpActivity<MessageCenterPresenter>(), MessageCenterView {


    private var systemUnReadMessageCount = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_message_center
    }


    override fun isStatusDark(): Boolean = true


    companion object {
        fun navigation(context: Context, systemUnreadMessage: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_COUNT, systemUnreadMessage)
            }

            val intent = if (User.isLogon()) {
                Intent(context, MessageCenterActivity::class.java)
                    .putExtras(bundle)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            } else {
                Intent(context, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun initView() {
        systemUnReadMessageCount = intent.getIntExtra(Constant.PARAM_COUNT, 0)
        pager.offscreenPageLimit = 1

    }

    override fun initData() {
        presenter = MessageCenterPresenter(this)

        val emptyView = EmptyView(this)
        val layoutParams = emptyView.parent_ll.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()

        val adapter = InnerPagerAdapter(supportFragmentManager, 0)
        adapter.emptyView = emptyView
        emptyView.setEmptyImage(R.mipmap.ic_empty_un_notice)
        emptyView.setEmptyHint(R.string.no_notice_yet)
        pager!!.adapter = adapter
        tabs_type!!.visibility = View.GONE

        handleCount()

    }

    private fun handleCount() {
        if (systemUnReadMessageCount > 0) {
            is_read_iv.visibility = View.VISIBLE
        } else {
            is_read_iv.visibility = View.GONE
        }
    }

    override fun bindListener() {}

    private class InnerPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

        var emptyView: EmptyView? = null

        override fun getItem(position: Int): Fragment {
            emptyView!!.showEmptyContainer()
            return SystemNoticeFragment()
        }

        override fun getCount(): Int {
            return 1
        }
    }

    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        when (baseEvent.code) {
            EventCode.EVENT_GET_MESSAGE_COUNT -> {
                presenter.getUnreadMessageCount()
            }
            else -> {}
        }

    }

    override fun bindUnreadMessageCount(apiUnreadMessageCount: Int) {
        handleCount()
    }


}
