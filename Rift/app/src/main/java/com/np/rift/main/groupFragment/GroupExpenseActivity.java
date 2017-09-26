/*
 *    Copyright (C) 2016 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.np.rift.main.groupFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.menuOptions.EditFragment;
import com.np.rift.main.personalFragment.addExp.AddExpFragment;
import com.np.rift.main.personalFragment.addExp.ExpenseModel;
import com.np.rift.main.personalFragment.addExp.MonthModel;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.serverRequest.ServerPostRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GroupExpenseActivity extends AppCompatActivity implements
        ServerGetRequest.Response, ServerPostRequest.Response {

    RecyclerViewExpandableItemManager expMgr;
    RecyclerView recyclerView;
    MyAdapter adapter;
    ArrayList<MonthModel> monthParentArray;
    ArrayList<ExpenseModel> childArray;
    String userId;
    LinkedHashMap<String, List<ExpenseModel>> listDataChild;
    SharedPrefUtil sharedPrefUtil;

    View container;
    AVLoadingIndicatorView progress_default;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<String> listToDelete = new ArrayList<>();

    String groupName, groupId;
    View.OnClickListener mItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickItemView(v);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_personal_expense);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        sharedPrefUtil = new SharedPrefUtil();
        userId = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "userId", "000");

        container = findViewById(R.id.container);
        progress_default = (AVLoadingIndicatorView) findViewById(R.id.progress_default);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                GetExpenseDetails();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getIntent().getStringExtra("groupName"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Setup expandable feature and RecyclerView
        expMgr = new RecyclerViewExpandableItemManager(null);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ParseJson(getJSON());

//        GetExpenseDetails();
    }

    private String getJSON() {
        String json = null;
        try {
            InputStream is = AppController.getContext().getAssets().open("expense_persona;.json");
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

    private void GetExpenseDetails() {
        swipeRefreshLayout.setEnabled(false);
//        String url = AppController.getInstance().getString(R.string.domain)
//                + "/getExpense?userId=" + userId;
        Progress(true);
        String url = "http://www.mocky.io/v2/57cfde922600006f1c64ffec";
        new ServerGetRequest(this, "GET_EXPENSES").execute(url);
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        swipeRefreshLayout.setEnabled(true);
        if (response != null && !response.isEmpty()) {
            try {
                Progress(false);
                ParseJson(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Progress(false);
//            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
        }
    }

    @Override
    public void getPostResult(String response, String requestCode, int responseCode) {
        Log.e("TAG", "response:" + response);
        swipeRefreshLayout.setEnabled(true);
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                Progress(false);
                if ("success".equals(status)) {
                    GetExpenseDetails();
                    showSnackBar(AppController.getInstance().getString(R.string.fetching));
                } else {
                    String errorMessage = responseObject.getString("errorMessage");
                    showSnackBar(errorMessage);
                }
            } catch (Exception e) {
                Progress(false);
                e.printStackTrace();
            }
        } else {
            Progress(false);
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
        }
    }

//    public void ParseJson(String s) {
//        Log.e("Data.java", "data: " + s);
//        try {
//            monthParentArray = new ArrayList<>();
//            listDataChild = new LinkedHashMap<>();
//
//            monthParentArray.clear();
//            listDataChild.clear();
//
//            JSONArray mainArray = new JSONArray(s);
//            for (int i = 0; i < mainArray.length(); i++) {
//                MonthModel month = new MonthModel();
//                JSONObject subObject = mainArray.getJSONObject(i);
//                month.setName(subObject.getString("name"));
//                monthParentArray.add(month);
//                JSONArray subArray = subObject.getJSONArray("sub_services");
//                Log.e(getClass().getSimpleName(), i + "subArray. " + subArray);
//
//                childArray = new ArrayList<>();
//                for (int j = 0; j < subArray.length(); j++) {
//                    ExpenseModel childClass = new ExpenseModel();
//                    JSONObject innerSubObj =
//                            subArray.getJSONObject(j);
//                    childClass.set(innerSubObj.getString("name"));
//                    childClass.setDescription(innerSubObj.getString("duration"));
//                    childClass.setPrice(innerSubObj.getString("price"));
//                    childArray.add(childClass);
//                }
//                Log.d(getClass().getSimpleName(), "childCount: " + childArray.size());
//                listDataChild.put(monthParentArray.get(i).getName(), childArray);
//            }
//
//            if (adapter == null) {
//                adapter = new MyAdapter();
//                recyclerView.setAdapter(expMgr.createWrappedAdapter(adapter));
////            recyclerView.setAdapter(expMgr.createWrappedAdapter(new MyAdapter()));
//                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
//                expMgr.attachRecyclerView(recyclerView);
//            } else {
//                adapter.notifyDataSetChanged();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    private void ParseJson(String response) {
        try {
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                JSONArray expenseDetailsArray = responseObject.getJSONArray("expenseDetails");
//                ParseJson(expenseDetailsArray);
                monthParentArray = new ArrayList<>();
                listDataChild = new LinkedHashMap<>();

                monthParentArray.clear();
                listDataChild.clear();

//            JSONArray mainArray = new JSONArray(s);
                for (int i = 0; i < expenseDetailsArray.length(); i++) {
                    MonthModel month = new MonthModel();
                    JSONObject monthObject = expenseDetailsArray.getJSONObject(i);
                    month.setName(monthObject.getString("month"));
                    monthParentArray.add(month);
                    JSONArray expensesSubArray = monthObject.getJSONArray("expenses");
                    Log.e(getClass().getSimpleName(), i + "expensesSubArray. " + expensesSubArray);
                    childArray = new ArrayList<>();
                    for (int j = 0; j < expensesSubArray.length(); j++) {
                        ExpenseModel expenseModel = new ExpenseModel();
                        JSONObject innerSubObj =
                                expensesSubArray.getJSONObject(j);
                        expenseModel.setId(innerSubObj.getString("id"));
                        expenseModel.setDate(innerSubObj.getString("date"));
                        expenseModel.setSpentOn(innerSubObj.getString("spentOn"));
                        expenseModel.setAmount(innerSubObj.getString("amount"));
                        childArray.add(expenseModel);
                    }
                    Log.d(getClass().getSimpleName(), "childCount: " + childArray.size());
                    listDataChild.put(monthParentArray.get(i).getName(), childArray);
                }

                if (adapter == null) {
                    adapter = new MyAdapter();
                    recyclerView.setAdapter(expMgr.createWrappedAdapter(adapter));
//            recyclerView.setAdapter(expMgr.createWrappedAdapter(new MyAdapter()));
                    ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                    expMgr.attachRecyclerView(recyclerView);
                } else {
                    adapter.notifyDataSetChanged();
                }
            } else {
                String errorMessage = responseObject.getString("errorMessage");
                showSnackBar(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            Bundle bundle = new Bundle();
            bundle.putString("for", "settle");
            BottomSheetDialogFragment fragment = new AddExpFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
            return true;
        } else if (item.getItemId() == R.id.menu_edit) {
            Bundle bundle = new Bundle();
            bundle.putString("for", "group");
            bundle.putString("groupName", groupName);
            BottomSheetDialogFragment fragment = new EditFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        } else if (item.getItemId() == R.id.menu_exit) {
            createDialog("Exit");
        } else if (item.getItemId() == R.id.menu_settle) {
            createDialog("Settle");
        }
        return false;
    }

    public void createDialog(final String from) {
        String title, body;
        if ("Exit".equals(from)) {
            title = "Exit Group";
            body = "Are you sure you want to exit " + groupName + "?";
        } else {
            title = "Settle Expense";
            body = "Are you sure you want to settle expenses on " + groupName + "?";
        }

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(body)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if ("Exit".equals(from)) {

                        } else {

                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuItem menu_edit, menuItem_settle;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);

        menu_edit = menu.findItem(R.id.menu_edit);
//        menuItem_settle=menu.findItem(R.id.menu_settle);
//
        menu_edit.setVisible(false);
//        menuItem_settle.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    private void Progress(boolean show) {
        if (show) {
            progress_default.show();
        } else {
            progress_default.hide();
        }
    }

    public void AddItems(JSONArray items) {
        Progress(true);
        swipeRefreshLayout.setEnabled(false);
        showSnackBar(AppController.getInstance().getString(R.string.updating));
        PostItems(items);
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(container, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

    private void PostItems(JSONArray itemsArray) {
        try {
            JSONObject postObject = new JSONObject();
            postObject.put("type", "personal");
            postObject.put("userId", userId);
            postObject.put("data", itemsArray);
            String url = AppController.getInstance().getString(R.string.domain)
                    + "/addExpense";
            Log.e("TAG", "url: " + url);
            Log.e("TAG", "postObject: " + postObject.toString());
            new ServerPostRequest(this, "ADD_ITEM").execute(url, postObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    void onClickItemView(View v) {
        RecyclerView.ViewHolder vh = RecyclerViewAdapterUtils.getViewHolder(v);
        int flatPosition = vh.getAdapterPosition();

        if (flatPosition == RecyclerView.NO_POSITION) {
            return;
        }

        long expandablePosition = expMgr.getExpandablePosition(flatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);
//        Log.e("TAG","v.getID: "+v.getId());
        switch (v.getId()) {
//            case R.id.linear_:
//                Log.e("TAG", listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
//                break;
            default:
                throw new IllegalStateException("Unexpected click event");
        }
    }

    private void handleOnClickChildItemRemoveButton(int groupPosition, int childPosition) {
        Log.e("TAG", groupPosition + "\t" + childPosition);
    }


    private interface Expandable extends ExpandableItemConstants {
    }

    private static class MyGroupItem {
        final List<MyChildItem> children;
        public long id;
        public String text;

        MyGroupItem(long id, String text) {
            this.id = id;
            this.text = text;
            children = new ArrayList<>();
        }
    }

    private static class MyChildItem {
        public long id;
        String text, text2, text3;

        MyChildItem(long id, String text, String text2, String text3) {
            this.id = id;
            this.text = text;
            this.text2 = text2;
            this.text3 = text3;
        }
    }


    static class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        TextView textView;
        ImageView imageView_arrow;

        MyGroupViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.grpfirst);
            imageView_arrow = itemView.findViewById(R.id.imageView_arrow);
        }
    }

    static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        TextView textView, textView2, textView3;
        ImageView delete;

        MyChildViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView_date);
            textView2 = itemView.findViewById(R.id.textView_amount);
            textView3 = itemView.findViewById(R.id.textView_spentOn);
//            delete = (ImageView) itemView.findViewById(R.id.delete_btn);
            delete.setOnClickListener(clickListener);
        }
    }

    private class MyAdapter extends AbstractExpandableItemAdapter<MyGroupViewHolder, MyChildViewHolder> {
        List<MyGroupItem> mItems;

        MyAdapter() {
            setHasStableIds(true); // this is required for expandable feature.
            mItems = new ArrayList<>();
            for (int i = 0; i < monthParentArray.size(); i++) {
                MyGroupItem group = new MyGroupItem(i, monthParentArray.get(i).getName());
                List<ExpenseModel> list = listDataChild.get(monthParentArray.get(i).getName());
                for (int j = 0; j < list.size(); j++) {
                    group.children.add(new MyChildItem(j, list.get(j).getDate(),
                            list.get(j).getSpentOn(), list.get(j).getAmount()));
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
        public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_view_, parent, false);
            return new MyGroupViewHolder(v);
        }

        @Override
        public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_view_, parent, false);
            return new MyChildViewHolder(v, mItemOnClickListener);
        }

        @Override
        public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
            MyGroupItem group = mItems.get(groupPosition);
            holder.textView.setText(group.text);

            holder.itemView.setClickable(true);

            // set background resource (target view ID: container)
            final int expandState = holder.getExpandStateFlags();

            if ((expandState & Expandable.STATE_FLAG_IS_UPDATED) != 0) {
                int textColor;
                Drawable img_arrow;

                if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                    textColor = ContextCompat.getColor(GroupExpenseActivity.this, R.color.colorAccent);
                    img_arrow = ContextCompat.getDrawable(GroupExpenseActivity.this, R.drawable.arrow_up);
                } else {
                    textColor = ContextCompat.getColor(GroupExpenseActivity.this, R.color.colorPrimaryDark);
                    img_arrow = ContextCompat.getDrawable(GroupExpenseActivity.this, R.drawable.arrow_down);
                }
                holder.textView.setTextColor(textColor);
                holder.imageView_arrow.setBackground(img_arrow);
            }
        }

        @Override
        public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
            MyChildItem child = mItems.get(groupPosition).children.get(childPosition);
            holder.textView.setText(child.text);
            holder.textView2.setText(child.text2);
            holder.textView3.setText(child.text3);
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
            return true;
        }
    }
}
