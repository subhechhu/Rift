package com.np.rift.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.np.rift.AppController;

/**
 * Created by subhechhu on 11/22/2017.
 */

public class ReferrerReceiver extends BroadcastReceiver {

    public static final String ACTION_UPDATE_DATA = "ACTION_UPDATE_DATA";
    private static final String ACTION_INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";
    private static final String KEY_REFERRER = "referrer";

    SharedPrefUtil sharedPrefUtil = new SharedPrefUtil();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", "subhechhu onReceive");
        if (intent == null) {
            Log.e("ReferrerReceiver", "Intent is null");
            return;
        }
        if (!ACTION_INSTALL_REFERRER.equals(intent.getAction())) {
            Log.e("ReferrerReceiver", "Wrong action! Expected: " + ACTION_INSTALL_REFERRER + " but was: " + intent.getAction());
            return;
        }
        Bundle extras = intent.getExtras();
        if (intent.getExtras() == null) {
            Log.e("ReferrerReceiver", "No data in intent");
            return;
        }
        Log.e("TAG", "subhechhu referer: " + intent.getStringExtra("referrer"));
        sharedPrefUtil.setSharedPreferenceString(AppController.getContext(),"referral",intent.getStringExtra("referrer"));
    }
}