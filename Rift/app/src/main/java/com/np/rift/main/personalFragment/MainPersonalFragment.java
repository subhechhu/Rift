package com.np.rift.main.personalFragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.connection.NetworkCheck;
import com.np.rift.main.HomeActivity;
import com.np.rift.main.personalFragment.addExp.PersonalExpenseActivity;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class MainPersonalFragment extends Fragment implements OnChartGestureListener,
        OnChartValueSelectedListener, ServerGetRequest.Response {

    private final SimpleDateFormat format_toGet = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat format_month = new SimpleDateFormat("MMM");

    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    TextView textView_total, textView_recent, texrView_month, textView_error;
    View relative_main, relative_error, linear_main;
    AVLoadingIndicatorView progress_default;

    ArrayList<Entry> values;
    ArrayList<ILineDataSet> dataSets;
    MyMarkerView mv;

    FloatingActionButton fab_add;
    float dX;
    float dY;
    int lastAction;
    HashSet<String> filteredSet = new HashSet<>();
    HashMap<Integer, Integer> filteredMap = new HashMap<>();
    Date date = new Date();
    SharedPrefUtil sharedPrefUtil;
    ViewPager vp;
    Button button_refresh;
    LinearLayout linear_month;
    LineChart mChart;

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
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentValue = getArguments() != null ? getArguments().getInt("val") : 1;
        sharedPrefUtil = new SharedPrefUtil();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_personal, container, false);
        fab_add = fragmentView.findViewById(R.id.fab_add);
        button_refresh = fragmentView.findViewById(R.id.button_refresh);

        vp = getActivity().findViewById(R.id.viewPager);

        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vp.getAdapter().notifyDataSetChanged();

            }
        });

        linear_month= fragmentView.findViewById(R.id.linear_month);
        linear_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    startActivity(new Intent(mActivity, PersonalExpenseActivity.class));
                } else {
                    showSnackBarInternet("No Internet");
                    ((HomeActivity) getActivity()).noInternet();
                }
            }
        });

        String currentMonth = format_month.format(date);
        getTotalMonthDate();

        progress_default = fragmentView.findViewById(R.id.progress_default);
        relative_main = fragmentView.findViewById(R.id.relative_main);
        relative_error = fragmentView.findViewById(R.id.relative_error);
        linear_main = fragmentView.findViewById(R.id.linear_main);
        textView_error = fragmentView.findViewById(R.id.textView_error);
        textView_total = fragmentView.findViewById(R.id.textView_total);
        textView_recent = fragmentView.findViewById(R.id.textView_recent);
        texrView_month = fragmentView.findViewById(R.id.texrView_month);
        texrView_month.setText(currentMonth + " Expense");

        mChart = fragmentView.findViewById(R.id.chart_line);
        mChart.setNoDataText("Please Add The Expenses To See The Graph");
        mChart.setNoDataTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));


        values = new ArrayList<>();
        dataSets = new ArrayList<>();

        if (NetworkCheck.isInternetAvailable()) {
            GetPersonalExpense();
//            parseJson(getJSON());
        } else {
            showSnackBarInternet("No Internet");
            ((HomeActivity) getActivity()).noInternet();
        }

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    startActivity(new Intent(mActivity, PersonalExpenseActivity.class));
                } else {
                    showSnackBarInternet("No Internet");
                    ((HomeActivity) getActivity()).noInternet();
                }
            }
        });

        textView_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    GetPersonalExpense();
                } else {
                    showSnackBarInternet("No Internet");
                    ((HomeActivity) getActivity()).noInternet();
                }
            }
        });

        return fragmentView;
    }

    private void getTotalMonthDate() {
        filteredMap.clear();
        filteredSet.clear();
        int lastDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        Log.e("TAG", "lasatDay: " + lastDay);

        for (int i = 1; i <= lastDay; i++) {
            if (filteredSet.add(String.valueOf(i))) {
                filteredMap.put(i, 0);
            }
        }
    }

    private void showSnackBar(String message, final String action) {
        final Snackbar _snackbar = Snackbar.make(relative_main, message, Snackbar.LENGTH_INDEFINITE);
        _snackbar.setAction(action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("ReTry".equalsIgnoreCase(action)) {
                    GetPersonalExpense();
                }
                _snackbar.dismiss();
            }
        }).show();
    }

    private void showSnackBarInternet(String message) {
        final Snackbar _snackbar = Snackbar.make(relative_main, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkCheck.isInternetAvailable()) {
                    GetPersonalExpense();
//                    parseJson(getJSON());
                } else {
                    showSnackBarInternet("No Internet");
                    ((HomeActivity) getActivity()).noInternet();
                }
            }
        }).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("TAG", "onResume mainPersonal");
        if (sharedPrefUtil.getSharedPreferenceBoolean(AppController.getContext(), "refreshGraph", false)) {
//            showSnackBar("Updating The Expense", "OK");
            values.clear();
            dataSets.clear();
            getTotalMonthDate();
            sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(), "refreshGraph", false);
            GetPersonalExpense();
        }
    }

    private void GetPersonalExpense() {
        progress(true);
        String url = AppController.getInstance().getString(R.string.domain)
                + "/getGraphData?userId=" + AppController.getUserId();
        Log.e("TAG", "url: " + url);
        new ServerGetRequest(this, "PERSONAL_EXP").execute(url);
    }

    private void initGraph(JSONArray graphArray) {
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        Log.e("TAG", "mv: " + mv);

        if (mv == null) {
            mv = new MyMarkerView(getActivity(), R.layout.view_custom_marker);
        }
        mv.setChartView(mChart);
        mChart.setMarker(mv);

        mChart.getAxisRight().setEnabled(false);

        mChart.getViewPortHandler().setMaximumScaleY(2f);
        mChart.getViewPortHandler().setMaximumScaleX(2f);

        try {
            for (int i = 0; i < graphArray.length(); i++) {
                JSONObject graphObject = graphArray.getJSONObject(i);
                String date_str = graphObject.getString("date");
                Date date = format_toGet.parse(date_str);
                SimpleDateFormat dt1 = new SimpleDateFormat("dd");
                int strdate = Integer.parseInt(dt1.format(date));

                int price = graphObject.getInt("amount");

                if (filteredSet.add(String.valueOf(strdate))) {
                    filteredMap.put(strdate, price);
                } else {
                    int currentValue = filteredMap.get(strdate);
                    currentValue += graphObject.getInt("amount");
                    filteredMap.put(strdate, currentValue);
                }
            }

            TreeMap filterMap = new TreeMap<>(filteredMap);
            Iterator it = filterMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                values.add(new Entry(Float.parseFloat(pair.getKey().toString()),
                        Float.parseFloat(pair.getValue().toString())));
                it.remove();
            }
            LineDataSet set1;

            set1 = new LineDataSet(values, "Expenses");
            set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            set1.setDrawIcons(false);

            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_blue);
            set1.setFillDrawable(drawable);

            dataSets.add(set1);

            LineData data = new LineData(dataSets);

            mChart.setData(data);

        } catch (Exception e) {
            e.printStackTrace();
        }

