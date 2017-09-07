package com.np.rift;

import android.app.Application;
import android.content.Context;

import com.np.rift.util.SharedPrefUtil;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class AppController extends Application {
    static SharedPrefUtil sharedPrefUtil;
    private static Context mContext;
    private static AppController mInstance;

    public static Context getContext() {
        return mContext;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public static String getUserId() {
        return sharedPrefUtil.getSharedPreferenceString(mContext, "userId", "0");
    }

    public static String getUserName() {
        return sharedPrefUtil.getSharedPreferenceString(mContext, "userName", "0");
    }

    public static String getUserEmail() {
        return sharedPrefUtil.getSharedPreferenceString(mContext, "userEmail", "0");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mInstance = this;
        sharedPrefUtil = new SharedPrefUtil();
    }
}
