package com.np.rift.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.serverRequest.ServerPostRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class AddExpenseActivity extends AppCompatActivity implements
        ServerPostRequest.Response, ServerGetRequest.Response{

    String TAG = getClass().getSimpleName();
    RelativeLayout relative_main;
    SharedPrefUtil sharedPrefUtil;
    String userId;
    List<AddItemModel> expenseList;

    AVLoadingIndicatorView progress_default;
    ExpandableListView expandableListView;
    ImageView imageView_arrow;
    TextView textView_title;
    SwipeRefreshLayout swipeRefreshLayout;
    int selectedCount = 0;
    private MenuItem menu_delete, menu_add;
    private HashSet<String> selectedSet;
    private String selectedSetString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_expense);

        expenseList = new ArrayList<>();
        sharedPrefUtil = new SharedPrefUtil();

        userId = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "userId", "000");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Expense");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        relative_main = (RelativeLayout) findViewById(R.id.relative_main);
        progress_default = (AVLoadingIndicatorView) findViewById(R.id.progress_default);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        GetExpenseDetails();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                GetExpenseDetails();
            }
        });
    }

    private void GetExpenseDetails() {
        swipeRefreshLayout.setEnabled(false);
//        String url = AppController.getInstance().getString(R.string.domain)
//                + "/getExpense?userId=" + userId;
        Progress(true);
        String url = "http://www.mocky.io/v2/57cfde922600006f1c64ffec";
        new ServerGetRequest(this, "GET_EXPENSES").execute(url);
    }

    private void PostItems(JSONArray itemsArray) {
        try {
            JSONObject postObject = new JSONObject();
            postObject.put("type", "personal");
            postObject.put("userId", userId);
            postObject.put("data", itemsArray);
            String url = AppController.getInstance().getString(R.string.domain)
                    + "/addExpense";
            Log.e(TAG, "url: " + url);
            Log.e(TAG, "postObject: " + postObject.toString());
//            new ServerPostRequest(this, "ADD_ITEM").execute(url, postObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(relative_main, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

    @Override
    public void getPostResult(String response, String requestCode, int responseCode) {
        Log.e(TAG, "response:" + response);
        swipeRefreshLayout.setEnabled(true);
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                if ("success".equals(status)) {
                    GetExpenseDetails();
                    showSnackBar(AppController.getInstance().getString(R.string.fetching));
                } else {
                    String errorMessage = responseObject.getString("errorMessage");
                    showSnackBar(errorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
        }
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
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            BottomSheetDialogFragment fragment = new AddExpFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
            return true;
        }
        if (item.getItemId() == R.id.menu_delete) {
            String message;
            if (selectedCount > 1) {
                message = "Are you sure you want to delete " + selectedCount + " items?";
            } else {
                message = "Are you sure you want to delete " + selectedCount + " item?";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete entry")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e(TAG, "after trimming: " + selectedSet);
                            selectedSetString = selectedSet.toString();
                            selectedSetString = selectedSetString
                                    .substring(1, selectedSetString.length() - 1)
                                    .replaceAll(" ", "");
                            Log.e(TAG, "after trimming: " + selectedSetString);
                            String[] selectedStg = selectedSetString.split(",");
//                                DeleteRequest(selectedStg);

                            dialog.dismiss();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return false;
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_add, menu);
//
//        return true;
//    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if (selectedCount == 0)
            inflater.inflate(R.menu.menu_add, menu);
        else
            inflater.inflate(R.menu.menu_delete, menu);
        return super.onPrepareOptionsMenu(menu);
    }


    public void ParseJson(String s) {
        Log.e("Data.java", "data: " + s);
        try {
            ArrayList<MonthModel> parentArray = new ArrayList<>();
            ArrayList<ExpenseModel> childArray;
            LinkedHashMap<String, List<ExpenseModel>> listDataChild = new LinkedHashMap<>();

            parentArray.clear();
            listDataChild.clear();

            JSONArray mainArray = new JSONArray(s);
            for (int i = 0; i < mainArray.length(); i++) {
                MonthModel month = new MonthModel();
                JSONObject subObject = mainArray.getJSONObject(i);
                month.setName(subObject.getString("name"));
                parentArray.add(i, month);
                JSONArray subArray = subObject.getJSONArray("sub_services");
                Log.e(getClass().getSimpleName(), i + "subArray. " + subArray);

                childArray = new ArrayList<>();
                for (int j = 0; j < subArray.length(); j++) {
                    ExpenseModel childClass = new ExpenseModel();
                    JSONObject innerSubObj = subArray.getJSONObject(j);
                    childClass.setName(innerSubObj.getString("name"));
                    childClass.setDescription(innerSubObj.getString("description"));
                    childClass.setPrice(innerSubObj.getString("price"));
                    childArray.add(childClass);
                }
                Log.d(getClass().getSimpleName(), "childCount: " + childArray.size());

                listDataChild.put(parentArray.get(i).getName(), childArray);
            }

            CustomAdapterAdd adapter = new CustomAdapterAdd(getLayoutInflater(), this, parentArray, listDataChild);
            expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
            expandableListView.setAdapter(adapter);

            expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem == 0 && listIsAtTop()) {
                        swipeRefreshLayout.setEnabled(true);
                    } else {
                        swipeRefreshLayout.setEnabled(false);
                    }
                }
            });

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                    TextView textView_description = view.findViewById(R.id.textView_description);
                    Log.d("subehchhu", "Child Clicked: " + textView_description.getText().toString());
                    return true;
                }
            });
            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                    imageView_arrow = view.findViewById(R.id.imageView_arrow);
                    textView_title = view.findViewById(R.id.textView_title);
                    if (expandableListView.isGroupExpanded(i)) {
                        imageView_arrow.setImageResource(R.drawable.arrow_down);
                        textView_title.setTextColor(ContextCompat.getColor(AddExpenseActivity.this,
                                R.color.colorPrimaryDark));
                    } else {
                        textView_title.setTextColor(ContextCompat.getColor(AddExpenseActivity.this,
                                R.color.colorAccent));
                        imageView_arrow.setImageResource(R.drawable.arrow_up);
                    }
                    return false;
                }
            });
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int i) {

                }
            });
            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int i) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean listIsAtTop() {
        return expandableListView.getChildCount() == 0 || expandableListView.getChildAt(0).getTop() == 0;
    }

    private void Progress(boolean show) {
        if (show) {
            progress_default.show();
        } else {
            progress_default.hide();
        }
    }

    public void AddItems(JSONArray items) {
        showSnackBar(AppController.getInstance().getString(R.string.updating));
        PostItems(items);
    }
}
