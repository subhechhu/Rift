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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.np.rift.main.personalFragment.addExp.PersonalExpenseActivity;
import com.np.rift.serverRequest.ServerGetRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class MainPersonalFragment extends Fragment implements OnChartGestureListener,
        OnChartValueSelectedListener, ServerGetRequest.Response {
    protected FragmentActivity mActivity;
    String TAG = getClass().getSimpleName();
    int fragmentValue;

    TextView textView_total;
    View relative_main;

    Button button_refresh;
    ArrayList<Entry> values;
    ArrayList<ILineDataSet> dataSets;
    MyMarkerView mv;

    FloatingActionButton fab_add;
    float dX;
    float dY;
    int lastAction;
    private LineChart mChart;

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
        Log.d("subhechhu", "A onCreate" + fragmentValue);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_personal, container, false);
        fab_add = fragmentView.findViewById(R.id.fab_add);

        relative_main = fragmentView.findViewById(R.id.relative_main);
        button_refresh = fragmentView.findViewById(R.id.button_refresh);
        textView_total = fragmentView.findViewById(R.id.textView_total);
        mChart = fragmentView.findViewById(R.id.chart_line);
        mChart.setNoDataText("Please Add The Expenses To See The Graph");

        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values.clear();
                dataSets.clear();

                button_refresh.startAnimation(
                        AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_indefinitely));
//                GetPersonalExpense();

                parseJson(getJSON());
            }
        });

//        GetPersonalExpense();

        values = new ArrayList<>();
        dataSets = new ArrayList<>();
        parseJson(getJSON());

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mActivity, PersonalExpenseActivity.class));
            }
        });
        return fragmentView;
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

    private void GetPersonalExpense() {
        String url = AppController.getInstance().getString(R.string.domain)
                + "/detailGraph?userId=" + AppController.getUserId();
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

        if(mv==null){
            mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        }
        mv.setChartView(mChart);
        mChart.setMarker(mv);

        mChart.getAxisRight().setEnabled(false);

        mChart.getViewPortHandler().setMaximumScaleY(2f);
        mChart.getViewPortHandler().setMaximumScaleX(2f);

        try {
            for (int i = 0; i < graphArray.length(); i++) {
                JSONObject graphObject = graphArray.getJSONObject(i);
//                setData(Float.parseFloat(graphObject.getString("date")),
//                        Float.parseFloat(graphObject.getString("amount")));
                values.add(new Entry(Float.parseFloat(graphObject.getString("date")),
                        Float.parseFloat(graphObject.getString("amount"))));
            }
            LineDataSet set1;

            set1 = new LineDataSet(values, "Expenses");
            set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            set1.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
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


    private void setData(float i, float val) {
//        float total = 0;
//        float val = (float) (Math.random() * range) + 3;
//        total += val;
//        values.add(new Entry(i, val));
//        for (int i = 0; i < count; i++) {
//
//            float val = (float) (Math.random() * range) + 3;
//            total += val;
//            values.add(new Entry(i, val));
//        }



//        mChart.getData().setHighlightEnabled(!mChart.getData().isHighlightEnabled());
    }

    private String getJSON() {
        String json = null;
        try {
            InputStream is = AppController.getContext().getAssets().open("personalgraph.json");
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
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {
            parseJson(response);
        } else {
            showSnackBar(AppController.getInstance().getString(R.string.something_went_wrong));
        }
    }

    private void parseJson(String response) {
        try {
            button_refresh.clearAnimation();
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");
            if ("success".equalsIgnoreCase(status)) {
                textView_total.setText(responseObject.getString("totalExpense"));
                JSONArray graphArray = responseObject.getJSONArray("graphPoints");
                initGraph(graphArray);
            } else {
                String errorMessage = responseObject.getString("errorMessage");
                showSnackBar(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}