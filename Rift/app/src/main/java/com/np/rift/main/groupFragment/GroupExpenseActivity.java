package com.np.rift.main.groupFragment;

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
import com.np.rift.main.personalFragment.addExp.AddExpFragment;
import com.np.rift.main.personalFragment.addExp.ExpenseModel;
import com.np.rift.main.personalFragment.addExp.MonthModel;
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

public class GroupExpenseActivity extends AppCompatActivity implements
        ServerGetRequest.Response, ServerPostRequest.Response {


    private final SimpleDateFormat format_toGet = new SimpleDateFormat("yyyy-MM-dd");

    RecyclerViewExpandableItemManager expMgr;
    RecyclerView recyclerView;
    GroupExpenseActivity.MyAdapter adapter;
    ArrayList<MonthModel> userParentArray;
    ArrayList<ExpenseModel> expenseArray;
    String groupId, groupName;
    LinkedHashMap<String, List<ExpenseModel>> listDataChild;
    SharedPrefUtil sharedPrefUtil;
    TextView textView_empty;

    View container;
    AVLoadingIndicatorView progress_default;
    int selected = 0;
    LinearLayout linear_buttons;

    ArrayList<String> deleteList = new ArrayList<>();

    Snackbar _snackbar;
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
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        fab_delete = (FloatingActionButton) findViewById(R.id.fab_delete);
        fab_edit = (FloatingActionButton) findViewById(R.id.fab_edit);

        container = findViewById(R.id.container);
        progress_default = (AVLoadingIndicatorView) findViewById(R.id.progress_default);
        linear_buttons = (LinearLayout) findViewById(R.id.linear_buttons);

        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        interpolator = new MyBounceInterpolator(0.2, 20);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Expense");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setEnabled(false);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        textView_empty = (TextView) findViewById(R.id.textView_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        ParseJson(getJSON());
        GetExpenseDetails();

        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected > 1) {
                    Toast.makeText(GroupExpenseActivity.this, "Cannot edit arrow_more than 1 expense", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupExpenseActivity.this, "Edit Coming soon", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    if (selected > 1) {
                        Toast.makeText(GroupExpenseActivity.this, "Delete Coming soon", Toast.LENGTH_SHORT).show();
                    } else {
//                        selected = 0;
                        Toast.makeText(GroupExpenseActivity.this, "Delete Coming soon", Toast.LENGTH_SHORT).show();
//                        deleteRequest();
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
                    + "/getGroupExpense?groupId=" + groupId;
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
            Log.e("TAG", "resppnse: " + response);
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
            String date;
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                JSONArray expenseDetailsArray = responseObject.getJSONArray("expenseDetails");
                userParentArray = new ArrayList<>();
                listDataChild = new LinkedHashMap<>();

                userParentArray.clear();
                listDataChild.clear();

                if (expenseDetailsArray.length() == 0) {
                    showSnackBar("Add Expenses!!", "Add");
                }

//            JSONArray mainArray = new JSONArray(s);
                for (int i = 0; i < expenseDetailsArray.length(); i++) {
                    MonthModel month = new MonthModel();
                    JSONObject monthObject = expenseDetailsArray.getJSONObject(i);

                    String mMonth = monthObject.getString("userName");
//                    mMonth = mMonth.substring(0, 3);
                    month.setName(monthObject.getString("userName"));
//                    month.setDate((Date) monthObject.get("month"));

                    JSONArray expensesSubArray = monthObject.getJSONArray("expenses");
                    expenseArray = new ArrayList<>();
                    float monthTotal = 0;
                    for (int j = 0; j < expensesSubArray.length(); j++) {
                        ExpenseModel expenseModel = new ExpenseModel();
                        JSONObject innerSubObj =
                                expensesSubArray.getJSONObject(j);
                        expenseModel.setRealDate(format_toGet.parse(innerSubObj.getString("date")));
                        expenseModel.setId(innerSubObj.getString("expId"));
                        expenseModel.setDate(innerSubObj.getString("date"));
                        expenseModel.setSpentOn(innerSubObj.getString("spentOn"));
                        expenseModel.setAmount(innerSubObj.getString("amount"));
                        expenseModel.setUserId(innerSubObj.getString("userId"));

                        expenseModel.setType("group");

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
                    userParentArray.add(month);
                    listDataChild.put(userParentArray.get(i).getName(), expenseArray);
                }

                expMgr = new RecyclerViewExpandableItemManager(null);
                adapter = new GroupExpenseActivity.MyAdapter();
                recyclerView.setAdapter(expMgr.createWrappedAdapter(adapter));
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                expMgr.attachRecyclerView(recyclerView);
            } else {
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
                    sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "refreshGroup", true);
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
            bundle.putString("for", "group");
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
        MenuItem menuItem_exit, menuItem_settle, menuItem_history, menuItem_edit;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);

        menuItem_exit = menu.findItem(R.id.menu_exit);
        menuItem_settle = menu.findItem(R.id.menu_settle);
        menuItem_history = menu.findItem(R.id.menu_history);
        menuItem_edit = menu.findItem(R.id.menu_edit);

        menuItem_exit.setVisible(false);
        menuItem_edit.setVisible(false);
        menuItem_settle.setVisible(false);
        menuItem_history.setVisible(false);
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
            postObject.put("type", "group");
            postObject.put("groupId", groupId);
            postObject.put("groupName", groupName);
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
                if (listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).getUserId().equalsIgnoreCase(AppController.getUserId())) {
                    if (listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).getSelected()) {
                        deleteList.remove(listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).getId());
                        listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).setSelected(false);
                        linearlayout_child.setBackgroundColor(ContextCompat.getColor(GroupExpenseActivity.this, R.color.white));
                        selected--;
                        if (selected == 0) {
                            linear_buttons.setVisibility(View.GONE);
//                            _snackbar.dismiss();
                        }
