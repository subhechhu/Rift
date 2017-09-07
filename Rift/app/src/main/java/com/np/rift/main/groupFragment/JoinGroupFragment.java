package com.np.rift.main.groupFragment;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.np.rift.R;
import com.np.rift.main.HomeActivity;
import com.np.rift.serverRequest.ServerPostRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class JoinGroupFragment extends BottomSheetDialogFragment implements ServerPostRequest.Response {


    View contentView;
    AVLoadingIndicatorView progress_primary;
    EditText editText_groupName, editText_groupPurpose;
    TextView textView_message;
    Button button_proceed;

    String purpose;


    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_group_options, null);
        dialog.setContentView(contentView);

        progress_primary = contentView.findViewById(R.id.progress_primary);
        Progress(false);

        purpose = getArguments().getString("purpose");

        textView_message = contentView.findViewById(R.id.textView_message);

        editText_groupName = contentView.findViewById(R.id.editText_groupName);
        editText_groupPurpose = contentView.findViewById(R.id.editText_groupPurpose);

        button_proceed = contentView.findViewById(R.id.button_proceed);
        button_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText_groupName.getText().toString().isEmpty() &&
                        editText_groupPurpose.getText().toString().isEmpty()) {
                    textView_message.setText("Fields Cannot Be Empty");
                    textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                } else {
//                    if ("Join".equals(purpose)) {
//                        //TODO API CALL
//                        try {
//                            String url="";
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("groupName", editText_groupName.getText().toString());
//                            jsonObject.put("groupId", editText_groupPurpose.getText().toString());
//
//                            RequestJoin("JOIN_GROUP",url, jsonObject);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        //TODO API CALL
//                        try {String url="";
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("groupName", editText_groupName.getText().toString());
//                            jsonObject.put("groupPurpose", editText_groupPurpose.getText().toString());
//
//                            RequestJoin("ADD_GROUP",url, jsonObject);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
                    ((HomeActivity)getActivity()).refreshViewpager();
                    getDialog().dismiss();
                }
            }
        });

        if ("Join".equals(purpose)) {
            textView_message.setText("Get the Group name & ID from other group member");
            editText_groupPurpose.setHint("Group ID");
        }
    }

    private void RequestJoin(String purpose,String url, JSONObject jsonObject) {
        new ServerPostRequest(this, purpose).execute(url, jsonObject.toString());
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
        if(response!=null && !response.isEmpty()){
            ((HomeActivity)getActivity()).refreshViewpager();
        }else {
            Progress(false);
            textView_message.setText("Something went wrong. Please Try Again!!");
            textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
        }
    }
}