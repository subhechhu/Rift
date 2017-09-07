package com.np.rift.main.groupFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import com.np.rift.main.personalFragment.addExp.AddExpFragment;
import com.np.rift.serverRequest.ServerGetRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;


public class GroupActivity extends AppCompatActivity implements OnChartValueSelectedListener,
        ServerGetRequest.Response {

    String group_name, group_id;
    View linearlayout_main;
    ArrayList<GroupModel> pieCharList;
    float totalExpense;
    ViewPager viewPager;
    GroupAdapter groupAdapter;
    private PieChart mChart;

    List<Fragment> fragments;
    CircleIndicator indicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group);

        group_name = getIntent().getStringExtra("group_name");
        group_id = getIntent().getStringExtra("group_id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(group_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        linearlayout_main = findViewById(R.id.linearlayout_main);
        indicator = (CircleIndicator) findViewById(R.id.indicator);

        mChart = (PieChart) findViewById(R.id.chart_pie);

        Log.e("TAG", "onCreate grp name: " + group_name);
        RequestGroupInfo(group_id);
    }

    private void initPie(ArrayList<GroupModel> pieCharList) {
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterText(generateCenterSpannableText());
        mChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
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
        ParseResponse(getJSON());
        String url = "" + "/getGroupInfo?groupId=" + group_id;
//        new ServerGetRequest(this, "GET_GROUP_INFO").execute(url);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.e("VAL SELECTED",
                "Value: " + e.getY() + ", xIndex: " + e.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.e("PieChart", "nothing selected");
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            ParseResponse(response);
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

    private void ParseResponse(String response) {
        try {
            Log.e("TAG", "parseResponse: " + response);
            pieCharList = new ArrayList<>();
            JSONObject responseObject = new JSONObject(response);
            JSONArray chartArray = responseObject.getJSONArray("chartObject");

            for (int i = 0; i < chartArray.length(); i++) {
                GroupModel groupModel = new GroupModel();

                JSONObject membersObject = chartArray.getJSONObject(i);
                groupModel.setMembersId(membersObject.getString("membersId"));
                groupModel.setGroupMembers(membersObject.getString("memberName"));
                groupModel.setExpenses((float) membersObject.getInt("expenses"));
                totalExpense += totalExpense + (float) membersObject.getInt("expenses");
                pieCharList.add(groupModel);

                initPie(pieCharList);
                initViewPager(pieCharList);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViewPager(ArrayList<GroupModel> pieCharList) {
        ArrayList<String> memberIds=new ArrayList<>();
        ArrayList<String> memberNames=new ArrayList<>();
        ArrayList<String> memberExpense=new ArrayList<>();

        for(int i=0;i<pieCharList.size();i++){
            memberIds.add(pieCharList.get(i).getMembersId());
            memberNames.add(pieCharList.get(i).getGroupMembers());
            memberExpense.add(String.valueOf(pieCharList.get(i).getExpenses()));
        }

        fragments = buildFragments(memberIds, memberNames, memberExpense);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        groupAdapter = new GroupAdapter(this, getSupportFragmentManager(), fragments, memberIds);
        viewPager.setAdapter(groupAdapter);
        indicator.setViewPager(viewPager);
    }

    private List<Fragment> buildFragments(ArrayList<String> memberIds,
                                          ArrayList<String> memberNames,
                                          ArrayList<String> memberExpense) {
        List<android.support.v4.app.Fragment> fragments = new ArrayList<>();
        for(int i = 0; i<memberIds.size(); i++) {
            Bundle b = new Bundle();
            b.putInt("position", i);
            b.putString("memberId", memberIds.get(i));
            b.putString("memberNames", memberNames.get(i));
            b.putString("memberExpense", memberExpense.get(i));
            b.putString("groupId", group_id);

            InnerGroupFragment innerGroupFragment=new InnerGroupFragment();
            innerGroupFragment.setArguments(b);
            fragments.add(innerGroupFragment);
        }
        return fragments;
    }


    private void RenderPieChart(ArrayList<GroupModel> pieCharList) {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        for (int i = 0; i < pieCharList.size(); i++) {
            entries.add(new PieEntry(pieCharList.get(i).getExpenses(),
                    pieCharList.get(i).getGroupMembers()));
        }
        PieDataSet dataSet = new PieDataSet(entries, group_name);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

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
//        dataSet.setSelectionShift(0f);


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
            InputStream is = AppController.getContext().getAssets().open("get_group_details.json");
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
        showSnackBar(AppController.getInstance().getString(R.string.updating), "OK");
        PostItems(items);
    }

    private void PostItems(JSONArray itemsArray) {
        try {
            JSONObject postObject = new JSONObject();
            postObject.put("type", "group");
            postObject.put("userId", AppController.getUserId());
            postObject.put("groupId", group_id);
            postObject.put("data", itemsArray);
            String url = AppController.getInstance().getString(R.string.domain)
                    + "/addExpense";
            Log.e("TAG", "url: " + url);
            Log.e("TAG", "postObject: " + postObject.toString());
//            new ServerPostRequest(this, "ADD_ITEM").execute(url, postObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
