package com.np.rift.main.groupFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.main.HomeActivity;
import com.np.rift.main.menuOptions.EditFragment;
import com.np.rift.main.personalFragment.addExp.AddExpFragment;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.serverRequest.ServerPostRequest;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class GroupPieActivity extends AppCompatActivity implements OnChartValueSelectedListener,
        ServerGetRequest.Response, ServerPostRequest.Response {

    String group_name, group_id, group_expense;
    View linearlayout_main;
    ArrayList<GroupModel> pieCharList;
    float totalExpense;
    float averageExpense;
    ArrayList<String> settledDetails;
    ArrayList<Float> actualContribution;
    ArrayList<String> users;
    DecimalFormat df = new DecimalFormat("0.00");

    JSONArray finalArray, memberExpArray;
    JSONObject finalSettleObject, settleddata;

    TextView textView_groupName, textView_expense, textView_more;
    RelativeLayout relative_more, relative_graph;
    Button button_refresh;
    AVLoadingIndicatorView progress_default;
    int totalMembers;
    JSONArray chartArray;
    boolean settled;
    private PieChart mChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group);

        group_name = getIntent().getStringExtra("group_name");
        group_id = getIntent().getStringExtra("group_id");
        group_expense = getIntent().getStringExtra("group_expense");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(group_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progress_default = (AVLoadingIndicatorView) findViewById(R.id.progress_default);

        button_refresh = (Button) findViewById(R.id.button_refresh);

        linearlayout_main = findViewById(R.id.linearlayout_main);
        relative_more = (RelativeLayout) findViewById(R.id.relative_more);
        relative_graph = (RelativeLayout) findViewById(R.id.relative_graph);

        textView_groupName = (TextView) findViewById(R.id.textView_groupName);
        textView_expense = (TextView) findViewById(R.id.textView_expense);
        textView_more = (TextView) findViewById(R.id.textView_more);

        textView_groupName.setText("Total Expense");
        textView_more.setText("View expenses in detail");

        mChart = (PieChart) findViewById(R.id.chart_pie);
        mChart.setNoDataText("Please Add The Expenses To See The Pie Chart");
        mChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestGroupInfo(group_id);
            }
        });

        relative_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupPieActivity.this, GroupExpenseActivity.class);
                intent.putExtra("groupId", group_id);
                intent.putExtra("groupName", group_name);
                startActivity(intent);
            }
        });

        Log.e("TAG", "onCreate grp name: " + group_name);
        RequestGroupInfo(group_id);
    }

    private void progress(boolean show) {
        if (show) {
            progress_default.setVisibility(View.VISIBLE);
        } else {
            progress_default.setVisibility(View.INVISIBLE);
        }
    }