//                        else if (selected == 1) {
//                            linear_buttons.setVisibility(View.VISIBLE);
//                            _snackbar.setText(selected + " item");
//                        }
                        else {
                            bounceAnimation();
//                            _snackbar.setText(selected + " items");
                        }
                    } else {
                        deleteList.add(listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).getId());
                        listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).setSelected(true);
                        linearlayout_child.setBackgroundColor(ContextCompat.getColor(GroupExpenseActivity.this, R.color.red_shade));
                        if (selected >= 0) {
                            selected++;
                            bounceAnimation();
//                            showSnackBar(selected + " item", "Delete");
                        }
//                        else if (selected > 0) {
//                            selected++;
//                            _snackbar.setText(selected + " items");
//                        }
                    }
                } else {
                    Toast.makeText(GroupExpenseActivity.this, "Mind your own business " +
                            "" + AppController.getUserName() + "?? :P \n" +
                            "Please ask " + userParentArray.get(groupPosition).getName() + " to delete this", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected click event");
        }
    }

    private interface Expandable extends ExpandableItemConstants {
    }

    private static class MyGroupItem {
        final List<GroupExpenseActivity.MyChildItem> children;
        public long id;
        String month, expense;
        boolean selected;

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
        boolean selected;

        MyChildItem(long id, String date, String spentOn, String amount, String type, boolean selected) {
            this.id = id;
            this.date = formatDate(date);
            this.spentOn = spentOn;
            this.amount = amount;
            this.type = type;
            this.selected = selected;
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
        }
    }

    private class MyAdapter extends AbstractExpandableItemAdapter<GroupExpenseActivity.MyGroupViewHolder, GroupExpenseActivity.MyChildViewHolder> {
        List<GroupExpenseActivity.MyGroupItem> mItems;

        MyAdapter() {
            setHasStableIds(true); // this is required for expandable feature.
            mItems = new ArrayList<>();
            for (int i = 0; i < userParentArray.size(); i++) {
                GroupExpenseActivity.MyGroupItem group = new GroupExpenseActivity.MyGroupItem(i, userParentArray.get(i).getName(),
                        userParentArray.get(i).getExpense());
                List<ExpenseModel> list = listDataChild.get(userParentArray.get(i).getName());
                for (int j = 0; j < list.size(); j++) {
                    group.children.add(new GroupExpenseActivity.MyChildItem(j, list.get(j).getDate(),
                            list.get(j).getSpentOn(), list.get(j).getAmount(),
                            list.get(j).getType(), list.get(j).getSelected()));
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
        public GroupExpenseActivity.MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_month, parent, false);
            return new GroupExpenseActivity.MyGroupViewHolder(v);
        }

        @Override
        public GroupExpenseActivity.MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_expense, parent, false);
            return new GroupExpenseActivity.MyChildViewHolder(v, mItemOnClickListener);
        }

        @Override
        public void onBindGroupViewHolder(GroupExpenseActivity.MyGroupViewHolder holder, int groupPosition, int viewType) {
            GroupExpenseActivity.MyGroupItem group = mItems.get(groupPosition);
//            holder.setIsRecyclable(false);
            holder.textView_monthName.setText(group.month);
            holder.textView_monthAmt.setText(group.expense);

            holder.itemView.setClickable(true);

            final int expandState = holder.getExpandStateFlags();
            if ((expandState & GroupExpenseActivity.Expandable.STATE_FLAG_IS_UPDATED) != 0) {
                int textColor;
                Drawable img_arrow;
                if ((expandState & GroupExpenseActivity.Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                    textColor = ContextCompat.getColor(GroupExpenseActivity.this, R.color.colorAccent);
                    img_arrow = ContextCompat.getDrawable(GroupExpenseActivity.this, R.drawable.arrow_more);
                } else {
                    textColor = ContextCompat.getColor(GroupExpenseActivity.this, R.color.colorPrimaryDark);
                    img_arrow = ContextCompat.getDrawable(GroupExpenseActivity.this, R.drawable.arrow_less);
                }
                holder.textView_monthName.setTextColor(textColor);
                holder.textView_monthAmt.setTextColor(textColor);
                holder.imageView_arrow.setBackground(img_arrow);
            }
        }

        @Override
        public void onBindChildViewHolder(GroupExpenseActivity.MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
            GroupExpenseActivity.MyChildItem child = mItems.get(groupPosition).children.get(childPosition);
//            holder.setIsRecyclable(false);
            holder.textView_date.setText(child.date);
            holder.textView_amount.setText(child.amount);
            holder.textView_spentOn.setText(child.spentOn);
//            holder.imageView_type.setVisibility(View.INVISIBLE);
//            if (child.type.equalsIgnoreCase("personal")) {
//                holder.imageView_type.setImageResource(R.drawable.user_default);
//            } else {
//                holder.imageView_type.setImageResource(R.drawable.group_default);
//            }

//            if (listDataChild.get(userParentArray.get(groupPosition).getName()).get(childPosition).getSelected()) {
//                holder.linearlayout_child.setBackgroundColor(ContextCompat.getColor(GroupExpenseActivity.this,
//                        R.color.red_shade));
//            } else {
//                holder.linearlayout_child.setBackgroundColor(ContextCompat.getColor(GroupExpenseActivity.this,
//                        R.color.white));
//            }
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(GroupExpenseActivity.MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
            return true;
        }
    }
}
