package com.np.rift.init;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.HomeActivity;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class OTPActivity extends AppCompatActivity implements ServerGetRequest.Response {
    String TAG = getClass().getSimpleName();

    View linearlayout_main;
    AVLoadingIndicatorView progress_primary;
    Button button_proceed;
    EditText editText_otp;

    SharedPrefUtil sharedPrefUtil;

    String email;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_otp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("OTP Verification");


        sharedPrefUtil = new SharedPrefUtil();

        email = getIntent().getStringExtra("email");

        progress_primary = (AVLoadingIndicatorView) findViewById(R.id.progress_primary);
        linearlayout_main = findViewById(R.id.linearlayout_main);

        editText_otp = (EditText) findViewById(R.id.editText_otp);

        button_proceed = (Button) findViewById(R.id.button_proceed);
        button_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText_otp.getText().toString().isEmpty()) {
                    showSnackBar("OTP cannot be empty.");
                } else {
                    VerifyOTP(editText_otp.getText().toString(), email);
                }
            }
        });
    }

    private void VerifyOTP(String otp, String email) {
        otp = otp.trim();
        Progress(true);
        String url = AppController.getInstance().getString(R.string.domain) + "/verifyOTP?OTP=" + otp + "&userEmail=" + email;
        new ServerGetRequest(this, "VERIFY_OTP").execute(url);
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

    private void Progress(boolean show) {
        if (show) {
            progress_primary.show();
        } else {
            progress_primary.hide();
        }
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                if ("success".equals(status)) {
                    sharedPrefUtil.setSharedPreferenceString(AppController.getContext(),
                            "userName", responseObject.getString("userName"));
                    sharedPrefUtil.setSharedPreferenceString(AppController.getContext(),
                            "userId", responseObject.getString("userId"));
                    sharedPrefUtil.setSharedPreferenceString(AppController.getContext(),
                            "userEmail", responseObject.getString("userEmail"));
                    sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(),
                            "verified", true);
                    startActivity(new Intent(this,HomeActivity.class));
                    finish();
                } else {
                    Progress(false);
                    showSnackBar("Invalid OTP.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showSnackBar("Something went wrong. Please Try Again!!!");
        }
    }
}
