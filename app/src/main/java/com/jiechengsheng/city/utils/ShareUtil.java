package com.jiechengsheng.city.utils;

import android.content.Intent;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;

import com.jiechengsheng.city.R;

/** 本地演示模式下的分享入口，使用系统分享面板，不依赖商业 ShareSDK。 */
public final class ShareUtil {

    private ShareUtil() {
    }

    public static void shareLink(String webUrl, FragmentActivity activity) {
        if (activity == null || TextUtils.isEmpty(webUrl)) {
            return;
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, webUrl);
        activity.startActivity(Intent.createChooser(send, activity.getString(R.string.share_link)));
    }
}
