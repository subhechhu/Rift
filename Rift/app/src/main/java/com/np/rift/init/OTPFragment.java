package com.np.rift.init;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.connection.NetworkCheck;
import com.np.rift.main.HomeActivity;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class OTPFragment extends BottomSheetDialogFragment implements ServerGetRequest.Response {

    EditText editText_otp;
    TextView textView_error;
    Button proceed;
    View contentView;
    AVLoadingIndicatorView progress_primary;
    SharedPrefUtil sharedPrefUtil;

    String email;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_otp, null);
        dialog.setContentView(contentView);

        email = getArguments().getString("email");
        Log.e("TAG", "email: " + email);
        sharedPrefUtil = new SharedPrefUtil();

        editText_otp = contentView.findViewById(R.id.editText_otp);
        textView_error = contentView.findViewById(R.id.textView_error);
        progress_primary = contentView.findViewById(R.id.progress_primary);
        proceed = contentView.findViewById(R.id.button_proceed);
        Progress(false);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    if (!editText_otp.getText().toString().isEmpty()) {
                        Progress(true);
                        VerifyOTP(editText_otp.getText().toString(), email);
                    }
                }else {
                    textView_error.setText("No Internet.");
                    textView_error.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
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
                    startActivity(new Intent(getActivity(), HomeActivity.class));
                    getActivity().finish();
                } else {
                    Progress(false);
                    textView_error.setText("Invalid OTP.\nPlease Check Your Email & Try Again");
                    textView_error.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            textView_error.setText("Something went wrong. Please Try Again!!!");
            textView_error.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
        }
    }
}