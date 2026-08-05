package com.jiechengsheng.city;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.jiechengsheng.city.api.RetrofitManager;
import com.jiechengsheng.city.features.account.login.LoginActivity;
import com.jiechengsheng.city.storage.WhereDataBase;
import com.jiechengsheng.city.utils.CacheUtil;
import com.jiechengsheng.city.utils.CrashHandler;
import com.jiechengsheng.city.utils.LocalLanguageUtil;
import com.jiechengsheng.city.utils.SPUtil;

import java.util.HashSet;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;

public class BaseApplication extends Application {


    private WhereDataBase database = null;

    public WhereDataBase getDatabase() {
        return database;
    }

    private static Context mApplicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
        mApplicationContext = this;
        database = WhereDataBase.get(this);
        RetrofitManager.getManager().initRetrofit(this);
//        LocationUtil.initInstance(this);
        SPUtil.initInstance(this);
        changeLanguage();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        initJpush();
    }


    public void changeLanguage() {
        LocalLanguageUtil.getInstance().setApplicationLanguage(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        SPUtil.initInstance(base);
        super.attachBaseContext(LocalLanguageUtil.getInstance().setLocal(base));
        MultiDex.install(this);
    }


    /** 初始化极光推送。 */
    private void initJpush() {
        // 极光推送
        if (BuildConfig.DEBUG) {
            JPushInterface.setDebugMode(true);
        }

        HashSet<String> tag = new HashSet<>();

        // 根据开发环境设置tag
        if (BuildConfig.FLAVOR == "formal") {
            tag.add("formal");
        }
        if (BuildConfig.FLAVOR == "dev") {
            tag.add("develop");
        }

        int sequence = Math.abs((int) System.currentTimeMillis());
        JPushInterface.setTags(this, sequence, tag);


        // 用户同意协议后的真正注册
        boolean isAgree = CacheUtil.isAgreeUserAgreement();
        if (isAgree) {
            JPushInterface.init(this);
        }

        BasicPushNotificationBuilder builder =
                new BasicPushNotificationBuilder(this.getApplicationContext());

        builder.statusBarDrawable = R.drawable.ic_notification;
        builder.notificationFlags = Notification.FLAG_SHOW_LIGHTS;
        builder.notificationDefaults = Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS;


        JPushInterface.setDefaultPushNotificationBuilder(builder);

        if (Build.VERSION.SDK_INT > 23) {
            // 设置保留最近通知条数 API
            JPushInterface.setLatestNotificationNumber(this.getApplicationContext(), 5);
        }

        if (BuildConfig.DEBUG) {
            JPushInterface.getConnectionState(this);
        }

    }

    public static void toLogin() {
        Intent toLogin = new Intent(mApplicationContext, LoginActivity.class);
        toLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApplicationContext.startActivity(toLogin);
    }

}
