package com.np.rift.main.aboutFragment;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.np.rift.R;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class EditFragment extends BottomSheetDialogFragment {

    EditText editText_userName, editText_userEmail;
    Button button_save;
    TextView textView_message;

    AVLoadingIndicatorView progress_primary;

    View contentView;
    String userName, userEmail;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_edit, null);
        dialog.setContentView(contentView);

        progress_primary = contentView.findViewById(R.id.progress_primary);
        Progress(false);

        editText_userName = contentView.findViewById(R.id.editText_userName);
        editText_userEmail = contentView.findViewById(R.id.editText_userEmail);

        textView_message = contentView.findViewById(R.id.textView_message);

        Log.e("TAG", "textView_userName: " + getArguments().getString("email"));
        Log.e("TAG", "textView_userName: " + getArguments().getString("userName"));

        userEmail = getArguments().getString("email");
        userName = getArguments().getString("userName");

        editText_userEmail.setText(userEmail);
        editText_userName.setText(userName);

        button_save = contentView.findViewById(R.id.button_save);

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText_userName.getText().toString().isEmpty() ||
                        editText_userEmail.getText().toString().isEmpty()) {
                    textView_message.setText("Fields cannot be empty!!");
                    textView_message.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
                } else {
                    //TODO call api to check
                }
            }
        });

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

}