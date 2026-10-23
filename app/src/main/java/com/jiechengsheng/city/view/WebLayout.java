package com.jiechengsheng.city.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.jiechengsheng.city.R;
import com.just.agentweb.IWebLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WebLayout implements IWebLayout {

    private final ViewGroup mContainer;
    private final WebView mWebView;

    public WebLayout(Activity activity) {
        mContainer = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.view_agent_web, null);
        mWebView = mContainer.findViewById(R.id.webView);
    }

    @NonNull
    @Override
    public ViewGroup getLayout() {
        return mContainer;
    }

    @Nullable
    @Override
    public WebView getWebView() {
        return mWebView;
    }
}
