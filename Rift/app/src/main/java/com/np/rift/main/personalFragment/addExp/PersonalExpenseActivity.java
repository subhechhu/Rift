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

package com.np.rift.main.personalFragment.addExp;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.connection.ConnectionLost;
import com.np.rift.connection.NetworkCheck;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.serverRequest.ServerPostRequest;
import com.np.rift.util.MyBounceInterpolator;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class PersonalExpenseActivity extends AppCompatActivity implements
        ServerGetRequest.Response, ServerPostRequest.Response {

    private final SimpleDateFormat format_toGet = new SimpleDateFormat("yyyy-MM-dd");

    RecyclerViewExpandableItemManager expMgr;
    RecyclerView recyclerView;
    MyAdapter adapter;
    ArrayList<MonthModel> monthParentArray;
    ArrayList<ExpenseModel> expenseArray;
    String userId, userName;
    LinkedHashMap<String, List<ExpenseModel>> listDataChild;
    SharedPrefUtil sharedPrefUtil;
    TextView textView_empty;

    View container;
    AVLoadingIndicatorView progress_default;
    int selected = 0;

    ArrayList<String> deleteList = new ArrayList<>();
    Snackbar _snackbar;
    LinearLayout linear_buttons;

    Animation myAnim;
    MyBounceInterpolator interpolator;
    FloatingActionButton fab_delete, fab_edit;


    View.OnClickListener mItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickItemView(v);
        }
    };

    public static String formatDate(String strdate) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("mm-dd-yyyy");
            Date date = dt.parse(strdate);
            SimpleDateFormat dt1 = new SimpleDateFormat("mm/dd");
            strdate = (dt1.format(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strdate;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_personal_expense);

        sharedPrefUtil = new SharedPrefUtil();
        userId = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "userId", "000");
        userName = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "userName", "000");

        container = findViewById(R.id.container);
        progress_default = (AVLoadingIndicatorView) findViewById(R.id.progress_default);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Expense");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        linear_buttons = (LinearLayout) findViewById(R.id.linear_buttons);

        fab_delete = (FloatingActionButton) findViewById(R.id.fab_delete);
        fab_edit = (FloatingActionButton) findViewById(R.id.fab_edit);

