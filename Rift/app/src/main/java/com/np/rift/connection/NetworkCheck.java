package com.np.rift.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.np.rift.AppController;


public class NetworkCheck {
    private static final String TAG = NetworkCheck.class.getSimpleName();

    public static boolean isInternetAvailable() {
        NetworkInfo info = ((ConnectivityManager) AppController.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info == null) {
            return false;
        } else {
            if (info.isConnected()) {
                return true;
            } else {
                return true;
            }
        }
    }
}
