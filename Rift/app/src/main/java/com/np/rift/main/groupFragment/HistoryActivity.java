package com.np.rift.main.groupFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.serverRequest.ServerGetRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by subhechhu on 11/17/2017.
 */

public class HistoryActivity extends AppCompatActivity implements ServerGetRequest.Response {

    String TAG = getClass().getSimpleName();
    RecyclerView recycler_view;
    LinearLayoutManager mLayoutManager;
    CustomAdapterListHistory customAdapterHistory;
    ArrayList<GroupModel> groupModelArrayList;
    AVLoadingIndicatorView progress_default;

    LinearLayout linear_parent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String groupId = getIntent().getStringExtra("groupId");

        linear_parent = (LinearLayout) findViewById(R.id.linear_parent);

        progress_default = (AVLoadingIndicatorView) findViewById(R.id.progress_default);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new android.support.v7.widget.LinearLayoutManager(this);
        recycler_view.setLayoutManager(mLayoutManager);
//        recycler_view.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_view.setItemAnimator(new DefaultItemAnimator());

        getHistory(groupId);

    }

    private void getHistory(String groupId) {
        String url = AppController.getInstance().getString(R.string.domain) +
                "/getSettleHistoryList?groupId=" + groupId;
        Log.e("TAG", "history response: " + url);
        new ServerGetRequest(this, "GET_HISTORY").execute(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return false;
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        Log.e("TAG", "history response: " + response);
        progress_default.setVisibility(View.GONE);
        try {
            if (response != null || !response.equalsIgnoreCase("null")) {
                groupModelArrayList = new ArrayList<>();
                JSONObject responseObject = new JSONObject(response);
                String status = responseObject.getString("status");
                if ("success".equalsIgnoreCase(status)) {
                    JSONArray historyListArray = responseObject.getJSONArray("HistoryList");
                    for (int i = 0; i < historyListArray.length(); i++) {
                        GroupModel groupModel = new GroupModel();
                        JSONObject object = historyListArray.getJSONObject(i);
                        groupModel.setSettleId(object.getString("_id"));
                        groupModel.setSettleBy(object.getString("settledBy"));
                        groupModel.setSettledOn(object.getString("settledOn"));
                        groupModel.setExpenses(Float.parseFloat(object.getString("totalExpense")));
                        groupModelArrayList.add(groupModel);
                    }

                    if (customAdapterHistory == null) {
                        customAdapterHistory = new CustomAdapterListHistory(this,
                                groupModelArrayList);
                        recycler_view.setAdapter(customAdapterHistory);
                        customAdapterHistory.notifyDataSetChanged();

                    } else {
                        customAdapterHistory.notifyDataSetChanged();
                    }

                } else {
                    String errorMessage = responseObject.getString("errorMessage");
                    showSnackBar(errorMessage);
                }
            } else {
                showSnackBar("Something went wrong. Please try again!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(linear_parent, message, Snackbar.LENGTH_INDEFINITE);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }


    //===================================================================================================================================
//===================================================================================================================================
//===============================================                           =========================================================
//=============================================== CUSTOM ADAPER STARTS HERE =========================================================
//===============================================                           =========================================================
//===================================================================================================================================
//===================================================================================================================================
//===================================================================================================================================
//===================================================================================================================================
    class CustomAdapterListHistory extends RecyclerView.Adapter<CustomAdapterListHistory.MyViewHolder> {
        private final List<GroupModel> groupList;
        private final Context context;
        private String TAG = getClass().getSimpleName();

        CustomAdapterListHistory(Context context, List<GroupModel> groupList) {
            HashSet<String> checkSet = new HashSet<>();
            this.groupList = groupList;
            this.context = context;
        }

        @Override
        public CustomAdapterListHistory.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_group, parent, false);
            return new CustomAdapterListHistory.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final CustomAdapterListHistory.MyViewHolder holder, int position) {
            final GroupModel details = groupList.get(position);
            holder.textView_settleBy.setText(details.getSettleBy());
            holder.textView_settleDate.setText(details.getSettledOn());
            holder.textView_userExpense.setText("Total Expense\n" + details.getExpenses());

            holder.linearlayout_child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(context, "id: " + details.getSettleId(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HistoryActivity.this, SettledActivity.class);
//                        finalSettleObject.put("status", "success");
//                        finalSettleObject.put("settleddata",settleddata);
                    intent.putExtra("from", "history");
                    intent.putExtra("settleId", details.getSettleId());
                    startActivity(intent);
//                    getDialog().dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            final TextView textView_settleBy;
            final TextView textView_settleDate;
            final TextView textView_userExpense;
            final LinearLayout linearlayout_child;
            final RelativeLayout relative_contribute;
            View view_divider;

            MyViewHolder(View view) {
                super(view);

                this.setIsRecyclable(false);

                textView_settleBy = view.findViewById(R.id.textView_group);
                textView_settleDate = itemView.findViewById(R.id.textView_groupName);
                linearlayout_child = itemView.findViewById(R.id.linearlayout_child);
                relative_contribute = itemView.findViewById(R.id.relative_contribute);
                view_divider = itemView.findViewById(R.id.view_divider);
                textView_userExpense = itemView.findViewById(R.id.textView_groupMembers);
                view_divider.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

            }
        }
    }
}
