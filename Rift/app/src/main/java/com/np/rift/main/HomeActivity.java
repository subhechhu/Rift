package com.np.rift.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.fcm.Config;
import com.np.rift.fcm.NotificationUtils;
import com.np.rift.serverRequest.ServerGetRequest;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class HomeActivity extends AppCompatActivity implements ServerGetRequest.Response {
    final static int FRAGMENT_COUNT = 3;
    private final String TAG = getClass().getSimpleName();


    TextView textView_user, textView_exp;
    View linearlayout_main;
    AVLoadingIndicatorView progress_primary;
    ViewPager viewPager;
    MainAdapter adapter;
    BottomNavigation bottomNavigation;
    List<Fragment> fragList = new ArrayList<>();
    Fragment[] array = new Fragment[FRAGMENT_COUNT];
    String userId, userName;
    SharedPrefUtil sharedPrefUtil;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        sharedPrefUtil = new SharedPrefUtil();
        userName = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "userName", "user");
        userId = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "userId", "000");

        displayFirebaseRegId();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "fcm message: " + message);

                }
            }
        };

        linearlayout_main = findViewById(R.id.linearlayout_main);
        progress_primary = (AVLoadingIndicatorView) findViewById(R.id.progress_primary);

//        GetTotalExpense();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Rift");

        bottomNavigation = (BottomNavigation) findViewById(R.id.bottomNavigation);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new MainAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(FRAGMENT_COUNT);
        viewPager.setAdapter(adapter);

        bottomNavigation.setOnMenuItemClickListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(@IdRes int i, int i1, boolean b) {
                viewPager.setCurrentItem(i1);
            }

            @Override
            public void onMenuItemReselect(@IdRes int i, int i1, boolean b) {
                viewPager.setCurrentItem(i1);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.setSelectedIndex(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void SetUpGraph() {

    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    private void GetTotalExpense() {
        String url = AppController.getInstance().getString(R.string.domain) + "/getTotalExp?userId=" + userId;
        new ServerGetRequest(this, "GET_EXPENSE").execute(url);
    }

    @Override
    public void getGetResult(String response, String requestCode, int responseCode) {
        if (response != null && !response.isEmpty()) {

        } else {
            Progress(false);
            showSnackBar("Something went wrong. Please Try Again!!!");
        }
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

    private void Progress(boolean show) {
        if (show) {
            progress_primary.show();
        } else {
            progress_primary.hide();
        }
    }

    public void refreshViewpager() {
        int position = viewPager.getCurrentItem();
        if (0 == position) {
            showSnackBar("Refreshing Your Expenses");
        }else if(1== position){
            showSnackBar("Refreshing Your Groups");
        }
        viewPager.setAdapter(adapter);
        bottomNavigation.setSelectedIndex(position, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            refreshViewpager();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(0!=viewPager.getCurrentItem()){
            viewPager.setCurrentItem(0);
        }else {
            finish();
        }
    }
}