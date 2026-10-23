package com.jiechengsheng.city.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * 统一的新闻视频播放器，基于工程中已有的 GSYVideoPlayer。
 * 移除了旧的饺子播放器依赖，避免同时打包两份 ijkplayer 内核。
 */
public class JcsVideoPlayer extends StandardGSYVideoPlayer {
    private boolean mHasSetUp = false;

    public JcsVideoPlayer(Context context) {
        super(context);
    }

    public JcsVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUp(String url, String title) {
        setUp(url, true, title);
        mHasSetUp = true;
    }

    public void startVideo() {
        if (mHasSetUp) {
            startPlayLogic();
        }
    }
}
