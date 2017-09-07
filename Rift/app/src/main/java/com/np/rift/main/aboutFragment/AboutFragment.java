package com.np.rift.main.aboutFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class AboutFragment extends Fragment {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    TextView textView_userEmail, textView_userName;
    Button button_edit;

    public static AboutFragment init(int position) {
        AboutFragment frndsInviteFragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt("val", position);
        frndsInviteFragment.setArguments(args);
        return frndsInviteFragment;
    }

    protected FragmentActivity getActivityReference() {
        Log.d(TAG, "FragmentActivity()");
        if (mActivity == null) {
            mActivity = getActivity();
        }
        return mActivity;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
//        ((HomeActivity) getActivityReference()).fragmentOnCreateSuccess(this, fragmentValue);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentValue = getArguments() != null ? getArguments().getInt("val") : 1;
        Log.d("subhechhu", "C onCreate" + fragmentValue);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_about, container, false);

        textView_userEmail = fragmentView.findViewById(R.id.textView_userEmail);
        textView_userName = fragmentView.findViewById(R.id.textView_userName);
        button_edit = fragmentView.findViewById(R.id.button_edit);

        textView_userName.setText(AppController.getUserName());
        textView_userEmail.setText(AppController.getUserEmail());

        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Log.e("TAG","textView_userEmail.getText().toString(): "+textView_userEmail.getText().toString());
                Log.e("TAG","textView_userEmail.getText().toString(): "+textView_userName.getText().toString());
                bundle.putString("email", textView_userEmail.getText().toString());
                bundle.putString("userName", textView_userName.getText().toString());
                BottomSheetDialogFragment fragment = new EditFragment();
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), fragment.getTag());
            }
        });

        return fragmentView;

    }
}
