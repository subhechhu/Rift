package com.np.rift.init;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hanks.library.AnimateCheckBox;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.HomeActivity;
import com.np.rift.registration.UserRegistration;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class LoginActivity extends AppCompatActivity implements ServerGetRequest.Response {
    String TAG = getClass().getSimpleName();

    AVLoadingIndicatorView progress_white;
    AnimateCheckBox checkBox_autoLogin;
    EditText editText_email;
    Button button_proceed;
    TextView texView_autoLogin;
    View linearlayout_main;

    SharedPrefUtil sharedPrefUtil;
    boolean fromUserReg;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        progress_white = (AVLoadingIndicatorView) findViewById(R.id.progress_white);

        linearlayout_main = findViewById(R.id.linearlayout_main);
        fromUserReg = getIntent().getBooleanExtra("fromUserReg", false);
        if (fromUserReg) {
            Bundle bundle = new Bundle();
            bundle.putString("email", getIntent().getStringExtra("email"));
            BottomSheetDialogFragment fragment = new OTPFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        }

        checkBox_autoLogin = (AnimateCheckBox) findViewById(R.id.checkBox_autoLogin);
        editText_email = (EditText) findViewById(R.id.editText_email);
        button_proceed = (Button) findViewById(R.id.button_proceed);
        texView_autoLogin = (TextView) findViewById(R.id.texView_autoLogin);

        sharedPrefUtil = new SharedPrefUtil();

        if (sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "rememberMe", false)
                && sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "verified", false)) {
            checkBox_autoLogin.setChecked(true);
            editText_email.setText(sharedPrefUtil
                    .getSharedPreferenceString(AppController.getContext(),
                            "userEmail",
                            "Enter email"));
//            startActivity(new Intent(this, TestActivity.class));
            startActivity(new Intent(this, HomeActivity.class));
            finish();

        }
        texView_autoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox_autoLogin.isChecked()) {
                    checkBox_autoLogin.setChecked(false);
                } else {
                    checkBox_autoLogin.setChecked(true);
                }
            }
        });

        button_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText_email.getText().toString().isEmpty()) {
                    showSnackBar("Invalid email.", "OK");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editText_email.getText().toString()).matches()) {
                    showSnackBar("Invalid email.", "OK");
                } else {
                    if (checkBox_autoLogin.isChecked()) {
                        sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(), "rememberMe", true);
                    }
                    GenerateOTP(editText_email.getText().toString());
                }
            }
        });
    }

    private void GenerateOTP(String email) {
        Progress(true);
        String url = AppController.getInstance().getString(R.string.domain)
                + "/generateOTP?userEmail=" + email;
        new ServerGetRequest(this, "GENERATE_OTP").execute(url);
    }

    private void showSnackBar(String message, final String action) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_INDEFINITE);
        _snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("Create".equals(action)) {
                    Progress(false);
                    Intent intent = new Intent(LoginActivity.this, UserRegistration.class);
                    intent.putExtra("email", editText_email.getText().toString());
                    startActivity(intent);
                    finish();
                }
            }
        }).show();
    }

    private void Progress(boolean show) {
        if (show) {
            progress_white.show();
        } else {
            progress_white.hide();
        }
    }


    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            try {
                if ("GENERATE_OTP".equals(requestCode)) {
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    if ("success".equalsIgnoreCase(status)) {
                        Progress(false);
                        Bundle bundle = new Bundle();
                        bundle.putString("email", editText_email.getText().toString());
                        BottomSheetDialogFragment fragment = new OTPFragment();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
//                        Intent intent = new Intent(this, OTPActivity.class);
//                        intent.putExtra("email", editText_email.getText().toString());
//                        startActivity(intent);
//                        finish();
                    } else {
                        Progress(false);
                        String errorMessage = responseObject.getString("errorMessage");
                        if ("new".equalsIgnoreCase(errorMessage)) {
                            showSnackBar("Email not registered", "Create");
                        } else {
                            showSnackBar(errorMessage, "OK");
                        }
                    }
                } else if ("VERIFY_USER".equalsIgnoreCase(requestCode)) {
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    if ("success".equals(status)) {

                    } else {
                        String errorMessage = responseObject.getString("errorMessage");
                        showSnackBar(errorMessage, "OK");
                    }
                }
            } catch (Exception e) {
                Progress(false);
                e.printStackTrace();
            }
        } else {
            Progress(false);
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong), "OK");
        }
    }
}