//        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setEnabled(false);

        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        interpolator = new MyBounceInterpolator(0.2, 20);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        textView_empty = (TextView) findViewById(R.id.textView_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        ParseJson(getJSON());
        GetExpenseDetails();


        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected > 1) {
                    Toast.makeText(PersonalExpenseActivity.this, "Cannot edit arrow_more than 1 expense", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PersonalExpenseActivity.this, "Ask Vasanta about it ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    if (selected > 1) {
                        Toast.makeText(PersonalExpenseActivity.this, "Sandeep is stupid for not " +
                                "making this feature", Toast.LENGTH_SHORT).show();
                    } else {
                        selected = 0;
                        deleteRequest();
                    }
                } else {
                    noInternet();
                }
            }
        });

        textView_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetExpenseDetails();
            }
        });
    }

    public void bounceAnimation() {
        if (linear_buttons.getVisibility() == View.GONE) {
            linear_buttons.setVisibility(View.VISIBLE);
            myAnim.setInterpolator(interpolator);
            linear_buttons.startAnimation(myAnim);
        }
    }

    public void noInternet() {
        ConnectionLost connectionLostFragment = new ConnectionLost();
        connectionLostFragment.show(getFragmentManager(), "dialogFragment");
    }


    private String getJSON() {
        String json = null;
        try {
            InputStream is = AppController.getContext().getAssets().open("expense_personal.json");
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
        if (NetworkCheck.isInternetAvailable()) {
            String url = AppController.getInstance().getString(R.string.domain)
                    + "/getPersonalExpense?userId=" + userId;
            progress(true);
            new ServerGetRequest(this, "GET_EXPENSES").execute(url);
        } else {
            showSnackBar("No Internet!!", "ReTry");
            noInternet();
        }
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            if ("GET_EXPENSES".equals(requestCode)) {
                try {
                    progress(false);
                    textView_empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    ParseJson(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("DELETE_REQUEST".equals(requestCode)) {
                try {
                    progress(false);
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    if ("Success".equalsIgnoreCase(status)) {
                        GetExpenseDetails();
                        showSnackBar("Expenses has been deleted", "OK");
                        linear_buttons.setVisibility(View.GONE);
                        GetExpenseDetails();
                    } else {
                        String errorMessage = responseObject.getString("errorMessage");
                        showSnackBar(errorMessage, "OK");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            progress(false);
            textView_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong), "OK");
        }
    }

    private void progress(boolean b) {
        if (b) {
            progress_default.setVisibility(View.VISIBLE);
        } else {
            progress_default.setVisibility(View.INVISIBLE);
        }
    }

    private void ParseJson(String response) {
        try {
//            String date;
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                if (recyclerView.getVisibility() == View.INVISIBLE) {
                    recyclerView.setVisibility(View.VISIBLE);
                    expMgr.expandGroup(0);
                }
                JSONArray expenseDetailsArray = responseObject.getJSONArray("expenseDetails");
                monthParentArray = new ArrayList<>();
                listDataChild = new LinkedHashMap<>();

                monthParentArray.clear();
                listDataChild.clear();

                if (expenseDetailsArray.length() == 0) {
                    showSnackBar("Add Expenses!!", "Add");
                }

//            JSONArray mainArray = new JSONArray(s);
                for (int i = 0; i < expenseDetailsArray.length(); i++) {
                    MonthModel month = new MonthModel();
                    JSONObject monthObject = expenseDetailsArray.getJSONObject(i);
                    month.setName(monthObject.getString("month"));
                    JSONArray expensesSubArray = monthObject.getJSONArray("expenses");
                    expenseArray = new ArrayList<>();
                    float monthTotal = 0;
                    for (int j = 0; j < expensesSubArray.length(); j++) {
                        ExpenseModel expenseModel = new ExpenseModel();
                        JSONObject innerSubObj = expensesSubArray.getJSONObject(j);
                        expenseModel.setRealDate(format_toGet.parse(innerSubObj.getString("date")));
                        expenseModel.setId(innerSubObj.getString("expId"));
                        expenseModel.setDate(innerSubObj.getString("date"));
                        expenseModel.setSpentOn(innerSubObj.getString("spentOn"));
                        expenseModel.setAmount(innerSubObj.getString("amount"));
                        expenseModel.setType(innerSubObj.getString("type"));
                        expenseModel.setSelected(false);
                        monthTotal += Float.parseFloat(innerSubObj.getString("amount"));
                        expenseArray.add(expenseModel);
                    }
                    Collections.sort(expenseArray, new Comparator<ExpenseModel>() {
                        public int compare(ExpenseModel o1, ExpenseModel o2) {
                            return o2.getRealDate().compareTo(o1.getRealDate());
                        }
                    });

                    month.setExpense(String.valueOf(monthTotal));
                    monthParentArray.add(month);

                    listDataChild.put(monthParentArray.get(i).getName(), expenseArray);
                }

                expMgr = new RecyclerViewExpandableItemManager(null);
                adapter = new MyAdapter();
                recyclerView.setAdapter(expMgr.createWrappedAdapter(adapter));
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                expMgr.attachRecyclerView(recyclerView);
            } else {
                recyclerView.setVisibility(View.INVISIBLE);
                String errorMessage = responseObject.getString("errorMessage");
                showSnackBar(errorMessage, "OK");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void getPostResult(String response, String requestCode, int responseCode) {
        Log.e("TAG", "response:" + response);
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                progress(false);
                if ("success".equals(status)) {
                    sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(), "refreshGraph", true);
                    GetExpenseDetails();
                } else {
                    String errorMessage = responseObject.getString("errorMessage");
                    showSnackBar(errorMessage, "OK");
                }
            } catch (Exception e) {
                progress(false);
                e.printStackTrace();
            }
        } else {
            progress(false);
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong), "OK");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            Bundle bundle = new Bundle();
            bundle.putString("for", "individual");
            BottomSheetDialogFragment fragment = new AddExpFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuItem menuItem_exit, menuItem_settle, menuItem_history, menuItem_edit, menu_share;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);

        menuItem_exit = menu.findItem(R.id.menu_exit);
        menuItem_settle = menu.findItem(R.id.menu_settle);
        menuItem_history = menu.findItem(R.id.menu_history);
        menuItem_edit = menu.findItem(R.id.menu_edit);
        menu_share = menu.findItem(R.id.menu_share);

        menuItem_exit.setVisible(false);
        menuItem_edit.setVisible(false);
        menuItem_settle.setVisible(false);
        menuItem_history.setVisible(false);
        menu_share.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

//    private void progress(boolean show) {
//        if (show) {
//            progress_default.show();
//        } else {
//            progress_default.hide();
//        }
//    }

    public void AddItems(JSONArray items) {
        progress(true);
        showSnackBar("Updating You Expense", "OK");
        PostItems(items);
    }

    private void showSnackBar(String message, final String action) {
        _snackbar = Snackbar.make(container, message, Snackbar.LENGTH_INDEFINITE);
        _snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("Delete".equalsIgnoreCase(action)) {
                    if (NetworkCheck.isInternetAvailable()) {
                        selected = 0;
                        deleteRequest();
                    } else {
                        noInternet();
                    }
                } else if ("Add".equalsIgnoreCase(action)) {
                    if (NetworkCheck.isInternetAvailable()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("for", "individual");
                        BottomSheetDialogFragment fragment = new AddExpFragment();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
                    } else {
                        noInternet();
                    }
                } else if ("ReTry".equalsIgnoreCase(action)) {
                    GetExpenseDetails();
                }
                _snackbar.dismiss();
            }
        }).show();
    }

    public void deleteRequest() {
        try {
            String idList = deleteList.toString();
            idList = idList.replace("[", "");
            idList = idList.replace("]", "");
            idList = idList.replaceAll("\\s+", "");
            String url = AppController.getInstance().getString(R.string.domain)
                    + "/deletePersonalInfo?userId=" + AppController.getUserId()
                    + "&expId=" + idList;
            Log.e("TAG", "url: " + url);

            deleteList.clear();
            new ServerGetRequest(this, "DELETE_REQUEST").execute(url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void PostItems(JSONArray itemsArray) {
        try {
            JSONObject postObject = new JSONObject();
            postObject.put("type", "personal");
            postObject.put("userId", userId);
            postObject.put("userName", userName);
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
        LinearLayout linearlayout_child = v.findViewById(R.id.linearlayout_child);
        int flatPosition = 0;
        if (vh != null) {
            flatPosition = vh.getAdapterPosition();
        }

        if (flatPosition == RecyclerView.NO_POSITION) {
            return;
        }

        long expandablePosition = expMgr.getExpandablePosition(flatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);

        switch (v.getId()) {
            case R.id.linearlayout_child:

                if (listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getSelected()) {
                    deleteList.remove(listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
                    listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).setSelected(false);
                    linearlayout_child.setBackgroundColor(ContextCompat.getColor(PersonalExpenseActivity.this, R.color.white));
                    selected--;
                    if (selected == 0) {
                        linear_buttons.setVisibility(View.GONE);
//                        _snackbar.dismiss();
                    }
//                    else if (selected == 1) {
//
//                        _snackbar.setText(selected + " item");
//                    }
                    else {
                        bounceAnimation();
//                        _snackbar.setText(selected + " items");
                    }
                } else {
                    deleteList.add(listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
                    listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).setSelected(true);
                    linearlayout_child.setBackgroundColor(ContextCompat.getColor(PersonalExpenseActivity.this, R.color.red_shade));
                    if (selected >= 0) {
                        selected++;
//                        showSnackBar(selected + " item", "Delete");
                        bounceAnimation();
                    }
//                    else if (selected > 0) {
//                        selected++;
//                        _snackbar.setText(selected + " items");
//                    }
                }
//                Log.e("TAG", listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getId());
//                Log.e("TAG", "delete list: " + deleteList.toString());
                break;
            default:
                throw new IllegalStateException("Unexpected click event");
        }
    }

    private interface Expandable extends ExpandableItemConstants {
    }

    private static class MyGroupItem {
        final List<MyChildItem> children;
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
        String date, spentOn, amount, type;

        MyChildItem(long id, String date, String spentOn, String amount, String type) {
            this.id = id;
            this.date = formatDate(date);
            this.spentOn = spentOn;
            this.amount = amount;
            this.type = type;
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
        }
    }

    static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        TextView textView_date, textView_amount, textView_spentOn;
        LinearLayout linearlayout_child;
//        ImageView imageView_type;

        MyChildViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            textView_date = itemView.findViewById(R.id.textView_date);
            textView_amount = itemView.findViewById(R.id.textView_amount);
            textView_spentOn = itemView.findViewById(R.id.textView_spentOn);
            linearlayout_child = itemView.findViewById(R.id.linearlayout_child);
//            imageView_type = itemView.findViewById(R.id.imageView_type);
            linearlayout_child.setOnClickListener(clickListener);
//            imageView_type.setOnClickListener(clickListener);
        }
    }

    private class MyAdapter extends AbstractExpandableItemAdapter<MyGroupViewHolder, MyChildViewHolder> {
        List<MyGroupItem> mItems;

        MyAdapter() {
            setHasStableIds(true); // this is required for expandable feature.
            mItems = new ArrayList<>();
            for (int i = 0; i < monthParentArray.size(); i++) {
                MyGroupItem group = new MyGroupItem(i, monthParentArray.get(i).getName(),
                        monthParentArray.get(i).getExpense());
                List<ExpenseModel> list = listDataChild.get(monthParentArray.get(i).getName());
                for (int j = 0; j < list.size(); j++) {
                    group.children.add(new MyChildItem(j, list.get(j).getDate(),
                            list.get(j).getSpentOn(), list.get(j).getAmount(),
                            list.get(j).getType()));
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_month, parent, false);
            return new MyGroupViewHolder(v);
        }

        @Override
        public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_expense, parent, false);
            return new MyChildViewHolder(v, mItemOnClickListener);
        }

        @Override
        public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
            MyGroupItem group = mItems.get(groupPosition);
            holder.textView_monthName.setText(group.month);
            holder.textView_monthAmt.setText(group.expense);

            holder.itemView.setClickable(true);

            final int expandState = holder.getExpandStateFlags();
            if ((expandState & Expandable.STATE_FLAG_IS_UPDATED) != 0) {
                int textColor;
                Drawable img_arrow;

                if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                    textColor = ContextCompat.getColor(PersonalExpenseActivity.this, R.color.colorAccent);
                    img_arrow = ContextCompat.getDrawable(PersonalExpenseActivity.this, R.drawable.arrow_more);
                } else {
                    textColor = ContextCompat.getColor(PersonalExpenseActivity.this, R.color.colorPrimaryDark);
                    img_arrow = ContextCompat.getDrawable(PersonalExpenseActivity.this, R.drawable.arrow_less);
                }
                holder.textView_monthName.setTextColor(textColor);
                holder.textView_monthAmt.setTextColor(textColor);
                holder.imageView_arrow.setBackground(img_arrow);
            }
        }

        @Override
        public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
            MyChildItem child = mItems.get(groupPosition).children.get(childPosition);
            holder.textView_date.setText(child.date);
            holder.textView_amount.setText(child.amount);
            holder.textView_spentOn.setText(child.spentOn);

            if (listDataChild.get(monthParentArray.get(groupPosition).getName()).get(childPosition).getSelected()) {
                holder.linearlayout_child.setBackgroundColor(ContextCompat.getColor(PersonalExpenseActivity.this,
                        R.color.red_shade));
            } else {
                holder.linearlayout_child.setBackgroundColor(ContextCompat.getColor(PersonalExpenseActivity.this,
                        R.color.white));
            }

        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
            return true;
        }
    }
}
