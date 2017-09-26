package com.np.rift.main.notificationFragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.menuOptions.EditFragment;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class NotificationFragment extends Fragment {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    TextView textView_userEmail, textView_userName, textView_version;
    Button button_editusername, button_editemail, button_share;

    public static NotificationFragment init(int position) {
        NotificationFragment frndsInviteFragment = new NotificationFragment();
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
        View fragmentView = inflater.inflate(R.layout.fragment_notification, container, false);

        return fragmentView;
    }
}
