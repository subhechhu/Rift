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


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mInstance = this;
        sharedPrefUtil = new SharedPrefUtil();
    }
}