//    private void showSnackBar(String message) {
//        final Snackbar _snackbar = Snackbar.make(relative_graph, message, Snackbar.LENGTH_LONG);
//        _snackbar.setAction("Got it", new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                _snackbar.dismiss();
//            }
//        }).show();
//    }

    public void createDialog(final String from) {
        String title = "", body = "";
        if ("Exit".equals(from)) {
            title = "Exit Group";
            if (totalMembers == 1) {
                body = "Are you sure you want to exit " + group_name + "?";
            } else {
                body = "Are you sure you want to exit and delete " + group_name + "?";
            }

        } else if ("Settle".equalsIgnoreCase(from)) {
            title = "Settle Expense";
            body = "Are you sure you want to settle expenses on " + group_name + "?";
        }

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(body)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if ("Exit".equals(from)) {
                            RequestExit();
                            HomeActivity.isDeleted = true;
                            dialog.dismiss();
                            finish();
                        } else {
                            try {
                                progress(true);
                                Settle();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

    private void Settle() {
        try {
            JSONObject memberExp;
            memberExpArray = new JSONArray();
            settledDetails = new ArrayList<>();
            users = new ArrayList<>();
            actualContribution = new ArrayList<>();
            averageExpense = totalExpense / (float) totalMembers;

            for (int i = 0; i < pieCharList.size(); i++) {
                memberExp = new JSONObject();
                memberExp.put("userName", pieCharList.get(i).getGroupMembers());
                memberExp.put("userExpense", pieCharList.get(i).getExpenses());
                users.add(pieCharList.get(i).getGroupMembers());
                actualContribution.add(pieCharList.get(i).getExpenses() - averageExpense);
                memberExpArray.put(memberExp);
            }
            finalArray = new JSONArray();
            finalSettleObject = new JSONObject();
            settleddata = new JSONObject();
            finalSettleObject.put("settle", true);
            settleddata.put("groupId", group_id);
            settleddata.put("totalExpense", totalExpense);
            settleddata.put("averageExpense", averageExpense);
            settleddata.put("settledBy", AppController.getUserName());
            settleddata.put("settledOn", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

            CheckPendingCalculation(actualContribution, users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CheckPendingCalculation(ArrayList<Float> actualContribution, ArrayList<String> users) {
        if (!VerifyExpense(actualContribution))
            BreakExpense(actualContribution, users);
        else {
            try {
                settleddata.put("membersExpense", memberExpArray);
                settleddata.put("settledExpense", finalArray);
                finalSettleObject.put("settleddata",settleddata);
                Log.e("TAG", "finalSettleObject: " + finalSettleObject);
//                String url = AppController.getInstance().getString(R.string.domain);
                String url = AppController.getInstance().getString(R.string.domain) + "/settleExpenses";
                new ServerPostRequest(this, "SETTLE_CONFIRM").execute(url, finalSettleObject.toString());
//                dummySettled(finalSettleObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean VerifyExpense(List<Float> actualExp) {
        for (Float anActualExp : actualExp) {
            int intVal = Math.round(anActualExp);
            if (Math.round(intVal) != 0) {
                return false;
            }
        }
        return true;
    }

    private void BreakExpense(ArrayList<Float> actualExp, ArrayList<String> users) {
        JSONObject jsonObject = new JSONObject();
        float smallest = actualExp.get(0);
        float largest = actualExp.get(0);
        String smallestHolder = users.get(0);
        String largestHolder = users.get(0);
        int smallPosition = 0, largePosition = 0;

        for (int i = 1; i < actualExp.size(); i++) {
            if (actualExp.get(i) > largest) {
                largest = actualExp.get(i);
                largestHolder = users.get(i);
                largePosition = i;
            } else if (actualExp.get(i) < smallest) {
                smallest = actualExp.get(i);
                smallestHolder = users.get(i);
                smallPosition = i;
            }
        }

        try {
            if (Math.abs(smallest) > largest) {
                jsonObject.put("from", smallestHolder);
                jsonObject.put("to", largestHolder);
                jsonObject.put("amount", df.format(largest));
                actualExp.set(largePosition, (float) 0);
                actualExp.set(smallPosition, largest - Math.abs(smallest));
            } else {
                jsonObject.put("from", smallestHolder);
                jsonObject.put("to", largestHolder);
                jsonObject.put("amount", df.format(Math.abs(smallest)));
                actualExp.set(smallPosition, (float) 0);
                actualExp.set(largePosition, (largest - Math.abs(smallest)));
            }
            finalArray.put(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CheckPendingCalculation(actualExp, users);
    }


    private void RequestExit() {
        progress(true);
        String url = AppController.getInstance().getString(R.string.domain) +
                "/removeUser?userId=" + AppController.getUserId() +
                "&groupId=" + group_id;
        new ServerGetRequest(this, "REQUEST_EXIT").execute(url);
    }

    private void initPie(ArrayList<GroupModel> pieCharList) {
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
//        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.99f);

//        mChart.setCenterText(generateCenterSpannableText());
        mChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0f);
        mChart.setRotationEnabled(true);

        mChart.setHighlightPerTapEnabled(true);

        mChart.setOnChartValueSelectedListener(this);

        RenderPieChart(pieCharList);

        mChart.animateY(1500, Easing.EasingOption.EaseInCirc);
        Legend l = mChart.getLegend();
        l.setEnabled(false);
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("Group Expense\n" + totalExpense);
        s.setSpan(new RelativeSizeSpan(2f), 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimaryDark)),
                14, s.length(), 0);
        return s;
    }

    private void RequestGroupInfo(String group_id) {
        progress(true);
//        parseResponse(getJSON());
        String url = AppController.getInstance().getString(R.string.domain) + "/getGroupDetails?groupId=" + group_id;
        new ServerGetRequest(this, "GET_GROUP_INFO").execute(url);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Toast.makeText(this, "Expense: " + e.getY(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {
        Log.e("PieChart", "nothing selected");
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            parseResponse(response);
        } else {
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong), "OK");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.menu_add) {
            Bundle bundle = new Bundle();
            bundle.putString("for", "group");
            BottomSheetDialogFragment fragment = new AddExpFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
            return true;
        } else if (item.getItemId() == R.id.menu_edit) {
            Bundle bundle = new Bundle();
            bundle.putString("for", "group");
            bundle.putString("groupName", group_name);
            BottomSheetDialogFragment fragment = new EditFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        } else if (item.getItemId() == R.id.menu_exit) {
            if (settled) {
                createDialog("Exit");
            } else {
                showSnackBar("Group should be settled before you can exit it", "OK");
            }
        } else if (item.getItemId() == R.id.menu_history) {
            Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_settle) {
            createDialog("Settle");
        }else if( item.getItemId() == R.id.menu_share){
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
            String shareBodyText = "Hi!! You have been invited to join a group by "+AppController.getUserName()
                    +"\nGroup Name- *"+group_name+"*\nGroup ID- *"+group_id+"*\n\nLet's Rift!!";
//                String shareBodyText = "Hi!.You have been invited by to join group *"+group_name
//                        +"* having id *"+group_id+"* by "+AppController.getUserName()+"\nLet's Rift!!";
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
        }
        return false;
    }

    private void parseResponse(String response) {
        progress(false);
        try {
            Log.e("TAG", "parseResponse: " + response);
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            settled = responseObject.getBoolean("settled");
            if ("success".equalsIgnoreCase(status)) {
                totalMembers = responseObject.getInt("totalMembers");
                pieCharList = new ArrayList<>();

                totalExpense = responseObject.getInt("totalExpense");
                textView_expense.setText("" + totalExpense);

                chartArray = responseObject.getJSONArray("chartObject");

                for (int i = 0; i < chartArray.length(); i++) {
                    GroupModel groupModel = new GroupModel();
                    JSONObject membersObject = chartArray.getJSONObject(i);
                    groupModel.setMembersId(membersObject.getString("userId"));
                    groupModel.setGroupMembers(membersObject.getString("userName"));
                    groupModel.setExpenses((float) membersObject.getInt("expenses"));
//                    totalExpense += totalExpense + (float) membersObject.getInt("expenses");
                    pieCharList.add(groupModel);
                }
                initPie(pieCharList);
            } else {
                String errorMessage = responseObject.getString("errorMessage");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void RenderPieChart(ArrayList<GroupModel> pieCharList) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < pieCharList.size(); i++) {
            entries.add(new PieEntry(pieCharList.get(i).getExpenses(),
                    pieCharList.get(i).getGroupMembers()));
        }
        PieDataSet dataSet = new PieDataSet(entries, group_name);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.getXValuePosition();

        ArrayList<Integer> colors = new ArrayList<>();

        colors.add(ColorTemplate.getHoloBlue());

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        dataSet.setColors(colors);
        dataSet.setSelectionShift(12f);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);

        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);

        mChart.highlightValues(null);
        mChart.invalidate();
    }

    private void showSnackBar(String message, final String action) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

    private String getJSON() {
        String json = null;
        try {
            InputStream is = AppController.getContext().getAssets().open("group_detail.json");
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

    public void AddItems(JSONArray items) {
//        showSnackBar(AppController.getInstance().getString(R.string.updating), "OK");
        Log.e("TAG", "addItems GroupPieActivity");
        PostItems(items);
    }

    private void PostItems(JSONArray itemsArray) {
        try {
            JSONObject postObject = new JSONObject();
            postObject.put("type", "group");
            postObject.put("userId", AppController.getUserId());
            postObject.put("userName", AppController.getUserName());
            postObject.put("groupId", group_id);
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

    @Override
    public void getPostResult(String response, String requestCode, int responseCode) {
        progress(false);
        if (response != null && !response.equalsIgnoreCase("null")
                && !response.isEmpty()) {
            if ("REQUEST_EXIT".equalsIgnoreCase(requestCode)) {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    if ("success".equalsIgnoreCase(status)) {
                        HomeActivity.isDeleted = true;
                        finish();
                    } else {
                        String errorMessage = responseObject.getString("errorMessage");
                        showSnackBar(errorMessage, "OK");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("GET_GROUP_INFO".equalsIgnoreCase(requestCode)) {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    progress(false);
                    if ("success".equals(status)) {
                        parseResponse(response);
                    } else {
                        String errorMessage = responseObject.getString("errorMessage");
                        showSnackBar(errorMessage, "OK");
                    }
                } catch (Exception e) {
                    progress(false);
                    e.printStackTrace();
                }
            } else if ("ADD_ITEM".equalsIgnoreCase(requestCode)) {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    progress(false);
                    if ("success".equals(status)) {
                        RequestGroupInfo(group_id);
                    } else {
                        String errorMessage = responseObject.getString("errorMessage");
                        showSnackBar(errorMessage, "OK");
                    }
                } catch (Exception e) {
                    progress(false);
                    e.printStackTrace();
                }
            } else if ("SETTLE_CONFIRM".equalsIgnoreCase(requestCode)) {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String status = responseObject.getString("status");
                    progress(false);
                    if ("success".equalsIgnoreCase(status)) {

                        HomeActivity.settled = true;

                        Intent intent = new Intent(GroupPieActivity.this, SettledActivity.class);
//                        finalSettleObject.put("status", "success");
//                        finalSettleObject.put("settleddata",settleddata);
                        intent.putExtra("response", finalSettleObject.toString());
                        intent.putExtra("group_name", group_name);
                        intent.putExtra("group_id", group_id);
                        intent.putExtra("group_expense", group_expense);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = responseObject.getString("errorMessage");
                        showSnackBar(errorMessage, "OK");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    progress(false);
                }
            }
        } else {
            progress(false);
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong), "OK");
        }
    }

    public void dummySettled(JSONObject responseObject) {
        try {
//            JSONObject responseObject = new JSONObject(response);
//            String status = responseObject.getString("status");
            progress(false);
//            if ("success".equalsIgnoreCase(status)) {
            Intent intent = new Intent(GroupPieActivity.this, SettledActivity.class);
            finalSettleObject.put("status", "success");
            intent.putExtra("response", finalSettleObject.toString());
            intent.putExtra("group_name", group_name);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            progress(false);
        }
    }
}
