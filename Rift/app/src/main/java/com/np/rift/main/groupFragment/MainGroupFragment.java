package com.np.rift.main.groupFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.BottomSheetDialogFragment;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class MainGroupFragment extends Fragment implements ServerGetRequest.Response {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    ArrayList<GroupModel> groupArrayList;
    RecyclerView recycler_view;
    CustomAdapterListGroup customAdapterGroup;
    SwipeRefreshLayout swipeRefreshLayout;
    AVLoadingIndicatorView progress_default;
    FabSpeedDial fabSpeedDial;
    LinearLayoutManager mLayoutManager;

    TextView textView_empty;
    LinearLayout linearlayout_main;

    SharedPrefUtil sharedPrefUtil;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentValue = getArguments() != null ? getArguments().getInt("val") : 1;
        sharedPrefUtil = new SharedPrefUtil();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_group, container, false);

        progress_default = fragmentView.findViewById(R.id.progress_default);
        linearlayout_main = fragmentView.findViewById(R.id.linearlayout_main);

        recycler_view = fragmentView.findViewById(R.id.recycler_view);
        mLayoutManager = new android.support.v7.widget.LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(mLayoutManager);
        recycler_view.setItemAnimator(new DefaultItemAnimator());

        textView_empty = fragmentView.findViewById(R.id.textView_empty);

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fabSpeedDial.getVisibility() == View.VISIBLE) {
                    fabSpeedDial.hide();
                } else if (dy < 0 && fabSpeedDial.getVisibility() != View.INVISIBLE) {
                    fabSpeedDial.show();
                }
            }
        });

        textView_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGroups();
            }
        });

        swipeRefreshLayout = fragmentView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
//                showSnackBar("Updating Groups");
                getGroups();
            }
        });

        getGroups();

        fabSpeedDial = fragmentView.findViewById(R.id.fab_shortcut);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                Bundle bundle = new Bundle();
                BottomSheetDialogFragment fragment = new JoinGroupFragment();
                switch (menuItem.getTitle().toString()) {
                    case "Join Group":
                        bundle.putString("purpose", "Join");
                        fragment.setArguments(bundle);
                        fragment.show(getFragmentManager(), fragment.getTag());
                        break;
                    case "Add Group":
                        bundle.putString("purpose", "Add");
                        fragment.setArguments(bundle);
                        fragment.show(getFragmentManager(), fragment.getTag());
                        break;
                }
                return false;
            }
        });
        return fragmentView;
    }

    public void getGroups() {
        progress(true);
        String url = AppController.getInstance().getString(R.string.domain)
                + "/GetGrouplist?userId=" + AppController.getUserId();
        new ServerGetRequest(this, "GET_GROUPS").execute(url);
        Log.e("TAG", "url: " + url);
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
        Log.e("TAG", "175 response: " + response);
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
            customAdapterGroup = null;
            JSONObject responseObject = new JSONObject(response);
            groupArrayList = new ArrayList<>();
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                if (recycler_view.getVisibility() == View.INVISIBLE) {
                    recycler_view.setVisibility(View.VISIBLE);
                }
                JSONArray groupListArray = responseObject.getJSONArray("groupList");
                for (int a = 0; a < groupListArray.length(); a++) {
                    GroupModel groupModel = new GroupModel();
                    JSONObject groupObj = groupListArray.getJSONObject(a);
                    groupModel.setGroupId(groupObj.getString("groupId"));
                    groupModel.setGroupName(groupObj.getString("groupName"));
                    groupModel.setMemberContribution(groupObj.getString("userExpense"));
//                    groupModel.setGroupExpense(groupObj.getString("groupExpense"));
                    groupModel.setSettled(groupObj.getBoolean("settled"));
                    groupArrayList.add(groupModel);
                }
                Collections.sort(groupArrayList, new Comparator<GroupModel>() {
                    @Override
                    public int compare(GroupModel groupModel, GroupModel t1) {
                        return groupModel.getGroupName().compareTo(t1.getGroupName());
                    }
                });

                if (customAdapterGroup == null) {
                    customAdapterGroup = new CustomAdapterListGroup(getActivity(),
                            groupArrayList);
                    recycler_view.setAdapter(customAdapterGroup);
                    customAdapterGroup.notifyDataSetChanged();

                } else {
                    customAdapterGroup.notifyDataSetChanged();
                }

//                if (1 == vp.getCurrentItem()) {
//                    showSnackBar("Groups Updated");
//                }
            } else {
                Log.e("TAG", "vp.getCurrentItem(): " + vp.getCurrentItem());
                textView_empty.setVisibility(View.VISIBLE);
                recycler_view.setVisibility(View.INVISIBLE);
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

    private String getJSON() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("group_list.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("TAG", "sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), \"refreshGroup\", false): "
                + sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "refreshGroup", false));
        //Called when group is exited.
        if (sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "refreshGroup", false)) {
            sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(), "refreshGroup", false);
            getGroups();
        }
//        if (HomeActivity.isDeleted) {
//            HomeActivity.isDeleted = false;
//            getGroups();
//        }
    }
}
