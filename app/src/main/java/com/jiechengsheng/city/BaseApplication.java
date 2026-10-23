package com.jiechengsheng.city;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.multidex.MultiDex;

import com.jiechengsheng.city.api.RetrofitManager;
import com.jiechengsheng.city.features.account.login.LoginActivity;
import com.jiechengsheng.city.storage.WhereDataBase;
import com.jiechengsheng.city.utils.CrashHandler;
import com.jiechengsheng.city.utils.LocalLanguageUtil;
import com.jiechengsheng.city.utils.SPUtil;

public class BaseApplication extends Application {


    private WhereDataBase database = null;

    public WhereDataBase getDatabase() {
        return database;
    }

    private static Context mApplicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationContext = this;
        database = WhereDataBase.get(this);
        RetrofitManager.getManager().initRetrofit(this);
//        LocationUtil.initInstance(this);
        SPUtil.initInstance(this);
        changeLanguage();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
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

    public static void toLogin() {
        Intent toLogin = new Intent(mApplicationContext, LoginActivity.class);
        toLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApplicationContext.startActivity(toLogin);
    }

}
