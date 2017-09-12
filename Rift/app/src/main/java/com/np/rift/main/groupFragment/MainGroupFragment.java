package com.np.rift.main.groupFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.np.rift.R;
import com.np.rift.serverRequest.ServerGetRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

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
        Log.d("subhechhu", "B onCreate" + fragmentValue);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_group, container, false);

        progress_default = fragmentView.findViewById(R.id.progress_default);
        Progress(false);

        recycler_view = fragmentView.findViewById(R.id.recycler_view);
        mLayoutManager = new android.support.v7.widget.LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(mLayoutManager);
        recycler_view.setItemAnimator(new DefaultItemAnimator());

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

        swipeRefreshLayout = fragmentView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

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

        GetGroups();
        return fragmentView;
    }

    private void GetGroups() {
        String url = "";
        ParseResponse();
//        new ServerGetRequest(this, "GET_GROUPS").execute(url);
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            ParseResponse();
        }
    }

    private void ParseResponse() {
        try {
            groupArrayList = new ArrayList<>();
            String response = getJSON();
            JSONArray responseArray = new JSONArray(response);
            for (int a = 0; a < responseArray.length(); a++) {
                GroupModel groupModel = new GroupModel();
                JSONObject groupObj = responseArray.getJSONObject(a);
                groupModel.setGroupId(groupObj.getString("groupId"));
                groupModel.setGroupName(groupObj.getString("groupName"));
                groupModel.setMemberContribution(groupObj.getString("userExpense"));
                groupModel.setGroupExpense(groupObj.getString("groupExpense"));
                groupModel.setSettled(groupObj.getBoolean("settled"));
                groupArrayList.add(groupModel);
            }

            if (customAdapterGroup == null) {
                customAdapterGroup = new CustomAdapterListGroup(getActivity(),
                        groupArrayList);
                recycler_view.setAdapter(customAdapterGroup);
                customAdapterGroup.notifyDataSetChanged();

            } else {
                customAdapterGroup.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        }
        return json;
    }

    private void Progress(boolean show) {
        if (show) {
            progress_default.show();
        } else {
            progress_default.hide();
        }
    }
}