//        textView_total.setText("Rs. " + total);
        mChart.animateX(2500);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
    }

    public void progress(boolean show) {
        if (show) {
            progress_default.setVisibility(View.VISIBLE);
        } else {
            progress_default.setVisibility(View.INVISIBLE);
        }
    }

    private String getJSON() {
        String json = null;
        try {
            InputStream is = AppController.getContext().getAssets().open("p.json"); //personalgraph
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
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
//        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
//        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
//        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
//        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
//        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
//        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
//        Log.i("Entry selected", e.toString());
//        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
//        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
//        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        progress(false);
        button_refresh.setVisibility(View.VISIBLE);
        if (response != null && !response.isEmpty()) {
            linear_main.setVisibility(View.VISIBLE);
            relative_error.setVisibility(View.GONE);
            parseJson(response);
        } else {
            linear_main.setVisibility(View.GONE);
            relative_error.setVisibility(View.VISIBLE);
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong), "OK");
        }
    }

    private void parseJson(String response) {
        try {
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                textView_total.setText(responseObject.getString("totalExpense"));
                textView_recent.setText(responseObject.getString("monthExpense"));
                JSONArray graphArray = responseObject.getJSONArray("graphPoints");
                initGraph(graphArray);
            } else {
                String errorMessage = responseObject.getString("errorMessage");
                showSnackBar(errorMessage, "ReTry");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}