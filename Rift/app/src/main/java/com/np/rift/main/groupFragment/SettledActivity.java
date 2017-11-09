package com.np.rift.main.groupFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.menuOptions.EditFragment;
import com.np.rift.serverRequest.ServerGetRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by subhechhu on 9/10/2017.
 */

public class SettledActivity extends AppCompatActivity implements ServerGetRequest.Response {

    String group_name, group_id, group_expense;

    TextView textView_total, textView_average, textView_settledOn, textView_settledBy, textView_;
    LinearLayout linearlayout_body, linearlayout_end, linearlayout_main;
    String response;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settled);

        Log.e("TAG", "onCreate Settled()");

        response = getIntent().getStringExtra("response");
        group_name = getIntent().getStringExtra("group_name");
        group_id = getIntent().getStringExtra("group_id");
        group_expense = getIntent().getStringExtra("group_expense");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Settled");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        linearlayout_body = (LinearLayout) findViewById(R.id.linearlayout_body);
        linearlayout_end = (LinearLayout) findViewById(R.id.linearlayout_end);
        linearlayout_main = (LinearLayout) findViewById(R.id.linearlayout_main);

        textView_total = (TextView) findViewById(R.id.textView_total);
        textView_average = (TextView) findViewById(R.id.textView_average);
        textView_settledOn = (TextView) findViewById(R.id.textView_settledOn);
        textView_settledBy = (TextView) findViewById(R.id.textView_settledBy);
        textView_ = (TextView) findViewById(R.id.textView__);

//        ParseJson(response);
        Log.e("TAG", "group_id: " + group_id);
        GetSettledInfo();

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuItem menuItem_exit, menuItem_settle;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);

        menuItem_exit = menu.findItem(R.id.menu_exit);
        menuItem_settle = menu.findItem(R.id.menu_settle);

        menuItem_exit.setVisible(false);
        menuItem_settle.setVisible(false);
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            Intent intent = new Intent(this, GroupPieActivity.class);
            intent.putExtra("group_name", group_name);
            intent.putExtra("group_id", group_id);
            intent.putExtra("group_expense", group_expense);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_history) {
            Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_edit) {
            Bundle bundle = new Bundle();
            bundle.putString("for", "group");
            BottomSheetDialogFragment fragment = new EditFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        }
        return false;
    }

    private void GetSettledInfo() {
        String url = AppController.getInstance().getString(R.string.domain) +
                "/getSettledInfo?groupId=" + group_id;
        new ServerGetRequest(this, "GET_SETTLED").execute(url);

    }

    private void ParseJson(String json) {
        try {
            Log.e("TAG", "json: " + json);
            JSONObject responseObject = new JSONObject(json);
            String status = responseObject.getString("status");
            if ("success".equals(status)) {
                JSONObject settledDataObject = responseObject.getJSONObject("settleddata");

                String settledBy = settledDataObject.getString("settledBy");
                String settledOn = settledDataObject.getString("settledOn");
                group_id = settledDataObject.getString("groupId");

                String totalExpense = settledDataObject.getString("totalExpense");
                String averageExpense = settledDataObject.getString("averageExpense");

                JSONArray membersExpense = settledDataObject.getJSONArray("membersExpense");
                JSONArray settledExpense = settledDataObject.getJSONArray("settledExpense");

                Log.e("TAG", "settledBy:" + settledBy);
                Log.e("TAG", "settledOn:" + settledOn);


                Log.e("TAG", "membersExpense:" + membersExpense);
                Log.e("TAG", "settledExpense:" + settledExpense);
                Log.e("TAG", "settledExpense.length():" + settledExpense.length());

                textView_settledBy.setText(settledBy);
                textView_settledOn.setText(settledOn);

                textView_average.setText(averageExpense);
                textView_total.setText(totalExpense);

                for (int i = 0; i < membersExpense.length(); i++) {
                    JSONObject memberExpenseObject = membersExpense.getJSONObject(i);
                    String member = memberExpenseObject.getString("userName");
                    String expense = memberExpenseObject.getString("userExpense");
                    RenderExpense(member, expense);
                }

                if (settledExpense.length() == 0) {
                    textView_.setText("Expense is settled among the members");
                }
                for (int j = 0; j < settledExpense.length(); j++) {
                    JSONObject settledObject = settledExpense.getJSONObject(j);
                    String from = settledObject.getString("from");
                    String to = settledObject.getString("to");
                    String amount = settledObject.getString("amount");
                    RenderSettled(from, amount, to);
                }

            } else {
                String errorMessage = responseObject.getString("errorMessage");
                showSnackBar(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getJSON() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("settle.json");
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
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            ParseJson(response);
        } else {
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
        }
    }

    private void RenderSettled(String from, String amount, String to) {
        View memberExpense = LayoutInflater.from(this)
                .inflate(R.layout.view_member_unsettled, null, false);
        TextView textView_to = memberExpense.findViewById(R.id.textView_to);
        TextView textView_from = memberExpense.findViewById(R.id.textView_from);
        TextView textView_from_settle = memberExpense.findViewById(R.id.textView_from_settle);
        TextView textView_expense = memberExpense.findViewById(R.id.textView_expense);
        LinearLayout linear_unsettled = memberExpense.findViewById(R.id.linear_unsettled);
        LinearLayout linear_settled = memberExpense.findViewById(R.id.linear_settled);

        if ("-".equals(amount)) {
            linear_unsettled.setVisibility(View.GONE);
            linear_settled.setVisibility(View.VISIBLE);

            textView_from_settle.setText(from);
        } else {
            linear_unsettled.setVisibility(View.VISIBLE);
            linear_settled.setVisibility(View.GONE);

            textView_from.setText(from);
            textView_to.setText(to);
            textView_expense.setText(amount);
        }
        linearlayout_end.addView(memberExpense);
    }

    private void RenderExpense(String member, String expense) {
        View memberExpense = LayoutInflater.from(this)
                .inflate(R.layout.view_member_expense, null, false);
        TextView textView_member = memberExpense.findViewById(R.id.textView_member);
        TextView textView_expense = memberExpense.findViewById(R.id.textView_expense);

        textView_expense.setText(expense);
        textView_member.setText(member);

        linearlayout_body.addView(memberExpense);
    }

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }
}
