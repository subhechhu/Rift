package com.np.rift.main.groupFragment;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.np.rift.R;
import com.np.rift.serverRequest.ServerGetRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class GroupExpenseFragment extends BottomSheetDialogFragment implements ServerGetRequest.Response {

    View contentView;
    TextView textView_user, textView_expense;
    AVLoadingIndicatorView progress_primary;

    ExpandableListView expandableListView;
    String userName, userExpense, userId, groupId;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.fragment_view_expense, null);
        dialog.setContentView(contentView);

        userExpense = getArguments().getString("userExpense");
        userName = getArguments().getString("userName");
        userId = getArguments().getString("userId");
        groupId = getArguments().getString("groupId");


        progress_primary = contentView.findViewById(R.id.progress_default);
        expandableListView = contentView.findViewById(R.id.expandableListView);

        textView_expense = contentView.findViewById(R.id.textView_expense);
        textView_user = contentView.findViewById(R.id.textView_user);

        textView_expense.setText(userExpense);
        textView_user.setText(userName+ "'s Expense");

    }

    private void GetExpenses(String purpose, String url, JSONObject jsonObject) {
        new ServerGetRequest(this, purpose).execute(url, jsonObject.toString());
    }


    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {

    }
}