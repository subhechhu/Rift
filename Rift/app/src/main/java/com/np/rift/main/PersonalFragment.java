package com.np.rift.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.np.rift.R;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class PersonalFragment extends Fragment {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    FloatingActionButton fab_add;

    float dX;
    float dY;
    int lastAction;


    public static PersonalFragment init(int position) {
        PersonalFragment frndsInviteFragment = new PersonalFragment();
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

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentValue = getArguments() != null ? getArguments().getInt("val") : 1;
        Log.d("subhechhu", "A onCreate" + fragmentValue);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_personal, container, false);
        fab_add = fragmentView.findViewById(R.id.fab_add);

        fab_add.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        v.setY(event.getRawY() + dY);
                        v.setX(event.getRawX() + dX);
                        lastAction = MotionEvent.ACTION_MOVE;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN)
                            startActivity(new Intent(mActivity, AddExpenseActivity.class));
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
        return fragmentView;
    }
}