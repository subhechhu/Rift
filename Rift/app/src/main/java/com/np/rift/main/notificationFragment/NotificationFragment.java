package com.np.rift.main.notificationFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.groupFragment.GroupModel;
import com.np.rift.serverRequest.ServerGetRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class NotificationFragment extends Fragment implements ServerGetRequest.Response {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    CustomAdapterNotification customAdapterNotification;

    ArrayList<GroupModel> notificationArrayList;
    RecyclerView recycler_view;
    SwipeRefreshLayout swipeRefreshLayout;
    AVLoadingIndicatorView progress_default;
    LinearLayoutManager mLayoutManager;

    TextView textView_empty;
    LinearLayout linearlayout_main;

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
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentValue = getArguments() != null ? getArguments().getInt("val") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_notification, container, false);
        progress_default = fragmentView.findViewById(R.id.progress_default);
        linearlayout_main = fragmentView.findViewById(R.id.linearlayout_main);

        recycler_view = fragmentView.findViewById(R.id.recycler_view);
        mLayoutManager = new android.support.v7.widget.LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(mLayoutManager);
        recycler_view.setItemAnimator(new DefaultItemAnimator());


        textView_empty = fragmentView.findViewById(R.id.textView_empty);

        textView_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNotifications();
            }
        });

        swipeRefreshLayout = fragmentView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                getNotifications();
            }
        });

        getNotifications();


        return fragmentView;
    }

    public void getNotifications() {
        progress(true);
        String url = AppController.getInstance().getString(R.string.domain)
                + "/GetGrouplist?userId=" + AppController.getUserId();
//                + "/getNotification?userId=" + AppController.getUserId();
        new ServerGetRequest(this, "GET_NOTIFICATION").execute(url);
    }

    public void progress(boolean show) {
        if (show) {
            progress_default.setVisibility(View.VISIBLE);
        } else {
            progress_default.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        progress(false);
        try {
            if (response != null && !response.isEmpty()) {
                recycler_view.setVisibility(View.VISIBLE);
                textView_empty.setVisibility(View.GONE);
                ParseResponse(response);
            } else {
                showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
                textView_empty.setVisibility(View.VISIBLE);
                recycler_view.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ParseResponse(String response) {
        ViewPager vp = getActivity().findViewById(R.id.viewPager);
        try {
            customAdapterNotification = null;
            JSONObject responseObject = new JSONObject(response);
            notificationArrayList = new ArrayList<>();
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                JSONArray groupListArray = responseObject.getJSONArray("groupList");
                for (int a = 0; a < groupListArray.length(); a++) {
                    GroupModel groupModel = new GroupModel();
                    JSONObject groupObj = groupListArray.getJSONObject(a);
                    groupModel.setGroupId(groupObj.getString("groupId"));
                    groupModel.setGroupName(groupObj.getString("groupName").toLowerCase());
                    groupModel.setMemberContribution(groupObj.getString("userExpense"));
//                    groupModel.setGroupExpense(groupObj.getString("groupExpense"));
                    groupModel.setSettled(groupObj.getBoolean("settled"));
                    notificationArrayList.add(groupModel);
                }

                if (customAdapterNotification == null) {
                    customAdapterNotification = new CustomAdapterNotification(getActivity(),
                            notificationArrayList);
                    recycler_view.setAdapter(customAdapterNotification);
                    customAdapterNotification.notifyDataSetChanged();

                } else {
                    customAdapterNotification.notifyDataSetChanged();
                }
//                if (1 == vp.getCurrentItem()) {
//                    showSnackBar("Groups Updated");
//                }
            } else {
                textView_empty.setVisibility(View.VISIBLE);
                String errorMessage = responseObject.getString("errorMessage");
                if (1 == vp.getCurrentItem()) {
                    showSnackBar(errorMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_INDEFINITE);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }
}
