package com.np.rift.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.connection.ConnectionLost;
import com.np.rift.fcm.Config;
import com.np.rift.fcm.NotificationUtils;
import com.np.rift.init.LoginActivity;
import com.np.rift.main.groupFragment.JoinGroupFragment;
import com.np.rift.main.menuOptions.AboutFragment;
import com.np.rift.main.menuOptions.EditFragment;
import com.np.rift.util.SharedPrefUtil;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

/**
 * Created by subhechhu on 9/5/2017.
 */

public class HomeActivity extends AppCompatActivity implements JoinGroupFragment.RefreshGroup {
    final static int FRAGMENT_COUNT = 3;
    public static boolean isDeleted = false;
    public static boolean settled = false;
    private final String TAG = getClass().getSimpleName();
    TextView textView_user, textView_exp;
    View linearlayout_main;
    AVLoadingIndicatorView progress_primary;
    ViewPager viewPager;
    MainAdapter adapter;
    BottomNavigation bottomNavigation;
    BadgeProvider provider;
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
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    displayFirebaseRegId();
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
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
        provider = bottomNavigation.getBadgeProvider();
        provider.show(R.id.action_notification);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new MainAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(FRAGMENT_COUNT);
        viewPager.setAdapter(adapter);

        bottomNavigation.setOnMenuItemClickListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(@IdRes int i, int i1, boolean b) {
//                Log.e("TAG", "subhechhu , il: " + i1);
                if (i1 == 2) {
                    bottomNavigation.getBadgeProvider().remove(R.id.action_notification);
                }
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
//                Log.e("TAG", "subhechhu , position: " + position);
                if (position == 2) {
                    bottomNavigation.getBadgeProvider().remove(R.id.action_notification);
                }
                bottomNavigation.setSelectedIndex(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void noInternet() {
        ConnectionLost connectionLostFragment = new ConnectionLost();
        connectionLostFragment.show(getFragmentManager(), "dialogFragment");
    }

    private void displayFirebaseRegId() {
//        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = sharedPrefUtil.getSharedPreferenceString(AppController.getContext(), "regId", "123321");
//        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (settled && viewPager.getCurrentItem() == 1) {
//            settled = false;
//            viewPager.getAdapter().notifyDataSetChanged();
////            showSnackBar("Group Settled");
//        }

        viewPager.getAdapter().notifyDataSetChanged();

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

    private void showSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_LONG);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

    private void showLongSnackBar(String message) {
        final Snackbar _snackbar = Snackbar.make(linearlayout_main, message, Snackbar.LENGTH_INDEFINITE);
        _snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _snackbar.dismiss();
            }
        }).show();
    }

    private void Progress(boolean show) {
        if (show) {
            progress_primary.setVisibility(View.VISIBLE);
        } else {
            progress_primary.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (0 != viewPager.getCurrentItem()) {
            viewPager.setCurrentItem(0);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(),
                        "verified", false);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.about:
                BottomSheetDialogFragment aboutFragment = new AboutFragment();
                aboutFragment.show(getSupportFragmentManager(), aboutFragment.getTag());
                break;
            case R.id.edit_profile:
                Bundle bundle = new Bundle();
                bundle.putString("for", "profile");
                bundle.putString("email", AppController.getUserEmail());
                bundle.putString("userName", AppController.getUserName());
                BottomSheetDialogFragment editFragment = new EditFragment();
                editFragment.setArguments(bundle);
                editFragment.show(getSupportFragmentManager(), editFragment.getTag());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshGroup(String message) {
        viewPager.getAdapter().notifyDataSetChanged();
        showSnackBar(message);
    }

    public Fragment getFragment() {
        return getSupportFragmentManager().getFragments().get(viewPager.getCurrentItem());
    }

    public void refreshAllFragments() {
        adapter.notifyDataSetChanged();
    }
}