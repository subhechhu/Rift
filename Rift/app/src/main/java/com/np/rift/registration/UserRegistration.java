package com.np.rift.registration;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.init.LoginActivity;
import com.np.rift.serverRequest.ServerPostRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class UserRegistration extends AppCompatActivity implements ServerPostRequest.Response {

    AVLoadingIndicatorView progress_primary;

    EditText editText_userName, editText_userEmail;
    Button button_proceed;
    View linearlayout_main;

    String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_registration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Register");

        email = getIntent().getStringExtra("email");

        progress_primary = (AVLoadingIndicatorView) findViewById(R.id.progress_primary);

        editText_userEmail = (EditText) findViewById(R.id.editText_userEmail);
        editText_userName = (EditText) findViewById(R.id.editText_userName);

        editText_userEmail.setText(email);

        button_proceed = (Button) findViewById(R.id.button_proceed);

        linearlayout_main = findViewById(R.id.linearlayout_main);

        button_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText_userEmail.getText().toString().isEmpty()
                        && !editText_userName.getText().toString().isEmpty()) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(editText_userEmail.getText().toString()).matches()) {
                        RegisterUser(editText_userEmail.getText().toString(), editText_userName.getText().toString());
                    } else {
                        showSnackBar("Invalid Email.");
                    }
                } else {
                    showSnackBar("Email & username cannot be empty");
                }
            }
        });
    }

    private void RegisterUser(String email, String username) {
        try {
            Progress(true);
            JSONObject object = new JSONObject();
            object.put("userEmail", email);
            object.put("userName", username);

            String url = AppController.getInstance().getString(R.string.domain) + "/registerUser";
            new ServerPostRequest(this, "USER_REG").execute(url, object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void getPostResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            try {
                Log.e("TAG", "114");
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                if ("success".equalsIgnoreCase(status)) {
                    Log.e("TAG", "119");
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("fromUserReg", true);
                    intent.putExtra("email",editText_userEmail.getText().toString());
                    startActivity(intent);
                    finish();
//                    Intent intent = new Intent(this, OTPActivity.class);
//                    intent.putExtra("email", editText_userEmail.getText().toString());
//                    startActivity(intent);
//                    finish();
                } else {
                    String errorMessage = responseObject.getString("errorMessage");
                    showSnackBar(errorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showSnackBar("Something Went Wrong. Please Try Again!!");
        }
    }
}
