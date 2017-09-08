package com.np.rift.main.groupFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.np.rift.R;

/**
 * Created by subhechhu on 9/7/2017.
 */

public class InnerGroupFragment extends Fragment {
    protected GroupActivity mActivity;
    String TAG = getClass().getSimpleName();

    int fragmentValue;

    TextView textView_user, textView_expense, textView_more;
    String userName, userId, userExpense, groupId;
    View linearlayout_main;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        if (context instanceof GroupActivity) {
            mActivity = (GroupActivity) context;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentValue = getArguments() != null ? getArguments().getInt("val") : 1;
        userId = getArguments().getString("memberId");
        userName = getArguments().getString("memberNames");
        userExpense = getArguments().getString("memberExpense");
        groupId = getArguments().getString("groupId");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_inner_group, container, false);

        textView_user = fragmentView.findViewById(R.id.textView2_user);
        textView_expense = fragmentView.findViewById(R.id.textView_expense);

        textView_more = fragmentView.findViewById(R.id.textView_more);

        linearlayout_main = fragmentView.findViewById(R.id.linearlayout_main);

        textView_user.setText(userName);
        textView_expense.setText(userExpense);

        textView_more.setText("Check " + userName + "'s contribution in details here");
        textView_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("userName", userName);
                bundle.putString("userId", userId);
                bundle.putString("groupId", groupId);
                bundle.putString("userExpense", userExpense);
                BottomSheetDialogFragment fragment = new GroupExpenseFragment();
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), fragment.getTag());
            }
        });
        return fragmentView;
    }
}
