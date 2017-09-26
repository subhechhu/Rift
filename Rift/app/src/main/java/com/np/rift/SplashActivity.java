package com.np.rift;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.np.rift.connection.ConnectionLost;
import com.np.rift.connection.NetworkCheck;
import com.np.rift.infoScreen.InfoActivity;
import com.np.rift.init.LoginActivity;
import com.np.rift.main.HomeActivity;
import com.np.rift.util.SharedPrefUtil;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private SharedPrefUtil sharedPrefUtil;
    private boolean newApp = false; // boolean flag to check if the app is new or not
    private View mainRelative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removing the action bars to make activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        mainRelative = findViewById(R.id.mainRelative);

        sharedPrefUtil = new SharedPrefUtil();
        newApp = NewApp();
        process();
    }

    private void process() {
        if (NetworkCheck.isInternetAvailable()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (newApp) {
                        startActivity(new Intent(SplashActivity.this, InfoActivity.class));
                    } else {
                        if (sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "rememberMe", false)
                                && sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "verified", false)) {
                            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }
                    }
                    finish();
                }
            }, 2500);
        }else {
            showSnackbar("No Internet","ReTry");
            ConnectionLost connectionLostFragment = new ConnectionLost();
            connectionLostFragment.show(getFragmentManager(), "dialogFragment");
        }
    }

    private boolean NewApp() {
        return sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "newApp", true);
    }

    private void showSnackbar(String message, String action) {
        final Snackbar snackBar = Snackbar.make(mainRelative,
                message,
                Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
                process();
            }
        });
        snackBar.show();
    }
}
