package com.np.rift.main.groupFragment;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
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

public class JoinGroupFragment extends BottomSheetDialogFragment implements ServerPostRequest.Response {


    View contentView;
    AVLoadingIndicatorView progress_primary;
    EditText editText_groupName, editText_groupPurpose;
    TextView textView_message;
    Button button_proceed;
    String purpose;
    RefreshGroup refreshGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        refreshGroup = (RefreshGroup) context;
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_group_options, null);
        dialog.setContentView(contentView);

        progress_primary = contentView.findViewById(R.id.progress_primary);
        progress(false);

        purpose = getArguments().getString("purpose");

        textView_message = contentView.findViewById(R.id.textView_message);

        editText_groupName = contentView.findViewById(R.id.editText_groupName);
        editText_groupPurpose = contentView.findViewById(R.id.editText_groupPurpose);

        button_proceed = contentView.findViewById(R.id.button_proceed);
        button_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView_message.setText(AppController.getInstance().getString(R.string.group_registration));
                if (editText_groupName.getText().toString().isEmpty() &&
                        editText_groupPurpose.getText().toString().isEmpty()) {
                    textView_message.setText("Fields Cannot Be Empty");
                    textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                } else {
                    progress(true);
                    if ("Join".equals(purpose)) {
                        try {
                            String url = AppController.getInstance().getString(R.string.domain)
                                    + "/joinGroup";
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("groupName", editText_groupName.getText().toString());
                            jsonObject.put("groupId", editText_groupPurpose.getText().toString());
                            jsonObject.put("userId", AppController.getUserId());
                            jsonObject.put("userName", AppController.getUserName());
                            RequestJoin("JOIN_GROUP", url, jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            String url = AppController.getInstance().getString(R.string.domain)
                                    + "/registerGroup";
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("groupName", editText_groupName.getText().toString());
                            jsonObject.put("groupPurpose", editText_groupPurpose.getText().toString());
                            jsonObject.put("userId", AppController.getUserId());
                            jsonObject.put("userName", AppController.getUserName());
                            jsonObject.put("settled", false);

                            RequestJoin("ADD_GROUP", url, jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        getDialog().dismiss();

        if ("Join".equals(purpose)) {
            textView_message.setText("Get the Group name & ID from other group member");
            editText_groupPurpose.setHint("Group ID");
        }
    }

    private void RequestJoin(String purpose, String url, JSONObject jsonObject) {
        new ServerPostRequest(this, purpose).execute(url, jsonObject.toString());
    }

    private void progress(boolean show) {
        if (show) {
            progress_primary.show();
        } else {
            progress_primary.hide();
        }
    }

    @Override
    public void getPostResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            Log.e("TAG", "response: " + response);
            try{
                JSONObject responseObject=new JSONObject(response);
                String status=responseObject.getString("status");
                if("success".equalsIgnoreCase(status)){
                    getDialog().dismiss();
                    refreshGroup.refreshGroup("Group added. Please refresh to view the changes");
                }else {
                    String errorMsg=responseObject.getString("errorMessage");
                    textView_message.setText(errorMsg);
                    textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        } else {
            progress(false);
            textView_message.setText("Something went wrong. Please Try Again!!");
            textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
        }
    }

    public interface RefreshGroup {
        public void refreshGroup(String message);
    }
}