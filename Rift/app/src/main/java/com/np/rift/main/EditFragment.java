package com.np.rift.main;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.serverRequest.ServerPostRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class EditFragment extends BottomSheetDialogFragment implements ServerPostRequest.Response {

    EditText editText_userName, editText_userEmail;
    TextView textView_message;

    AVLoadingIndicatorView progress_primary;

    View contentView;
    String userName, userEmail;
    Button button_save;

    String forView;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_edit, null);
        dialog.setContentView(contentView);

        progress_primary = contentView.findViewById(R.id.progress_primary);
        Progress(false);

        forView = getArguments().getString("for");

        editText_userName = contentView.findViewById(R.id.editText_userName);
        editText_userEmail = contentView.findViewById(R.id.editText_userEmail);
        button_save = contentView.findViewById(R.id.button_save);
        textView_message = contentView.findViewById(R.id.textView_message);

        if ("profile".equals(forView)) {
            userEmail = getArguments().getString("email");
            userName = getArguments().getString("userName");

            editText_userEmail.setText(userEmail);
            editText_userName.setText(userName);
        } else {
            userName = getArguments().getString("groupName");
            editText_userName.setText(userName);
            editText_userName.setHint("Group Name");
        }

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("profile".equals(forView)) {
                    if (editText_userEmail.getText().toString().isEmpty() &&
                            editText_userName.getText().toString().isEmpty()) {
                        textView_message.setText(AppController.getInstance().getString(R.string.fields_empty));
                        textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                    } else {
                        requestChangeDetails(editText_userEmail.getText().toString(),
                                editText_userName.getText().toString(), "profile");
                    }
                } else {
                    if (editText_userName.getText().toString().isEmpty()) {
                        textView_message.setText(AppController.getInstance().getString(R.string.fields_empty));
                        textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                    } else {
                        requestChangeDetails(editText_userEmail.getText().toString(),
                                editText_userName.getText().toString(), "group");
                    }
                }
            }
        });

        if ("profile".equals(forView)) {
            userEmail = getArguments().getString("email");
            userName = getArguments().getString("userName");

            editText_userEmail.setText(userEmail);
            editText_userName.setText(userName);
        } else {
            textView_message.setText(AppController.getInstance().getString(R.string.enter_new_group_name));
            userName = getArguments().getString("groupName");
            editText_userName.setText(userName);
            editText_userName.setVisibility(View.GONE);
        }
    }

    private void requestChangeDetails(String userEmail, String userName, String forView) {
        String url = AppController.getInstance().getString(R.string.domain) + "/changeDetails";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("for", forView);
            jsonObject.put("newEmail", userEmail);
            jsonObject.put("newUserName", userName);

            Log.e("TAG", "jsonObject: " + jsonObject.toString());

//            new ServerPostRequest(this, "CHANGE_CREDENTIALS").execute(url, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(contentView, message, Snackbar.LENGTH_LONG);
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
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                if ("success".equals(status)) {
                    AppController.setUserEmail(editText_userEmail.getText().toString());
                    AppController.setUserName(editText_userName.getText().toString());
                } else {
                    String errorMessage = responseObject.getString("errorMessage");
                    textView_message.setText(errorMessage);
                    textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            textView_message.setText(AppController.getInstance().getString(R.string.something_went_wrong));
            textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
        }
    }
}