package com.np.rift.main.activityFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.personalFragment.addExp.MonthModel;
import com.np.rift.serverRequest.ServerGetRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class ActivityFragment extends Fragment implements ServerGetRequest.Response {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    CustomAdapterActivity customAdapterActivity;

    ArrayList<MonthModel> monthParentArray;
    LinkedHashMap<String, List<ActivityModel>> listDataChild;

    ArrayList<ActivityModel> activityArrayList;
    ArrayList<String> dateArrayList;
    RecyclerView recycler_view;
    SwipeRefreshLayout swipeRefreshLayout;
    AVLoadingIndicatorView progress_default;
    LinearLayoutManager mLayoutManager;

    TextView textView_empty;
    LinearLayout linearlayout_main;

    RecyclerViewExpandableItemManager expMgr;
    MyAdapter adapter;

    public static ActivityFragment init(int position) {
        ActivityFragment frndsInviteFragment = new ActivityFragment();
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
                getActivitites();
            }
        });

        swipeRefreshLayout = fragmentView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                getActivitites();
            }
        });
        ParseResponse(getJson());
//        getActivitites();

        return fragmentView;
    }

    public String getJson() {
        String json = null;
        try {
            InputStream is = AppController.getContext().getAssets().open("activity.json");
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

    public void getActivitites() {
        progress(true);
        String url = AppController.getInstance().getString(R.string.domain)
                + "/getNotification?userId=" + AppController.getUserId();
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
//        ViewPager vp = getActivity().findViewById(R.id.viewPager);
        try {
            String date;
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                if (recycler_view.getVisibility() == View.INVISIBLE) {
                    recycler_view.setVisibility(View.VISIBLE);
                }
                JSONArray activityDetailArray = responseObject.getJSONArray("activities");
                monthParentArray = new ArrayList<>();
                listDataChild = new LinkedHashMap<>();

                monthParentArray.clear();
                listDataChild.clear();

                if (activityDetailArray.length() == 0) {
                    showSnackBar("Activity not found!!");
                }

                for (int i = 0; i < activityDetailArray.length(); i++) {
                    MonthModel month = new MonthModel();
                    JSONObject dateObject = activityDetailArray.getJSONObject(i);
                    month.setName(dateObject.getString("date"));
                    JSONArray expensesSubArray = dateObject.getJSONArray("actions");
                    ArrayList<ActivityModel> mainArray = new ArrayList<>();
                    float monthTotal = 0;
                    for (int j = 0; j < expensesSubArray.length(); j++) {
                        ActivityModel expenseModel = new ActivityModel();
                        JSONObject innerSubObj = expensesSubArray.getJSONObject(j);
                        expenseModel.setUserId(innerSubObj.getString("userId"));
                        expenseModel.setGroupId(innerSubObj.getString("groupId"));
                        expenseModel.setAction(innerSubObj.getString("action"));
                        mainArray.add(expenseModel);
                    }

                    month.setExpense(String.valueOf(monthTotal));
                    monthParentArray.add(month);

                    listDataChild.put(monthParentArray.get(i).getName(), mainArray);
                }

                expMgr = new RecyclerViewExpandableItemManager(null);
                adapter = new ActivityFragment.MyAdapter();
                recycler_view.setAdapter(expMgr.createWrappedAdapter(adapter));
                ((SimpleItemAnimator) recycler_view.getItemAnimator()).setSupportsChangeAnimations(false);
                expMgr.attachRecyclerView(recycler_view);
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

    //======================================================================================================================================
//======================================================================================================================================
//======================================================================================================================================
//======================================================================================================================================
//=====================================                                                           ======================================
//=====================================                    ADAPTER CLASS BELOW                    ======================================
//=====================================                                                           ======================================
//======================================================================================================================================
//======================================================================================================================================
//======================================================================================================================================
//======================================================================================================================================
//======================================================================================================================================

//    void onClickItemView(View v) {
//        RecyclerView.ViewHolder vh = RecyclerViewAdapterUtils.getViewHolder(v);
//        LinearLayout linearlayout_child = v.findViewById(R.id.linearlayout_child);
//        int flatPosition = 0;
//        if (vh != null) {
//            flatPosition = vh.getAdapterPosition();
//        }
//
//        if (flatPosition == RecyclerView.NO_POSITION) {
//            return;
//        }
//
//        long expandablePosition = expMgr.getExpandablePosition(flatPosition);
//        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
//        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);
//
//        switch (v.getId()) {
//            case R.id.linearlayout_child:
//
//                if (listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getSelected()) {
//                    deleteList.remove(listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
//                    listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).setSelected(false);
//                    linearlayout_child.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
//                    selected--;
//                    if (selected == 0) {
//                        linear_buttons.setVisibility(View.GONE);
////                        _snackbar.dismiss();
//                    }
////                    else if (selected == 1) {
////
////                        _snackbar.setText(selected + " item");
////                    }
//                    else {
//                        bounceAnimation();
////                        _snackbar.setText(selected + " items");
//                    }
//                } else {
//                    deleteList.add(listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
//                    listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).setSelected(true);
//                    linearlayout_child.setBackgroundColor(ContextCompat.getColor(PersonalExpenseActivity.this, R.color.red_shade));
//                    if (selected >= 0) {
//                        selected++;
////                        showSnackBar(selected + " item", "Delete");
//                        bounceAnimation();
//                    }
////                    else if (selected > 0) {
////                        selected++;
////                        _snackbar.setText(selected + " items");
////                    }
//                }
////                Log.e("TAG", listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
////                Log.e("TAG", "delete list: " + deleteList.toString());
//                break;
//            default:
//                throw new IllegalStateException("Unexpected click event");
//        }
//    }

    private interface Expandable extends ExpandableItemConstants {
    }

    private static class MyGroupItem {
        final List<ActivityFragment.MyChildItem> children;
        public long id;
        String month, expense;

        MyGroupItem(long id, String month, String expense) {
            this.id = id;
            this.month = month;
            this.expense = expense;

            children = new ArrayList<>();
        }
    }

    private static class MyChildItem {
        public long id;
        String action;

        MyChildItem(long id, String action) {
            this.id = id;
            this.action = action;
        }
    }

    static class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        TextView textView_monthName, textView_monthAmt;
        ImageView imageView_arrow;


        MyGroupViewHolder(View itemView) {
            super(itemView);
            textView_monthName = itemView.findViewById(R.id.grpfirst);
            textView_monthAmt = itemView.findViewById(R.id.grpAmount);
            imageView_arrow = itemView.findViewById(R.id.imageView_arrow);
            textView_monthAmt.setVisibility(View.GONE);
        }
    }

    static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        TextView textView_group;
        LinearLayout linearlayout_child;

        MyChildViewHolder(View itemView) {
            super(itemView);
            textView_group = itemView.findViewById(R.id.textView_group);
            linearlayout_child = itemView.findViewById(R.id.linearlayout_child);
        }
    }

    private class MyAdapter extends AbstractExpandableItemAdapter<ActivityFragment.MyGroupViewHolder, ActivityFragment.MyChildViewHolder> {
        List<ActivityFragment.MyGroupItem> mItems;

        MyAdapter() {
            setHasStableIds(true); // this is required for expandable feature.
            mItems = new ArrayList<>();
            for (int i = 0; i < monthParentArray.size(); i++) {
                ActivityFragment.MyGroupItem group = new ActivityFragment.MyGroupItem(i, monthParentArray.get(i).getName(),
                        monthParentArray.get(i).getExpense());
                List<ActivityModel> list = listDataChild.get(monthParentArray.get(i).getName());
                for (int j = 0; j < list.size(); j++) {
                    group.children.add(new ActivityFragment.MyChildItem(j, list.get(j).getAction()));
                }
                mItems.add(group);
            }
        }

        @Override
        public int getGroupCount() {
            return mItems.size();
        }

        @Override
        public int getChildCount(int groupPosition) {
            return mItems.get(groupPosition).children.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return mItems.get(groupPosition).id;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return mItems.get(groupPosition).children.get(childPosition).id;
        }

        @Override
        public ActivityFragment.MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_month, parent, false);
            return new ActivityFragment.MyGroupViewHolder(v);
        }

        @Override
        public ActivityFragment.MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_notification, parent, false);
            return new ActivityFragment.MyChildViewHolder(v);
        }

        @Override
        public void onBindGroupViewHolder(ActivityFragment.MyGroupViewHolder holder, int groupPosition, int viewType) {
            ActivityFragment.MyGroupItem group = mItems.get(groupPosition);
            holder.textView_monthName.setText(group.month);
            holder.itemView.setClickable(true);

            final int expandState = holder.getExpandStateFlags();
            if ((expandState & ActivityFragment.Expandable.STATE_FLAG_IS_UPDATED) != 0) {
                int textColor;
                Drawable img_arrow;

                if ((expandState & ActivityFragment.Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                    textColor = ContextCompat.getColor(getActivity(), R.color.colorAccent);
                    img_arrow = ContextCompat.getDrawable(getActivity(), R.drawable.arrow_more);
                } else {
                    textColor = ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark);
                    img_arrow = ContextCompat.getDrawable(getActivity(), R.drawable.arrow_less);
                }
                holder.textView_monthName.setTextColor(textColor);
                holder.imageView_arrow.setBackground(img_arrow);
            }
        }

        @Override
        public void onBindChildViewHolder(ActivityFragment.MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
            ActivityFragment.MyChildItem child = mItems.get(groupPosition).children.get(childPosition);
            holder.textView_group.setText(child.action);
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(ActivityFragment.MyGroupViewHolder holder,
                                                       int groupPosition, int x, int y, boolean expand) {
            return true;
        }
    }
}
